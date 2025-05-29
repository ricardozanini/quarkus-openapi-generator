package io.quarkiverse.openapi.generator.oidc.providers;

import java.util.List;
import java.util.function.Supplier;

import io.quarkiverse.openapi.generator.oidc.ClassicOidcClientRequestFilterDelegate;
import io.quarkiverse.openapi.generator.oidc.ReactiveOidcClientRequestFilterDelegate;
import io.quarkiverse.openapi.generator.providers.AuthProvider;
import io.quarkiverse.openapi.generator.providers.OperationAuthInfo;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class OidcAuthenticationRecorder {

    public RuntimeValue<AuthProvider> recordOauthAuthProvider(String name, String openApiSpecId,
            List<OperationAuthInfo> operations) {

        return new RuntimeValue<>(OidcAuthenticationFactory.createOidc(name, openApiSpecId, operations));
    }

    public Supplier<OAuth2AuthenticationProvider.OidcClientRequestFilterDelegate> recordOidcClient(String flowName,
            boolean reactive) {
        if (reactive) {
            return () -> new ReactiveOidcClientRequestFilterDelegate(flowName);
        }

        return () -> new ClassicOidcClientRequestFilterDelegate(flowName);
    }

}