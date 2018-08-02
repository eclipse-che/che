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
package org.eclipse.che.selenium.core.entrance;

import org.eclipse.che.selenium.core.user.TestUser;

/** @author Dmytro Nochevnov */
public interface Entrance {
  /**
   * Login to product.
   *
   * @param user user to login
   */
  void login(TestUser user);
}
