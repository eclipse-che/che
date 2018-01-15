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
