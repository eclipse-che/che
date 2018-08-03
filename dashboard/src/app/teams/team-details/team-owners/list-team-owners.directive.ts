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
 * @name teams.owners:ListTeamMembers
 * @restrict E
 * @element
 *
 * @description
 * `<list-team-owners></list-team-owners>` for displaying list of owners
 *
 * @usage
 *   <list-team-owners></list-team-owners>
 *
 * @author Ann Shumilova
 */
export class ListTeamOwners implements ng.IDirective {

  restrict: string = 'E';
  templateUrl: string = 'app/teams/team-details/team-owners/list-team-owners.html';

  controller: string = 'ListTeamOwnersController';
  controllerAs: string = 'listTeamOwnersController';
  bindToController: boolean = true;

  scope: any = {};
}
