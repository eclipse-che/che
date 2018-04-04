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
