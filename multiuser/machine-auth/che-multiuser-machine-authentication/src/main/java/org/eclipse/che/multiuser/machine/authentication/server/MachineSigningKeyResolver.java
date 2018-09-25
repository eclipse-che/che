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
package org.eclipse.che.multiuser.machine.authentication.server;

import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.MACHINE_TOKEN_KIND;
import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.WORKSPACE_ID_CLAIM;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import java.security.Key;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.multiuser.machine.authentication.server.signature.SignatureKeyManager;
import org.eclipse.che.multiuser.machine.authentication.server.signature.SignatureKeyManagerException;

/** Resolves signing key pair based on workspace Id claim of token. */
@Singleton
public class MachineSigningKeyResolver extends SigningKeyResolverAdapter {

  private final SignatureKeyManager keyManager;

  @Inject
  public MachineSigningKeyResolver(SignatureKeyManager keyManager) {
    this.keyManager = keyManager;
  }

  @Override
  public Key resolveSigningKey(JwsHeader header, Claims claims) {
    if (!MACHINE_TOKEN_KIND.equals(header.get("kind"))) {
      throw new NotMachineTokenJwtException();
    }
    String wsId = claims.get(WORKSPACE_ID_CLAIM, String.class);
    if (wsId == null) {
      throw new JwtException(
          "Unable to fetch signature key pair: no workspace id present in token");
    }
    try {
      return keyManager.getOrCreateKeyPair(wsId).getPublic();
    } catch (SignatureKeyManagerException e) {
      throw new JwtException("Unable to fetch signature key pair:" + e.getMessage(), e);
    }
  }
}
