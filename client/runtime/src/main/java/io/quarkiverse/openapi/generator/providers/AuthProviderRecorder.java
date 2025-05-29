package io.quarkiverse.openapi.generator;

import java.util.List;
import java.util.function.Function;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.util.TypeLiteral;

import io.quarkiverse.openapi.generator.providers.ApiKeyIn;
import io.quarkiverse.openapi.generator.providers.AuthProvider;
import io.quarkiverse.openapi.generator.providers.AuthProviderFactory;
import io.quarkiverse.openapi.generator.providers.BaseCompositeAuthenticationProvider;
import io.quarkiverse.openapi.generator.providers.OperationAuthInfo;
import io.quarkus.arc.SyntheticCreationalContext;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class AuthProviderRecorder {

    public Function<SyntheticCreationalContext<BaseCompositeAuthenticationProvider>, BaseCompositeAuthenticationProvider> recordCompositeProvider(
            String openApiSpecId) {
        return ctx -> {
            Instance<AuthProvider> providers = ctx.getInjectedReference(new TypeLiteral<>() {
            }, new OpenApiSpec.Literal(openApiSpecId));
            List<AuthProvider> list = providers.stream().toList();
            return new BaseCompositeAuthenticationProvider(list);
        };
    }

    public RuntimeValue<AuthProvider> recordApiKeyAuthProvider(String name, String openApiSpecId, ApiKeyIn apiKeyIn,
            String apiKeyName, List<OperationAuthInfo> operations) {
        return new RuntimeValue<>(AuthProviderFactory.createApiKey(openApiSpecId, name, apiKeyIn, apiKeyName, operations));
    }

    public RuntimeValue<AuthProvider> recordBearerAuthProvider(String name, String scheme, String openApiSpecId,
            List<OperationAuthInfo> operations) {
        return new RuntimeValue<>(AuthProviderFactory.createBearer(name, scheme, openApiSpecId, operations));
    }

    public RuntimeValue<AuthProvider> recordBasicAuthProvider(String name, String openApiSpecId,
            List<OperationAuthInfo> operations) {
        return new RuntimeValue<>(AuthProviderFactory.createBasic(name, openApiSpecId, operations));
    }

}
