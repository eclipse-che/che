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

interface IInviteResource<T> extends ng.resource.IResourceClass<T> {
  invite: any;
  getInvited: any;
  delete: any;
}

/**
 * This class is handling the invitation API.
 *
 * @author Ann Shumilova
 */
export class CheInvite implements che.api.ICheInvite {

  static $inject = ['$q', '$resource'];

  /**
   * Angular promise service.
   */
  private $q: ng.IQService;

  /**
   * Angular Resource service.
   */
  private $resource: ng.resource.IResourceService;
  /**
   * Team invitations with team's id as a key.
   */
  private teamInvitations: Map<string, any>;
  /**
   * Client to make remote invitation API calls.
   */
  private remoteInviteAPI: IInviteResource<any>;

  /**
   * Default constructor that is using resource
   */
  constructor($q: ng.IQService, $resource: ng.resource.IResourceService) {
    this.$q = $q;
    this.$resource = $resource;

    this.teamInvitations = new Map();

    this.remoteInviteAPI = <IInviteResource<any>>this.$resource('/api/invite', {}, {
      invite: {method: 'POST', url: '/api/invite'},
      getInvited: {method: 'GET', url: '/api/invite/:domain?instance=:instance', isArray: true},
      remove: {method: 'DELETE', url: '/api/invite/:domain?instance=:instance&email=:email'}
    });
  }

  /**
   * Invite non existing user to the team.
   *
   * @param {string} teamId id of the team to invite to
   * @param {string} email user's email to send invite
   * @param {string[]} actions actions to be granted
   * @return {angular.IPromise<any>}
   */
  inviteToTeam(teamId: string, email: string, actions: Array<string>): ng.IPromise<any> {
    let promise = this.remoteInviteAPI.invite({domainId: 'organization', instanceId: teamId, email: email, actions: actions}).$promise;
    return promise;
  }

  /**
   * Fetches the list of team invitations.
   *
   * @param {string} teamId id of the team to fetch invites
   * @return {angular.IPromise<any>}
   */
  fetchTeamInvitations(teamId: string): ng.IPromise<any> {
    let deferred = this.$q.defer();
    let promise = this.remoteInviteAPI.getInvited({domain: 'organization', instance: teamId}).$promise;
    promise.then((data: any) => {
      this.teamInvitations.set(teamId, data);
      deferred.resolve(data);
    }, (error: any) => {
      if (error.status === 304) {
        deferred.resolve(this.teamInvitations.get(teamId));
      } else {
        deferred.reject(error);
      }
    });
    return deferred.promise;
  }

  /**
   * Returns team's invitations by team's id.
   *
   * @param {string} teamId id of the team
   * @return {Array<any>}
   */
  getTeamInvitations(teamId: string): Array<any> {
    return this.teamInvitations.get(teamId);
  }

  /**
   * Deletes team invitation team's id and user's email.
   *
   * @param {string} teamId id of the team
   * @param {string} email user email to delete invitation
   * @return {angular.IPromise<any>}
   */
  deleteTeamInvitation(teamId: string, email: string): void {
    return this.remoteInviteAPI.remove({domain: 'organization', instance: teamId, email: email}).$promise;
  }
}
