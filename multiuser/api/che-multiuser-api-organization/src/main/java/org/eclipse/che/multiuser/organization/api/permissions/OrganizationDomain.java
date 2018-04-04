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
package org.eclipse.che.multiuser.organization.api.permissions;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.multiuser.organization.spi.impl.MemberImpl;

/**
 * Domain for storing organizations' permissions
 *
 * @author Sergii Leschenko
 */
public class OrganizationDomain extends AbstractPermissionsDomain<MemberImpl> {
  public static final String DOMAIN_ID = "organization";

  public static final String UPDATE = "update";
  public static final String DELETE = "delete";
  public static final String MANAGE_SUBORGANIZATIONS = "manageSuborganizations";
  public static final String MANAGE_RESOURCES = "manageResources";
  public static final String CREATE_WORKSPACES = "createWorkspaces";
  public static final String MANAGE_WORKSPACES = "manageWorkspaces";

  private static final List<String> ACTIONS =
      ImmutableList.of(
          SET_PERMISSIONS,
          UPDATE,
          DELETE,
          MANAGE_SUBORGANIZATIONS,
          MANAGE_RESOURCES,
          CREATE_WORKSPACES,
          MANAGE_WORKSPACES);

  /** Returns all the available actions for {@link OrganizationDomain}. */
  public static List<String> getActions() {
    return ACTIONS;
  }

  public OrganizationDomain() {
    super(DOMAIN_ID, ACTIONS);
  }

  @Override
  protected MemberImpl doCreateInstance(
      String userId, String instanceId, List<String> allowedActions) {
    return new MemberImpl(userId, instanceId, allowedActions);
  }
}
