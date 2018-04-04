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
 * @ngdoc directive
 * @name organization.details.invite-members:ListOrganizationInviteMembers
 * @restrict E
 * @element
 *
 * @description
 * `<list-organization-members members="ctrl.members"></list-organization-members>` for displaying list of members
 *
 * @usage
 *   <list-organization-members members="ctrl.members"></list-organization-members>
 *
 * @author Oleksii Orel
 */
export class ListOrganizationInviteMembers implements ng.IDirective {

  restrict: string = 'E';
  templateUrl: string = 'app/organizations/organization-details/organization-invite-members/list-organization-invite-members.html';

  controller: string = 'ListOrganizationInviteMembersController';
  controllerAs: string = 'listOrganizationInviteMembersController';
  bindToController: boolean = true;

  scope: any = {
    members: '=',
    parentOrganizationId: '=',
    parentOrganizationMembers: '='
  };
}
