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
 * @name teams.workspaces:ListTeamWorkspaces
 * @restrict E
 * @element
 *
 * @description
 * `<list-team-workspaces></list-team-workspaces>` for displaying list of workspaces
 *
 * @usage
 *   <list-team-workspaces></list-team-workspaces>
 *
 * @author Ann Shumilova
 */
export class ListTeamWorkspaces implements ng.IDirective {

  restrict: string = 'E';
  templateUrl: string = 'app/teams/team-details/team-workspaces/list-team-workspaces.html';

  controller: string = 'ListTeamWorkspacesController';
  controllerAs: string = 'listTeamWorkspacesController';
  bindToController: boolean = true;

  scope: any = {};
}
