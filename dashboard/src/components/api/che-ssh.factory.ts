/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

/**
 * This class is handling the ssh keys
 * @author Florent Benoit
 */
export class CheSsh {

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
  private remoteSshAPI: ng.resource.IResourceClass<ng.resource.IResource<any>>;
  remoteSshAPI: { getKeyPair: Function; removeKey : Function, generateKey: Function};

  private sshKeyPairs : Map<string, any>;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($resource : ng.resource.IResourceService, $q : ng.IQService) {

    // keep resource
    this.$resource = $resource;
    this.$q = $q;


    this.sshKeyPairs = new Map<string, any>();

    // remote call
    this.remoteSshAPI = this.$resource('/api/ssh', {}, {
      getKeyPair: { method: 'GET', url: '/api/ssh/:serviceId/:nameId'},
      removeKey: { method: 'DELETE', url: '/api/ssh/:serviceId/:nameId'},
      generateKey: { method: 'POST', url: '/api/ssh/generate'},
    });
  }

  /**
   * Fetch the keyPair
   */
  fetchKey(serviceId: string, nameId: string) {
    var defer = this.$q.defer();
    let promise = this.remoteSshAPI.getKeyPair({serviceId: serviceId, nameId: nameId}).$promise;

    promise.then((sshKeyPair) => {
      this.sshKeyPairs.set(serviceId + '/' + nameId, sshKeyPair);
      defer.resolve();
    }, (error) => {
      if (error.status != 304) {
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
   * @returns
   */
  getKey(serviceId: string, nameId: string) {
    return this.sshKeyPairs.get(serviceId + '/' + nameId);
  }

  removeKey(serviceId: string, nameId: string) {
    return this.remoteSshAPI.removeKey({serviceId: serviceId, nameId: nameId}).$promise;
  }

  generateKey(serviceId: string, nameId: string) {
    return this.remoteSshAPI.generateKey({}, {service: serviceId, name: nameId}).$promise;
  }

}
