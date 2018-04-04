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
package org.eclipse.che.multiuser.permission.workspace.server.stack;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain;

/**
 * Domain for storing stacks' permissions
 *
 * @author Sergii Leschenko
 * @author Yevhenii Voevodin
 */
public class StackDomain extends AbstractPermissionsDomain<StackPermissionsImpl> {
  public static final String DOMAIN_ID = "stack";

  public static final String READ = "read";
  public static final String SEARCH = "search";
  public static final String UPDATE = "update";
  public static final String DELETE = "delete";

  private static final List<String> ACTIONS =
      ImmutableList.of(SET_PERMISSIONS, READ, SEARCH, UPDATE, DELETE);

  /** Returns all the available actions for {@link StackDomain}. */
  public static List<String> getActions() {
    return ACTIONS;
  }

  public StackDomain() {
    super(DOMAIN_ID, ACTIONS);
  }

  @Override
  public StackPermissionsImpl doCreateInstance(
      String userId, String instanceId, List<String> allowedActions) {
    return new StackPermissionsImpl(userId, instanceId, allowedActions);
  }
}
