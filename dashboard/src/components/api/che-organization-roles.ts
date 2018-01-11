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
import {CheOrganizationActions} from './che-organization-actions';

/**
 * This is class of member's roles in organization.
 *
 * @author Oleksii Orel
 */
class CheOrganizationRolesStatic {

  static get MEMBER(): che.IRole {
    return {
      'name': 'MEMBER',
      'title': 'Member',
      'description': 'Can create workspaces in organization and use resources.',
      'actions': [CheOrganizationActions.CREATE_WORKSPACES]
    };
  }

  static get ADMIN(): che.IRole {
    return {
      'name': 'ADMIN',
      'title': 'Admin',
      'description': 'Can edit the organization’s settings, manage members and sub-organizations.',
      'actions': [
        CheOrganizationActions.UPDATE,
        CheOrganizationActions.SET_PERMISSIONS,
        CheOrganizationActions.MANAGE_RESOURCES,
        CheOrganizationActions.MANAGE_WORKSPACES,
        CheOrganizationActions.CREATE_WORKSPACES,
        CheOrganizationActions.DELETE,
        CheOrganizationActions.MANAGE_SUB_ORGANIZATION]
    };
  }

  static getRoles(): Array<string> {
    return [
      CheOrganizationRolesStatic.MEMBER.name,
      CheOrganizationRolesStatic.ADMIN.name
    ];
  }

  static getValues(): Array<che.IRole> {
    return [
      CheOrganizationRolesStatic.MEMBER,
      CheOrganizationRolesStatic.ADMIN
    ];
  }

}

export const CheOrganizationRoles: che.resource.ICheOrganizationRoles = CheOrganizationRolesStatic;
