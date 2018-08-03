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
 * @name teams.directive:NavbarTeams
 * @description This class is handling the directive of for listing teams in navbar.
 * @author Ann Shumilova
 */
export class NavbarTeams implements ng.IDirective {

  restrict: string = 'E';
  templateUrl: string = 'app/teams/navbar-teams/navbar-teams.html';

  controller: string = 'NavbarTeamsController';
  controllerAs: string = 'navbarTeamsController';
  bindToController: boolean = true;

}
