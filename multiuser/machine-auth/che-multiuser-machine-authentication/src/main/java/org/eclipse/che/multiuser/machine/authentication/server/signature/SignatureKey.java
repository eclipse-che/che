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
