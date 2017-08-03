package org.eclipse.che.datasource.api.ssl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by test on 7/15/17.
 */
public class CheSSLSocketFactory extends SSLSocketFactory {
    private static final Logger LOG =
            LoggerFactory.getLogger(CheSSLSocketFactory.class);

    public static ThreadLocal<CheSSLSocketFactoryKeyStoreSettings> keystore =
            new ThreadLocal<CheSSLSocketFactoryKeyStoreSettings>();
    public static SSLSocketFactory defaultSocketFactory;
    protected ThreadLocal<SSLSocketFactory> wrappedSocketFactory = new ThreadLocal<SSLSocketFactory>();

    public static boolean isNullOrEmpty(byte[] byteArray) {
        return byteArray == null || byteArray.length <= 0;
    }

    protected void reloadIfNeeded() {
        if (keystore.get() != null) {
            CheSSLSocketFactoryKeyStoreSettings keystoreConfig = keystore.get();
            keystore.set(null);
            wrappedSocketFactory.set(getSSLSocketFactoryDefaultOrConfigured(keystoreConfig));
        }
        if (wrappedSocketFactory.get() == null) {
            wrappedSocketFactory.set(getDefaultSSLSocketFactory());
        }
    }

    protected SSLSocketFactory getDefaultSSLSocketFactory() {
        try {
            if (defaultSocketFactory == null) {
                defaultSocketFactory = SSLContext.getDefault().getSocketFactory();
            }
            return defaultSocketFactory;
        } catch (NoSuchAlgorithmException e) {
            throw new Error("Couldn't set default socket factory", e);
        }
    }

    protected SSLSocketFactory getSSLSocketFactoryDefaultOrConfigured(CheSSLSocketFactoryKeyStoreSettings keystoreConfig) {
        TrustManagerFactory tmf = null;
        KeyManagerFactory kmf = null;

        try {
            tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        } catch (NoSuchAlgorithmException nsae) {
            LOG.error("An error occured while setting up custom SSL Socket factory. Falling back the the default one", nsae);
            return getDefaultSSLSocketFactory();
        }

        if (keystoreConfig.getKeyStoreContent() != null && keystoreConfig.getKeyStoreContent().length > 0) {
            char[] password = (keystoreConfig.getKeyStorePassword() == null) ? new char[0]
                    : keystoreConfig.getKeyStorePassword().toCharArray();
            try (InputStream fis = new ByteArrayInputStream(keystoreConfig.getKeyStoreContent())) {
                KeyStore ks = KeyStore.getInstance("JKS");
                ks.load(fis, password);
                kmf.init(ks, password);
            } catch (Exception e) {
                LOG.error("An error occured while setting up custom SSL Socket factory. Falling back the the default one", e);
                return getDefaultSSLSocketFactory();
            }
        }


        if (keystoreConfig.getTrustStoreContent() != null && keystoreConfig.getTrustStoreContent().length > 0) {
            LOG.info("Initializing truststore from file");
            char[] password = (keystoreConfig.getTrustStorePassword() == null) ? new char[0]
                    : keystoreConfig.getTrustStorePassword().toCharArray();
            try (InputStream fis = new ByteArrayInputStream(keystoreConfig.getTrustStoreContent())) {
                KeyStore ks = KeyStore.getInstance("JKS");
                ks.load(fis, password);
                tmf.init(ks);
            } catch (Exception e) {
                LOG.error("An error occured while setting up custom SSL Socket factory. Falling back the the default one", e);
                return getDefaultSSLSocketFactory();
            }
        }

        SSLContext sslContext = null;

        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(isNullOrEmpty(keystoreConfig.getKeyStoreContent()) ? null : kmf.getKeyManagers(),
                    isNullOrEmpty(keystoreConfig.tsContent) ?
                            new X509TrustManager[]{new X509TrustManager() {
                                @Override
                                public void checkClientTrusted(X509Certificate[] chain,
                                                               String authType) {
                                    // return without complaint
                                }

                                @Override
                                public void checkServerTrusted(X509Certificate[] chain,
                                                               String authType) throws CertificateException {
                                    // return without complaint
                                }

                                @Override
                                public X509Certificate[] getAcceptedIssuers() {
                                    return null;
                                }
                            }
                            } : tmf.getTrustManagers(), null);

            return sslContext.getSocketFactory();
        } catch (Exception e) {
            LOG.error("An error occured while setting up custom SSL Socket factory. Falling back the the default one", e);
            return (javax.net.ssl.SSLSocketFactory) javax.net.ssl.SSLSocketFactory
                    .getDefault();
        }
    }

    @Override
    public Socket createSocket() throws IOException {
        reloadIfNeeded();
        return wrappedSocketFactory.get().createSocket();
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        reloadIfNeeded();
        return wrappedSocketFactory.get().createSocket(host, port);
    }

    @Override
    public String[] getDefaultCipherSuites() {
        reloadIfNeeded();
        return wrappedSocketFactory.get().getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        reloadIfNeeded();
        return wrappedSocketFactory.get().getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        reloadIfNeeded();
        return wrappedSocketFactory.get().createSocket(s, host, port, autoClose);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        reloadIfNeeded();
        return wrappedSocketFactory.get().createSocket(host, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        reloadIfNeeded();
        return wrappedSocketFactory.get().createSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        reloadIfNeeded();
        return wrappedSocketFactory.get().createSocket(address, port, localAddress, localPort);
    }

}
