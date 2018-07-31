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
package org.eclipse.che.multiuser.machine.authentication.server.signature.spi;

import com.google.common.annotations.Beta;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.Page;
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
   * Removes signature key pair with given id.
   *
   * @param id signature key identifier
   * @throws ServerException when any errors occur while removing signature key pair
   */
  void remove(String id) throws ServerException;

  /**
   * Returns all the signature key pairs.
   *
   * @param skipCount the number of signature key pairs to skip
   * @param maxItems the maximum number of signature key pairs to return
   * @return list of signature key pairs or an empty list when no keys were found
   * @throws ServerException when any errors occur while fetching the key pairs
   * @throws IllegalArgumentException when {@code maxItems} or {@code skipCount} is negative
   */
  Page<SignatureKeyPairImpl> getAll(int maxItems, long skipCount) throws ServerException;
}
