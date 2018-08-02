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

import com.google.common.annotations.Beta;

/**
 * Defines signature key interface.
 *
 * @author Anton Korneta
 */
@Beta
public interface SignatureKey {

  /** Returns algorithm of this signature key. */
  String getAlgorithm();

  /** Returns encoding format of this signature key. */
  String getFormat();

  /** Returns encoded value of this signature key. */
  byte[] getEncoded();
}
