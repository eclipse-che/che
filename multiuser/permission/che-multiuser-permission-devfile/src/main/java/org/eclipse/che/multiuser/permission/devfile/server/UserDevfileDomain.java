/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.devfile.server;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.multiuser.permission.devfile.server.model.impl.UserDevfilePermissionImpl;

/** Domain for storing devfile's permissions */
public class UserDevfileDomain extends AbstractPermissionsDomain<UserDevfilePermissionImpl> {
  public static final String READ = "read";
  public static final String DELETE = "delete";
  public static final String UPDATE = "update";
  public static final String DOMAIN_ID = "devfile";

  public UserDevfileDomain() {
    super(DOMAIN_ID, ImmutableList.of(READ, DELETE, UPDATE));
  }

  @Override
  public UserDevfilePermissionImpl doCreateInstance(
      String userId, String instanceId, List<String> allowedActions) {
    return new UserDevfilePermissionImpl(instanceId, userId, allowedActions);
  }
}
