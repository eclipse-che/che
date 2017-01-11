/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.docker.client;

import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.eclipse.che.commons.lang.NameGenerator;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * @author andrew00x
 */
public class DockerCertificates {
    public static final String DEFAULT_CA_CERT_NAME     = "ca.pem";
    public static final String DEFAULT_CLIENT_CERT_NAME = "cert.pem";
    public static final String DEFAULT_CLIENT_KEY_NAME  = "key.pem";

    private static final char[] KEY_STORE_PASSWORD = NameGenerator.generate(null, 12).toCharArray();

    public static DockerCertificates loadFromDirectory(String dockerCertDir) {
        return loadFromDirectory(Paths.get(dockerCertDir));
    }

    public static DockerCertificates loadFromDirectory(File dockerCertDir) {
        return loadFromDirectory(dockerCertDir.toPath());
    }

    public static DockerCertificates loadFromDirectory(Path dockerCertDirPath) {
        try {
            final Path caCertPath = dockerCertDirPath.resolve(DEFAULT_CA_CERT_NAME);
            final Path clientKeyPath = dockerCertDirPath.resolve(DEFAULT_CLIENT_KEY_NAME);
            final Path clientCertPath = dockerCertDirPath.resolve(DEFAULT_CLIENT_CERT_NAME);
            final CertificateFactory cf = CertificateFactory.getInstance("X.509");
            final Certificate caCert = getCertificate(caCertPath, cf);
            final Certificate clientCert = getCertificate(clientCertPath, cf);
            final PrivateKey clientKey = getPrivateKey(clientKeyPath);
            final KeyStore keyStore = createKeyStore(clientCert, clientKey);
            final KeyStore trustStore = createTrustStore(caCert);
            final KeyManager[] keyManagers = loadKeyManagers(keyStore, KEY_STORE_PASSWORD);
            final TrustManager[] trustManagers = loadTrustManagers(trustStore);
            return new DockerCertificates(createSSLContext(keyManagers, trustManagers));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Certificate getCertificate(Path caCertPath, CertificateFactory cf) throws IOException, CertificateException {
        try (InputStream inputStream = Files.newInputStream(caCertPath)) {
            return cf.generateCertificate(inputStream);
        }
    }

    private static PrivateKey getPrivateKey(Path clientKeyPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        final PEMKeyPair clientKeyPair;
        try (Reader reader = Files.newBufferedReader(clientKeyPath, Charset.defaultCharset())) {
            clientKeyPair = (PEMKeyPair)new PEMParser(reader).readObject();
        }
        final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(clientKeyPair.getPrivateKeyInfo().getEncoded());
        final KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    private static KeyStore createKeyStore(Certificate clientCert, PrivateKey clientKey)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("client", clientCert);
        keyStore.setKeyEntry("key", clientKey, KEY_STORE_PASSWORD, new Certificate[]{clientCert});
        return keyStore;
    }

    private static KeyStore createTrustStore(Certificate caCert)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        final KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        trustStore.setEntry("ca", new KeyStore.TrustedCertificateEntry(caCert), null);
        return trustStore;
    }

    private static KeyManager[] loadKeyManagers(KeyStore keystore, char[] keyPassword)
            throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keystore, keyPassword);
        return kmf.getKeyManagers();
    }

    private static TrustManager[] loadTrustManagers(KeyStore trustStore) throws NoSuchAlgorithmException, KeyStoreException {
        final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        return tmf.getTrustManagers();
    }

    private static SSLContext createSSLContext(KeyManager[] keyManagers, TrustManager[] trustManagers)
            throws NoSuchAlgorithmException, KeyManagementException {
        final SSLContext sslcontext = SSLContext.getInstance("TLS");
        sslcontext.init(keyManagers, trustManagers, null);
        return sslcontext;
    }

    private final SSLContext sslContext;

    private DockerCertificates(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    public SSLContext getSslContext() {
        return sslContext;
    }
}