// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

// Adapted for current MI usage and container deployment from https://github.com/Azure/azure-event-hubs-for-kafka/tree/master/tutorials/oauth/java/managedidentity/producer

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.security.auth.AuthenticateCallbackHandler;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerToken;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerTokenCallback;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;

public class CustomAuthenticateCallbackHandler implements AuthenticateCallbackHandler {

    // Executor service for managing threads
    final static ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

    // DefaultAzureCredential for Managed Identity or other Azure authentication methods
    final static DefaultAzureCredential CREDENTIALS = new DefaultAzureCredentialBuilder()
            .managedIdentityClientId(<managedIdentityClientId>)
            .build();

    private String sbUri; // Service Bus URI

    @Override
    public void configure(Map<String, ?> configs, String mechanism, List<AppConfigurationEntry> jaasConfigEntries) {
        String bootstrapServer = Arrays.asList(configs.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)).get(0).toString();
        bootstrapServer = bootstrapServer.replaceAll("\\[|\\]", ""); // Remove brackets if present
        URI uri = URI.create("https://" + bootstrapServer);
        this.sbUri = uri.getScheme() + "://" + uri.getHost();
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof OAuthBearerTokenCallback) {
                try {
                    OAuthBearerToken token = getOAuthBearerToken();
                    OAuthBearerTokenCallback oauthCallback = (OAuthBearerTokenCallback) callback;
                    oauthCallback.token(token);
                } catch (InterruptedException | ExecutionException | TimeoutException | ParseException e) {
                    e.printStackTrace();
                }
            } else {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }

    private OAuthBearerToken getOAuthBearerToken() throws InterruptedException, ExecutionException, TimeoutException, IOException, ParseException {
        String accessToken = "";
        try {
            accessToken = CREDENTIALS.getToken(new TokenRequestContext()
                                .addScopes(sbUri + "/.default")
                                )
                            .block()
                            .getToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        JWT jwt = JWTParser.parse(accessToken);
        JWTClaimsSet claims = jwt.getJWTClaimsSet();
    
        return new OAuthBearerTokenImp(accessToken, claims.getExpirationTime());
    }
    @Override
    public void close() throws KafkaException {
        // No operation needed for cleanup
    }
}