package io.quarkiverse.openapi.generator.providers;

import static io.quarkiverse.openapi.generator.OpenApiGeneratorConfig.RUNTIME_TIME_CONFIG_PREFIX;
import static io.quarkiverse.openapi.generator.providers.AbstractAuthenticationPropagationHeadersFactory.propagationHeaderName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.config.ConfigProvider;

import io.quarkiverse.openapi.generator.AuthConfig;

public abstract class AbstractAuthProvider implements AuthProvider {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String BASIC_PREFIX = "Basic ";
    private static final String CONFIG_PATTERN = "quarkus." + RUNTIME_TIME_CONFIG_PREFIX + ".%s.auth.%s.%s";
    private final String openApiSpecId;
    private final String name;
    private final List<OperationAuthInfo> applyToOperations = new ArrayList<>();
    private final CredentialsProvider credentialsProvider;

    protected AbstractAuthProvider(String name, String openApiSpecId, List<OperationAuthInfo> operations,
            CredentialsProvider credentialsProvider) {
        this.name = name;
        this.openApiSpecId = openApiSpecId;
        this.applyToOperations.addAll(operations);
        this.credentialsProvider = credentialsProvider;
    }

    /**
     * Strip the given prefix (case-insensitive) from the start of the token.
     */
    protected static String stripPrefix(String token, String prefix) {
        if (token != null && token.regionMatches(true, 0, prefix, 0, prefix.length())) {
            return token.substring(prefix.length());
        }
        return token;
    }

    protected static String sanitizeBearerToken(String token) {
        return stripPrefix(token, BEARER_PREFIX);
    }

    protected static String sanitizeBasicToken(String token) {
        return stripPrefix(token, BASIC_PREFIX);
    }

    /**
     * Static form for building canonical config keys.
     */
    public static String getCanonicalAuthConfigPropertyName(String authPropertyName, String openApiSpecId, String authName) {
        return String.format(CONFIG_PATTERN, openApiSpecId, authName, authPropertyName);
    }

    protected final CredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getOpenApiSpecId() {
        return openApiSpecId;
    }

    @Override
    public List<OperationAuthInfo> operationsToFilter() {
        return applyToOperations;
    }

    /**
     * Should the original request's authorization header be propagated?
     */
    public boolean isTokenPropagation() {
        return ConfigProvider.getConfig()
                .getOptionalValue(getCanonicalAuthConfigPropertyName(AuthConfig.TOKEN_PROPAGATION), Boolean.class)
                .orElse(false);
    }

    /**
     * If propagation is enabled, returns the header value that was forwarded during propagation.
     */
    public Optional<String> getTokenForPropagation(MultivaluedMap<String, Object> httpHeaders) {
        String headerName = Optional.ofNullable(getHeaderName()).orElse(HttpHeaders.AUTHORIZATION);
        return Optional.ofNullable(httpHeaders.getFirst(propagationHeaderName(openApiSpecId, name, headerName)))
                .map(Object::toString);
    }

    public String getHeaderName() {
        return ConfigProvider.getConfig()
                .getOptionalValue(getCanonicalAuthConfigPropertyName(AuthConfig.HEADER_NAME), String.class).orElse(null);
    }

    /**
     * Builds the canonical config key for this auth provider and property.
     */
    public final String getCanonicalAuthConfigPropertyName(String authPropertyName) {
        return String.format(CONFIG_PATTERN, openApiSpecId, name, authPropertyName);
    }
}
