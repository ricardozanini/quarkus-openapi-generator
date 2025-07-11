package io.quarkiverse.openapi.generator.providers;

import static io.quarkiverse.openapi.generator.AuthConfig.TOKEN_PROPAGATION;

import java.io.IOException;
import java.util.List;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriBuilder;

import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkiverse.openapi.generator.OpenApiGeneratorException;

/**
 * Provider for API Key authentication.
 */
public class ApiKeyAuthenticationProvider extends AbstractAuthProvider {

    static final String USE_AUTHORIZATION_HEADER_VALUE = "use-authorization-header-value";
    private final ApiKeyIn apiKeyIn;
    private final String apiKeyName;

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyAuthenticationProvider.class);

    public ApiKeyAuthenticationProvider(final String openApiSpecId, final String name, final ApiKeyIn apiKeyIn,
            final String apiKeyName, List<OperationAuthInfo> operations, CredentialsProvider credentialsProvider) {
        super(name, openApiSpecId, operations, credentialsProvider);
        this.apiKeyIn = apiKeyIn;
        this.apiKeyName = apiKeyName;
        validateConfig();
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        switch (apiKeyIn) {
            case query:
                requestContext.setUri(
                        UriBuilder.fromUri(requestContext.getUri()).queryParam(apiKeyName, getApiKey(requestContext)).build());
                break;
            case cookie:
                requestContext.getHeaders().add(HttpHeaders.COOKIE,
                        new Cookie.Builder(apiKeyName).value(getApiKey(requestContext)).build());
                break;
            case header:
                if (requestContext.getHeaderString("Authorization") != null
                        && !requestContext.getHeaderString("Authorization").isEmpty()
                        && isUseAuthorizationHeaderValue()) {
                    requestContext.getHeaders().putSingle(apiKeyName, requestContext.getHeaderString("Authorization"));
                } else
                    requestContext.getHeaders().putSingle(apiKeyName, getApiKey(requestContext));
                break;
        }
    }

    private String getApiKey(ClientRequestContext requestContext) {
        final String key = credentialsProvider.getApiKey(CredentialsContext.builder()
                .requestContext(requestContext)
                .openApiSpecId(getOpenApiSpecId())
                .authName(getName())
                .build()).orElse("");

        if (key.isEmpty()) {
            LOGGER.warn("configured {} property (see application.properties) is empty. hint: configure it.",
                    AbstractAuthProvider.getCanonicalAuthConfigPropertyName(ConfigCredentialsProvider.API_KEY,
                            getOpenApiSpecId(),
                            getName()));
        }

        return key;
    }

    private boolean isUseAuthorizationHeaderValue() {
        return ConfigProvider.getConfig()
                .getOptionalValue(getCanonicalAuthConfigPropertyName(USE_AUTHORIZATION_HEADER_VALUE), Boolean.class)
                .orElse(true);
    }

    private void validateConfig() {
        if (isTokenPropagation()) {
            throw new OpenApiGeneratorException(
                    "Token propagation is not admitted for the OpenApi securitySchemes of \"type\": \"apiKey\"." +
                            " A potential source of the problem might be that the configuration property "
                            + getCanonicalAuthConfigPropertyName(TOKEN_PROPAGATION) +
                            " was set with the value true in your application, please check your configuration.");
        }
    }
}
