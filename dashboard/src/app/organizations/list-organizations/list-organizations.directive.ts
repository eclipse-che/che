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
 * @name organizations.organizations:ListOrganizations
 * @restrict E
 * @element
 *
 * @description
 * `<list-organizations organizations="ctrl.organizations"></list-organizations>` for displaying list of organizations
 *
 * @usage
 *   <list-organizations organizations="ctrl.organizations"></list-organizations>
 *
 * @author Oleksii Orel
 */
export class ListOrganizations implements ng.IDirective {

  restrict: string = 'E';
  templateUrl: string = 'app/organizations/list-organizations/list-organizations.html';

  controller: string = 'ListOrganizationsController';
  controllerAs: string = 'listOrganizationsController';
  bindToController: boolean = true;

  scope: any = {
    isLoading: '=?',
    organizations: '=',
    hideAddButton: '=?',
    onUpdate: '&?onUpdate'
  };
}
