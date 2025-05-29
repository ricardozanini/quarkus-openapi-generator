package io.quarkiverse.openapi.generator.oidc.providers;

import java.util.List;

import io.quarkiverse.openapi.generator.OidcClient;
import io.quarkiverse.openapi.generator.providers.AuthProvider;
import io.quarkiverse.openapi.generator.providers.CredentialsProvider;
import io.quarkiverse.openapi.generator.providers.OperationAuthInfo;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import jakarta.enterprise.inject.Instance;

public final class OidcAuthenticationFactory {

    public static AuthProvider createOidc(String name,
                                          String openApiSpecId,
                                          List<OperationAuthInfo> operations) {

        InstanceHandle<OAuth2AuthenticationProvider.OidcClientRequestFilterDelegate> del =
                Arc.container().instance(
                        OAuth2AuthenticationProvider.OidcClientRequestFilterDelegate.class,
                        new OidcClient.Literal(name)
                );

        OAuth2AuthenticationProvider.OidcClientRequestFilterDelegate delegate = del.get();

        Instance<CredentialsProvider> cpInst = Arc.container().select(CredentialsProvider.class);
        if (cpInst.isUnsatisfied()) {
            throw new IllegalStateException("No CredentialsProvider bean found");
        }
        CredentialsProvider cp = cpInst.get();

        return new OAuth2AuthenticationProvider(name, openApiSpecId, delegate, operations, cp);
    }

}
