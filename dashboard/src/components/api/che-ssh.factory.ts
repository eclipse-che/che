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
 * This class is handling the ssh keys
 * @author Florent Benoit
 */
export class CheSsh {

  static $inject = ['$resource', '$q'];

  /**
   * Angular Resource service.
   */
  private $resource: ng.resource.IResourceService;

  /**
   * Angular Promise service.
   */
  private $q: ng.IQService;

  /**
   * Remote API for SSH.
   */
  private remoteSshAPI: any;

  private sshKeyPairs : Map<string, any>;

  /**
   * Default constructor that is using resource
   */
  constructor($resource : ng.resource.IResourceService, $q : ng.IQService) {

    // keep resource
    this.$resource = $resource;
    this.$q = $q;

    this.sshKeyPairs = new Map<string, any>();

    // remote call
    this.remoteSshAPI = this.$resource('/api/ssh', {}, {
      getKeyPair: { method: 'GET', url: '/api/ssh/:serviceId/find?name=:nameId'},
      removeKey: { method: 'DELETE', url: '/api/ssh/:serviceId/?name=:nameId'},
      generateKey: { method: 'POST', url: '/api/ssh/generate'}
    });
  }

  /**
   * Fetch the keyPair
   */
  fetchKey(serviceId: string, nameId: string): ng.IPromise<any> {
    const defer = this.$q.defer();
    let promise = this.remoteSshAPI.getKeyPair({serviceId: serviceId, nameId: nameId}).$promise;

    promise.then((sshKeyPair: any) => {
      this.sshKeyPairs.set(serviceId + '/' + nameId, sshKeyPair);
      defer.resolve();
    }, (error: any) => {
      if (error.status !== 304) {
        this.sshKeyPairs.delete(serviceId + '/' + nameId);
        defer.reject(error);
      } else {
        defer.resolve();
      }
    });

    return defer.promise;
  }

  /**
   * Get ssh keypair
   *
   * @param {string} serviceId
   * @param {string} nameId
   * @returns {angular.IPromise<any>}
   */
  getKey(serviceId: string, nameId: string): ng.IPromise<any> {
    return this.sshKeyPairs.get(serviceId + '/' + nameId);
  }

  /**
   * @param {string} serviceId
   * @param {string} nameId
   * @returns {angular.IPromise<any>}
   */
  removeKey(serviceId: string, nameId: string): ng.IPromise<any> {
    return this.remoteSshAPI.removeKey({serviceId: serviceId, nameId: nameId}).$promise;
  }

  /**
   * @param {string} serviceId
   * @param {string} nameId
   * @returns {angular.IPromise<any>}
   */
  generateKey(serviceId: string, nameId: string): ng.IPromise<any> {
    return this.remoteSshAPI.generateKey({}, {service: serviceId, name: nameId}).$promise;
  }

}
