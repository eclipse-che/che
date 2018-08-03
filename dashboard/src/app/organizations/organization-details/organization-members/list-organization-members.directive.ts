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

/**
 * @ngdoc directive
 * @name organization.details.members:ListOrganizationMembers
 * @restrict E
 * @element
 *
 * @description
 * `<list-organization-members editable="ctrl.editable" organization="ctrl.organization"></list-organization-members>` for displaying list of members
 *
 * @usage
 *   <list-organization-members editable="ctrl.editable" organization="ctrl.organization"></list-organization-members>
 *
 * @author Oleksii Orel
 */
export class ListOrganizationMembers implements ng.IDirective {

  restrict: string = 'E';
  templateUrl: string = 'app/organizations/organization-details/organization-members/list-organization-members.html';
  controller: string = 'ListOrganizationMembersController';
  controllerAs: string = 'listOrganizationMembersController';
  bindToController: boolean = true;

  scope: any = {
    editable: '=',
    organization: '=',
    parentOrganizationMembers: '='
  };
}
