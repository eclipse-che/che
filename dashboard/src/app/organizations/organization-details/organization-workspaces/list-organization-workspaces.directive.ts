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
 * @name organizations.workspaces:ListOrganizationWorkspaces
 * @restrict E
 * @element
 *
 * @description
 * `<list-organization-workspaces></list-organization-workspaces>` for displaying list of workspaces
 *
 * @usage
 *   <list-organization-workspaces></list-organization-workspaces>
 *
 * @author Oleksii Orel
 */
export class ListOrganizationWorkspaces implements ng.IDirective {

  restrict: string = 'E';
  templateUrl: string = 'app/organizations/organization-details/organization-workspaces/list-organization-workspaces.html';

  controller: string = 'ListOrganizationWorkspacesController';
  controllerAs: string = 'listOrganizationWorkspacesController';
  bindToController: boolean = true;

  scope: {
    [propName: string]: string;
  } = {
    organization: '='
  };
}
