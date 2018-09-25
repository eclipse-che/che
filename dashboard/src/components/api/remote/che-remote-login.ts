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
 * This class is handling the call to remote API to login
 * @author Florent Benoit
 */
export class CheRemoteLogin {
  $resource: ng.resource.IResourceService;
  url: string;
  remoteAuthAPI: any;

  /**
   * Default constructor using angular $resource and remote URL
   * @param $resource the angular $resource object
   * @param url the remote server URL
   */
  constructor($resource: ng.resource.IResourceService,
              url: string) {
    this.$resource = $resource;
    this.url = url;
    this.remoteAuthAPI = this.$resource('', {}, {
      auth: {method: 'POST', url: url + '/api/auth/login'}
    });
  }

  /**
   * Authenticate on the remote URL the provided username/password
   * @param login the login on remote URL
   * @param password the password on remote URL
   * @returns {ng.IPromise<any>}
   */
  authenticate(login: string, password: string): ng.IPromise<any> {
    let authData = {username: login, password: password};
    let call = this.remoteAuthAPI.auth(authData);
    let promise = call.$promise;
    return promise.then((result: any) => {
      let token = result.value;
      return {'url': this.url, 'token': token};
    });
  }

}
