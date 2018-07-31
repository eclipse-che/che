/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.machine.authentication.server.signature;

import com.google.common.annotations.Beta;

/**
 * Represents signature key pair with public and private part.
 *
 * @see SignatureKey
 * @author Anton Korneta
 */
@Beta
public interface SignatureKeyPair {

  /** Returns unique identifier for this sign key pair. */
  String getId();

  /** Returns public part for this sign key pair. */
  SignatureKey getPublicKey();

  /** Returns private part for this sign key pair. */
  SignatureKey getPrivateKey();
}
