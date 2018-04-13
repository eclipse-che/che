/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che;

import com.google.inject.Provider;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Provides instance of {@link PublicKey} created from environment.
 *
 * @author Anton Korneta
 */
public class MachinePublicKeyProvider implements Provider<PublicKey> {

  private final String publicKey;
  private final String algorithm;

  @Inject
  public MachinePublicKeyProvider(
      @Named("env.CHE_MACHINE_AUTH_SIGNATURE__ALGORITHM") String alg,
      @Named("env.CHE_MACHINE_AUTH_SIGNATURE__PUBLIC__KEY") String key) {
    this.publicKey = key;
    this.algorithm = alg;
  }

  @Override
  public PublicKey get() {
    try {
      return KeyFactory.getInstance(algorithm)
          .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey)));
    } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
      throw new IllegalStateException(ex.getCause());
    }
  }
}
