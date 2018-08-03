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
 * @name teams.members:ListTeamMembers
 * @restrict E
 * @element
 *
 * @description
 * `<list-team-members editable="ctrl.editable"></list-team-members>` for displaying list of members
 *
 * @usage
 *   <list-team-members editable="ctrl.editable"></list-team-members>
 *
 * @author Ann Shumilova
 */
export class ListTeamMembers implements ng.IDirective {

  restrict: string = 'E';
  templateUrl: string = 'app/teams/team-details/team-members/list-team-members.html';

  controller: string = 'ListTeamMembersController';
  controllerAs: string = 'listTeamMembersController';
  bindToController: boolean = true;

  scope: any = {
    editable: '='
  };
}
