/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

/**
 * This is class of organization actions.
 *
 * @author Oleksii Orel
 * @author Oleksii Kurinnyi
 */
class CheOrganizationActionsStatic {
  static get UPDATE(): string {
    return 'update';
  }
  static get DELETE(): string {
    return 'delete';
  }
  static get SET_PERMISSIONS(): string {
    return 'setPermissions';
  }
  static get MANAGE_RESOURCES(): string {
    return 'manageResources';
  }
  static get CREATE_WORKSPACES(): string {
    return 'createWorkspaces';
  }
  static get MANAGE_WORKSPACES(): string {
    return 'manageWorkspaces';
  }
  static get MANAGE_SUB_ORGANIZATION(): string {
    return 'manageSuborganizations';
  }
}

export const CheOrganizationActions: che.resource.ICheOrganizationActions = CheOrganizationActionsStatic;
