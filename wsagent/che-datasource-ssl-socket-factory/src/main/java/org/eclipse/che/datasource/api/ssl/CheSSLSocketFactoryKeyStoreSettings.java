package org.eclipse.che.datasource.api.ssl;

/**
 * Created by test on 7/15/17.
 */
public class CheSSLSocketFactoryKeyStoreSettings {
    protected byte[] ksContent;
    protected String ksPassword;
    protected byte[] tsContent;
    protected String tsPassword;

    public CheSSLSocketFactoryKeyStoreSettings(byte[] sslKeyStoreContent,
                                                   String keyStorePassword,
                                                   byte[] sslTrustStoreContent,
                                                   String trustStorePassword) {
        ksContent = sslKeyStoreContent;
        ksPassword = keyStorePassword;
        tsContent = sslTrustStoreContent;
        tsPassword = trustStorePassword;
    }

    public CheSSLSocketFactoryKeyStoreSettings() {
    }

    public String getKeyStorePassword() {
        return ksPassword;
    }

    public byte[] getKeyStoreContent() {
        return ksContent;
    }

    public byte[] getTrustStoreContent() {
        return tsContent;
    }

    public String getTrustStorePassword() {
        return tsPassword;
    }

    public CheSSLSocketFactoryKeyStoreSettings withKeyStorePassword(String keyStorePassword) {
        ksPassword = keyStorePassword;
        return this;
    }

    public CheSSLSocketFactoryKeyStoreSettings withKeyStoreContent(byte[] sslKeyStoreContent) {
        ksContent = sslKeyStoreContent;
        return this;
    }

    public CheSSLSocketFactoryKeyStoreSettings withTrustStoreContent(byte[] sslTrustStoreContent) {
        tsContent = sslTrustStoreContent;
        return this;
    }

    public CheSSLSocketFactoryKeyStoreSettings withTrustStorePassword(String trustStorePassword) {
        tsPassword = trustStorePassword;
        return this;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        ksPassword = keyStorePassword;
    }

    public void setKeyStoreContent(byte[] sslKeyStoreContent) {
        ksContent = sslKeyStoreContent;
    }

    public void setTrustStoreContent(byte[] sslTrustStoreContent) {
        tsContent = sslTrustStoreContent;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        tsPassword = trustStorePassword;
    }
}
