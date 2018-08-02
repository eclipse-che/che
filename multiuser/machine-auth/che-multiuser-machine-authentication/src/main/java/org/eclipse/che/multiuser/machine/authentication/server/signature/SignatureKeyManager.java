/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.machine.authentication.server.signature;

import static org.eclipse.che.commons.lang.NameGenerator.generate;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Iterator;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.multiuser.machine.authentication.server.signature.model.impl.SignatureKeyPairImpl;
import org.eclipse.che.multiuser.machine.authentication.server.signature.spi.SignatureKeyDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages signature keys.
 *
 * @author Anton Korneta
 */
@Beta
@Singleton
public class SignatureKeyManager {

  public static final String PKCS_8 = "PKCS#8";
  public static final String X_509 = "X.509";

  private static final Logger LOG = LoggerFactory.getLogger(SignatureKeyManager.class);

  private final int keySize;

  private final String algorithm;
  private final SignatureKeyDao signatureKeyDao;

  @Inject
  @SuppressWarnings("unused")
  private DBInitializer dbInitializer;

  private KeyPair cachedPair;

  @Inject
  public SignatureKeyManager(
      @Named("che.auth.signature_key_size") int keySize,
      @Named("che.auth.signature_key_algorithm") String algorithm,
      SignatureKeyDao signatureKeyDao) {
    this.keySize = keySize;
    this.algorithm = algorithm;
    this.signatureKeyDao = signatureKeyDao;
  }

  /** Returns cached instance of {@link KeyPair} or null when failed to load key pair. */
  @Nullable
  public KeyPair getKeyPair() {
    if (cachedPair == null) {
      loadKeyPair();
    }
    return cachedPair;
  }

  /** Loads signature key pair if no existing keys found then stores a newly generated key pair. */
  @PostConstruct
  @VisibleForTesting
  void loadKeyPair() {
    try {
      final Iterator<SignatureKeyPairImpl> it = signatureKeyDao.getAll(1, 0).getItems().iterator();
      if (it.hasNext()) {
        cachedPair = toJavaKeyPair(it.next());
        return;
      }
    } catch (ServerException ex) {
      LOG.error("Failed to load signature keys. Cause: {}", ex.getMessage());
      return;
    }

    try {
      cachedPair = toJavaKeyPair(signatureKeyDao.create(generateKeyPair()));
    } catch (ConflictException | ServerException ex) {
      LOG.error("Failed to store signature keys. Cause: {}", ex.getMessage());
    }
  }

  @VisibleForTesting
  SignatureKeyPairImpl generateKeyPair() throws ServerException {
    final KeyPairGenerator kpg;
    try {
      kpg = KeyPairGenerator.getInstance(algorithm);
    } catch (NoSuchAlgorithmException ex) {
      throw new ServerException(ex.getMessage(), ex);
    }
    kpg.initialize(keySize);
    final KeyPair pair = kpg.generateKeyPair();
    final SignatureKeyPairImpl kp =
        new SignatureKeyPairImpl(generate("signatureKey", 16), pair.getPublic(), pair.getPrivate());
    LOG.info("Generated signature key pair with id {} and algorithm {}.", kp.getId(), algorithm);
    return kp;
  }

  /** Converts {@link SignatureKeyPair} to {@link KeyPair}. */
  public static KeyPair toJavaKeyPair(SignatureKeyPair keyPair) throws ServerException {
    try {
      final PrivateKey privateKey =
          KeyFactory.getInstance(keyPair.getPrivateKey().getAlgorithm())
              .generatePrivate(getKeySpec(keyPair.getPrivateKey()));
      final PublicKey publicKey =
          KeyFactory.getInstance(keyPair.getPublicKey().getAlgorithm())
              .generatePublic(getKeySpec(keyPair.getPublicKey()));
      return new KeyPair(publicKey, privateKey);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
      LOG.error("Failed to convert signature key pair to Java keys. Cause: {}", ex.getMessage());
      throw new ServerException("Failed to convert signature key pair to Java keys.");
    }
  }

  /** Returns key spec by key format and encoded data. */
  private static EncodedKeySpec getKeySpec(SignatureKey key) {
    switch (key.getFormat()) {
      case PKCS_8:
        return new PKCS8EncodedKeySpec(key.getEncoded());
      case X_509:
        return new X509EncodedKeySpec(key.getEncoded());
      default:
        throw new IllegalArgumentException(
            String.format("Unsupported key spec '%s' for signature keys", key.getFormat()));
    }
  }
}
