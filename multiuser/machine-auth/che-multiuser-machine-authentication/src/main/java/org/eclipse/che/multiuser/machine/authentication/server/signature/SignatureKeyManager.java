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

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;

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
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
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
  private final EventService eventService;
  private final EventSubscriber<?> workspaceEventsSubscriber;

  @Inject
  @SuppressWarnings("unused")
  private DBInitializer dbInitializer;

  @Inject
  public SignatureKeyManager(
      @Named("che.auth.signature_key_size") int keySize,
      @Named("che.auth.signature_key_algorithm") String algorithm,
      EventService eventService,
      SignatureKeyDao signatureKeyDao) {
    this.keySize = keySize;
    this.algorithm = algorithm;
    this.eventService = eventService;
    this.signatureKeyDao = signatureKeyDao;
    this.workspaceEventsSubscriber =
        new EventSubscriber<WorkspaceStatusEvent>() {
          @Override
          public void onEvent(WorkspaceStatusEvent event) {
            if (event.getStatus() == STOPPED) {
              removeKeyPair(event.getWorkspaceId());
            }
          }
        };
  }

  /**
   * Returns instance of {@link KeyPair} for given workspace.
   *
   * @throws SignatureKeyManagerException when stored keypair is incorrect (e.g. has bad algorithm
   *     or keyspec) or other error
   */
  public KeyPair getOrCreateKeyPair(String workspaceId) throws SignatureKeyManagerException {
    SignatureKeyPair keyPair;
    try {
      try {
        keyPair = signatureKeyDao.get(workspaceId);
      } catch (NotFoundException e) {
        keyPair = generateKeyPair(workspaceId);
      }
    } catch (NoSuchAlgorithmException | ServerException | ConflictException ex) {
      LOG.error(
          "Failed to load signature keys for ws  {}. Cause: {}", workspaceId, ex.getMessage());
      throw new SignatureKeyManagerException(ex.getMessage(), ex);
    }
    return toJavaKeyPair(keyPair);
  }

  /** Removes key pair from cache and DB. */
  @VisibleForTesting
  void removeKeyPair(String workspaceId) {
    try {
      signatureKeyDao.remove(workspaceId);
      LOG.debug("Removed signature key pair for ws id {}.", workspaceId);
    } catch (ServerException e) {
      LOG.error(
          "Unable to cleanup machine token signature keypairs for ws {}. Cause: {}",
          workspaceId,
          e.getMessage());
    }
  }

  @VisibleForTesting
  SignatureKeyPair generateKeyPair(String workspaceId)
      throws NoSuchAlgorithmException, ServerException, ConflictException {
    try {
      KeyPairGenerator kpg = KeyPairGenerator.getInstance(algorithm);
      kpg.initialize(keySize);
      final KeyPair pair = kpg.generateKeyPair();
      final SignatureKeyPairImpl kp =
          new SignatureKeyPairImpl(workspaceId, pair.getPublic(), pair.getPrivate());
      LOG.debug(
          "Generated signature key pair with ws id {} and algorithm {}.",
          kp.getWorkspaceId(),
          algorithm);
      return signatureKeyDao.create(kp);
    } catch (NoSuchAlgorithmException | ConflictException | ServerException ex) {
      LOG.error(
          "Unable to generate signature keypair for ws {}. Cause: {}",
          workspaceId,
          ex.getMessage());
      throw ex;
    }
  }

  /** Returns key spec by key format and encoded data. */
  private EncodedKeySpec getKeySpec(SignatureKey key) {
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

  private KeyPair toJavaKeyPair(SignatureKeyPair keyPair) throws SignatureKeyManagerException {
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
      throw new SignatureKeyManagerException("Failed to convert signature key pair to Java keys.");
    }
  }

  @VisibleForTesting
  @PostConstruct
  void subscribe() {
    eventService.subscribe(workspaceEventsSubscriber);
  }
}
