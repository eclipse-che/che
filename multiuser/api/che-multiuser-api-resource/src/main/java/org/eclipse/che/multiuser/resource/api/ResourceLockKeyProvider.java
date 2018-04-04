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
package org.eclipse.che.multiuser.resource.api;

import org.eclipse.che.api.core.ServerException;

/**
 * Returns key for fetching lock which will be used for locking resources during resources
 * operations for account with some type.
 *
 * @author Sergii Leschenko
 */
public interface ResourceLockKeyProvider {
  /**
   * Returns lock key by which resources should be lock during resources operations
   *
   * @param accountId account id
   * @return lock key by which resources should be lock during resources operations
   * @throws ServerException when any other exception occurs
   */
  String getLockKey(String accountId) throws ServerException;

  /** Returns account type for which this class provides locks' ids */
  String getAccountType();
}
