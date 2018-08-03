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
 * @name teams.invite.members:ListMembers
 * @restrict E
 * @element
 *
 * @description
 * `<list-members members="ctrl.members"></list-members>` for displaying list of members
 *
 * @usage
 *   <list-members members="ctrl.members"></list-members>
 *
 * @author Ann Shumilova
 */
export class ListMembers implements ng.IDirective {

  restrict: string = 'E';
  templateUrl: string = 'app/teams/invite-members/list-members.html';

  controller: string = 'ListMembersController';
  controllerAs: string = 'listMembersController';
  bindToController: boolean = true;

  scope: any = {
    members: '=',
    owner: '='
  };

}
