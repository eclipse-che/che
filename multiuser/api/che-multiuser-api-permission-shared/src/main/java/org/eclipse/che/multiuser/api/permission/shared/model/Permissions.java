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
package org.eclipse.che.multiuser.api.permission.shared.model;

import java.util.List;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Represents users' permissions to access to some resources
 *
 * @author Sergii Leschenko
 */
public interface Permissions {
  /**
   * Returns user id
   *
   * <p>Note: also supported '*' for marking all users
   */
  String getUserId();

  /** Returns domain id */
  String getDomainId();

  /**
   * Returns instance id. It is optional and can be null if domain supports it
   *
   * @see PermissionsDomain#isInstanceRequired()
   */
  @Nullable
  String getInstanceId();

  /** List of actions which user can perform for particular instance */
  List<String> getActions();
}
