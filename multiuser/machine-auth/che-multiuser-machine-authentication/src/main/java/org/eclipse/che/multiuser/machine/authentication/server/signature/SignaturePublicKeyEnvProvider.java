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

import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.SIGNATURE_PUBLIC_KEY_ENV;

import java.util.Base64;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.provision.env.EnvVarProvider;
import org.eclipse.che.commons.lang.Pair;

/**
 * Provides a public part of a signature key into machine environment.
 *
 * @author Anton Korneta
 */
public class SignaturePublicKeyEnvProvider implements EnvVarProvider {

  private final SignatureKeyManager keyManager;

  @Inject
  public SignaturePublicKeyEnvProvider(SignatureKeyManager keyManager) {
    this.keyManager = keyManager;
  }

  @Override
  public Pair<String, String> get(RuntimeIdentity runtimeIdentity) {
    return Pair.of(
        SIGNATURE_PUBLIC_KEY_ENV,
        new String(Base64.getEncoder().encode(keyManager.getKeyPair().getPublic().getEncoded())));
  }
}
