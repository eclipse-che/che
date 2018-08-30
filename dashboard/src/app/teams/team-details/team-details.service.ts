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
 * This class is fetch and handling the data for team details
 *
 * @author Oleksii Orel
 */
export class TeamDetailsService {

  static $inject = ['$q', 'cheUser', 'cheTeam', '$route'];

  /**
   * Promises service.
   */
  private $q: ng.IQService;
  /**
   * Team API interaction.
   */
  private cheTeam: che.api.ICheTeam;
  /**
   * User API interaction.
   */
  private cheUser: any;
  /**
   * Route service.
   */
  private $route: ng.route.IRouteService;
  /**
   * Current team (comes from directive's scope).
   */
  private team: any;
  /**
   * Current team's owner (comes from directive's scope).
   */
  private owner: any;

  /**
   */
  constructor($q: ng.IQService, cheUser: any, cheTeam: che.api.ICheTeam, $route: ng.route.IRouteService) {
    this.$q = $q;
    this.cheTeam = cheTeam;
    this.cheUser = cheUser;
    this.$route = $route;
  }

  /**
   * Fetches the team's details by it's name.
   * @param teamName {string}
   *
   * @return {ng.IPromise<any>}
   */
  fetchTeamDetailsByName(teamName: string): ng.IPromise<any> {
    if (!teamName) {
      return;
    }
    let deferred = this.$q.defer();
    this.cheTeam.fetchTeamByName(teamName).then((team: any) => {
      this.team = team;
      deferred.resolve(team);
    }, (error: any) => {
      this.team = null;
      deferred.reject(error);
    });

    return deferred.promise;
  }

  /**
   * Fetches the team's owner by team's name.
   * @param teamName {string}
   *
   * @return {ng.IPromise<any>}
   */
  fetchOwnerByTeamName(teamName: string): ng.IPromise<any> {
    let deferred = this.$q.defer();
    let parts = teamName.split('/');
    let accountName = (parts && parts.length > 0) ? parts[0] : '';

    this.cheUser.fetchUserByName(accountName).then((owner: any) => {
      this.owner = owner;
      deferred.resolve(owner);
    }, (error: any) => {
      this.owner = null;
      deferred.reject(error);
    });

    return deferred.promise;
  }

  /**
   * Gets the team.
   *
   * @return {any}
   */
  getTeam(): any {
    return this.team;
  }

  /**
   * Gets the owner.
   *
   * @return {any}
   */
  getOwner(): any {
    return this.owner;
  }
}
