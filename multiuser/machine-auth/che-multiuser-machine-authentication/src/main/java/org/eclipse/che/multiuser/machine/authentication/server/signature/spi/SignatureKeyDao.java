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
package org.eclipse.che.multiuser.machine.authentication.server.signature.spi;

import com.google.common.annotations.Beta;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.multiuser.machine.authentication.server.signature.model.impl.SignatureKeyPairImpl;

/**
 * Defines data access object for {@link SignatureKeyPairImpl}.
 *
 * @author Anton Korneta
 */
@Beta
public interface SignatureKeyDao {

  /**
   * Creates signature key pair.
   *
   * @param keyPair signature key pair to create
   * @throws ConflictException when signature key pair with given id already exists
   * @throws ServerException when any errors occur while creating signature key pair
   */
  SignatureKeyPairImpl create(SignatureKeyPairImpl keyPair)
      throws ConflictException, ServerException;

  /**
   * Removes signature key pair with given workspace id.
   *
   * @param workspaceId workspace identifier to remove keypair from
   * @throws ServerException when any errors occur while removing signature key pair
   */
  void remove(String workspaceId) throws ServerException;

  /**
   * Returns signature key pair for given workspace id.
   *
   * @param workspaceId identifier of workspace which key pair belongs to
   * @return signature key pair for the given workspace
   * @throws NotFoundException when any errors occur while fetching the key pairs
   */
  SignatureKeyPairImpl get(String workspaceId) throws NotFoundException, ServerException;
}
