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

import {NavbarTeamsController} from './navbar-teams/navbar-teams.controller';
import {NavbarTeams} from './navbar-teams/navbar-teams.directive';

import {CreateTeamController} from './create-team/create-team.controller';
import {MemberDialogController} from './member-dialog/member-dialog.controller';
// import {MoreDevsDialogController} from './more-devs-dialog/more-devs-dialog.controller';
import {ListMembersController} from './invite-members/list-members.controller';
import {ListMembers} from './invite-members/list-members.directive';

import {ListTeamWorkspaces} from './team-details/team-workspaces/list-team-workspaces.directive';
import {ListTeamWorkspacesController} from './team-details/team-workspaces/list-team-workspaces.controller';

import {ListTeamMembers} from './team-details/team-members/list-team-members.directive';
import {ListTeamMembersController} from './team-details/team-members/list-team-members.controller';
import {ListTeamOwners} from './team-details/team-owners/list-team-owners.directive';
import {ListTeamOwnersController} from './team-details/team-owners/list-team-owners.controller';
import {MemberItem} from './team-details/team-members/member-item/member-item.directive';
import {MemberItemController} from './team-details/team-members/member-item/member-item.controller';

import {TeamDetailsController} from './team-details/team-details.controller';
import {TeamDetailsService} from './team-details/team-details.service';
import {ListTeams} from './list/list-teams.directive';
import {ListTeamsController} from './list/list-teams.controller';
import {TeamItem} from './list/team-item/team-item.directive';
import {TeamItemController} from './list/team-item/team-item.controller';

/**
 * The configuration of teams, defines controllers, directives and routing.
 *
 * @author Ann Shumilova
 */
export class TeamsConfig {

  constructor(register: che.IRegisterService) {
    register.controller('NavbarTeamsController', NavbarTeamsController);
    register.directive('navbarTeams', NavbarTeams);
    register.controller('CreateTeamController', CreateTeamController);

    register.controller('ListTeamsController', ListTeamsController);
    register.directive('listTeams', ListTeams);

    register.controller('TeamItemController', TeamItemController);
    register.directive('teamItem', TeamItem);

    register.controller('MemberDialogController', MemberDialogController);
    // register.controller('MoreDevsDialogController', MoreDevsDialogController);
    register.controller('ListMembersController', ListMembersController);
    register.directive('listMembers', ListMembers);

    register.controller('ListTeamWorkspacesController', ListTeamWorkspacesController);
    register.directive('listTeamWorkspaces', ListTeamWorkspaces);

    register.controller('ListTeamMembersController', ListTeamMembersController);
    register.directive('listTeamMembers', ListTeamMembers);

    register.controller('ListTeamOwnersController', ListTeamOwnersController);
    register.directive('listTeamOwners', ListTeamOwners);

    register.controller('MemberItemController', MemberItemController);
    register.directive('memberItem', MemberItem);

    register.controller('TeamDetailsController', TeamDetailsController);
    register.service('teamDetailsService', TeamDetailsService);

    let checkPersonalTeam = ($q: ng.IQService, cheTeam: che.api.ICheTeam) => {
      var defer = $q.defer();
      cheTeam.fetchTeams().then(() => {
        if (cheTeam.getPersonalAccount()) {
          defer.resolve();
        } else {
          defer.reject();
        }
      }, (error: any) => {
        if (error.status === 304) {
          if (cheTeam.getPersonalAccount()) {
            defer.resolve();
          } else {
            defer.reject();
          }
        }
      });
      return defer.promise;
    };

    let checkTeamDetails = ($q: ng.IQService, teamDetailsService: TeamDetailsService, $route: ng.route.IRouteService) => {
      let defer = $q.defer();
      let teamName = $route.current.params.teamName;
      teamDetailsService.fetchTeamDetailsByName(teamName).then(() => {
        teamDetailsService.fetchOwnerByTeamName(teamName).finally(() => {
          defer.resolve();
        });
      }, (error: any) => {
          // resolve it to show 'team not found page' in case with error
          defer.resolve();
      });

      return defer.promise;
    };

    let locationProvider = {
      title: (params: any) => {
        return params.teamName;
      },
      reloadOnSearch: false,
      templateUrl: 'app/teams/team-details/team-details.html',
      controller: 'TeamDetailsController',
      controllerAs: 'teamDetailsController',
      resolve: {
        checkPersonal: ['$q', 'cheTeam', checkPersonalTeam],
        checkTeamDetails: ['$q', 'teamDetailsService', '$route', checkTeamDetails]
      }
    };

    // config routes
    register.app.config(['$routeProvider', ($routeProvider: che.route.IRouteProvider) => {
      $routeProvider.accessWhen('/team/create', {
        title: 'New Team',
        templateUrl: 'app/teams/create-team/create-team.html',
        controller: 'CreateTeamController',
        controllerAs: 'createTeamController',
        resolve: {
          check: ['$q', 'cheTeam', checkPersonalTeam]
        }
      }).accessWhen('/team/:teamName*', locationProvider);
    }]);
  }
}
