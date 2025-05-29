package io.quarkiverse.openapi.generator.providers;

import java.util.List;

import io.quarkus.arc.Arc;

public final class AuthProviderFactory {

    public static AuthProvider createBearer(String sanitizedName, String scheme, String openApiSpecId,
            List<OperationAuthInfo> operations) {
        CredentialsProvider cp = Arc.container().instance(CredentialsProvider.class).get();
        return new BearerAuthenticationProvider(openApiSpecId, sanitizedName, scheme, operations, cp);
    }

    public static AuthProvider createApiKey(final String openApiSpecId, final String name, final ApiKeyIn apiKeyIn,
            final String apiKeyName, List<OperationAuthInfo> operations) {
        CredentialsProvider cp = Arc.container().instance(CredentialsProvider.class).get();
        return new ApiKeyAuthenticationProvider(openApiSpecId, name, apiKeyIn, apiKeyName, operations, cp);
    }

    public static AuthProvider createBasic(final String openApiSpecId, String name, List<OperationAuthInfo> operations) {
        CredentialsProvider cp = Arc.container().instance(CredentialsProvider.class).get();
        return new BasicAuthenticationProvider(openApiSpecId, name, operations, cp);
    }
}
