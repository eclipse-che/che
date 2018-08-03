/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {CheOrganizationActions} from './che-organization-actions';

/**
 * This is enum of team roles.
 *
 * @author Ann Shumilova
 * @author Oleksii Kurinnyi
 */
class CheTeamRolesStatic {

  static get TEAM_MEMBER(): any {
    return {
      'title': 'Team Developer',
      'description': 'Can create and use own workspaces.',
      'actions': [CheOrganizationActions.CREATE_WORKSPACES]
    };
  }

  static get TEAM_ADMIN(): any {
    return {
      'title': 'Team Admin', 'description': 'Can edit the team’s settings, manage workspaces and members.',
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

  static getValues(): any[] {
    return [
      CheTeamRolesStatic.TEAM_MEMBER,
      CheTeamRolesStatic.TEAM_ADMIN
    ];
  }

}

export const CheTeamRoles: che.resource.ICheTeamRoles = CheTeamRolesStatic;
