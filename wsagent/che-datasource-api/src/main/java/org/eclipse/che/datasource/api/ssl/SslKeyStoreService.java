package org.eclipse.che.datasource.api.ssl;

import com.google.inject.Inject;

import javax.ws.rs.Path;

/**
 * Created by test on 7/15/17.
 */
public class SslKeyStoreService {
    protected KeyStoreObject keyStoreObject;
    protected TrustStoreObject trustStoreObject;

    // userProfileDao, injected with ...
    @Inject
    public SslKeyStoreService(KeyStoreObject keyStoreObject, TrustStoreObject trustStoreObject) {
        this.keyStoreObject = keyStoreObject;
        this.trustStoreObject = trustStoreObject;
    }

    public static String getDefaultTrustorePassword() {
        if (System.getProperty("com.codenvy.security.masterpwd") == null) {
            System.setProperty("com.codenvy.security.masterpwd", "changeMe");
        }
        return System.getProperty("com.codenvy.security.masterpwd");
    }

    public static String getDefaultKeystorePassword() {
        if (System.getProperty("com.codenvy.security.masterpwd") == null) {
            System.setProperty("com.codenvy.security.masterpwd", "changeMe");
        }
        return System.getProperty("com.codenvy.security.masterpwd");
    }

    @Path("keystore")
    public KeyStoreObject getClientKeyStore() throws Exception {
        return keyStoreObject;
    }

    @Path("truststore")
    public TrustStoreObject getTrustStore() throws Exception {
        return trustStoreObject;
    }
}
