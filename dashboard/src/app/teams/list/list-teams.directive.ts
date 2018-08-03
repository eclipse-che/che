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
 * @name list.teams:ListTeams
 * @restrict E
 * @element
 *
 * @description
 * `<list-teams></list-teams>` for displaying list of teams
 *
 * @usage
 *   <list-teams></list-teams>
 *
 * @author Ann Shumilova
 */
export class ListTeams implements ng.IDirective {

  restrict: string = 'E';
  templateUrl: string = 'app/teams/list/list-teams.html';

  controller: string = 'ListTeamsController';
  controllerAs: string = 'listTeamsController';
  bindToController: boolean = true;

  scope: any = {
    accountId: '=',
    readonly: '='
  };

  constructor () {
    //
  }
}
