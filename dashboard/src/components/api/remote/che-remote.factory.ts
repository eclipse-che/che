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

import {CheRemoteLogin} from './che-remote-login';
import {CheRemoteRecipe} from './che-remote-recipe';
import {CheRemoteWorkspace} from './che-remote-workspace';
import {CheRemoteProject} from './che-remote-project';
import {CheJsonRpcApi} from '../json-rpc/che-json-rpc-api.factory';

/**
 * This class is handling the call to remote API
 * @author Florent Benoit
 */
export class CheRemote {

  static $inject = ['$resource', '$q', 'cheJsonRpcApi'];

  private $resource: ng.resource.IResourceService;
  private $q: ng.IQService;
  private cheJsonRpcApi: CheJsonRpcApi;

  /**
   * Default constructor that is using resource
   */
  constructor($resource: ng.resource.IResourceService, $q: ng.IQService, cheJsonRpcApi: CheJsonRpcApi) {
    this.$resource = $resource;
    this.$q = $q;
    this.cheJsonRpcApi = cheJsonRpcApi;
  }

  /**
   * Build a new remote authenticator
   * @param url the URL of the remote server
   * @param login the login on the remote server
   * @param password the password on the remote server
   * @returns {*|promise|N|n}
   */
  newAuth(url: string, login: string, password: string): ng.IPromise<any> {
    let remoteLogin = new CheRemoteLogin(this.$resource, url);
    return remoteLogin.authenticate(login, password);
  }

  /**
   * Build a new remote workspace handler
   * @param url the URL
   * @param token
   * @returns {*}
   */
  newWorkspace(remoteConfig: any): CheRemoteWorkspace {
    return new CheRemoteWorkspace(this.$resource, this.$q, this.cheJsonRpcApi, remoteConfig);
  }

  /**
   * Build a new remote workspace handler
   * @param url the URL
   * @param token
   * @returns {*}
   */
  newProject(remoteConfig: any): CheRemoteProject {
    return new CheRemoteProject(this.$resource, remoteConfig);
  }

  /**
   * Build a new remote recipe handler
   * @param url the URL
   * @param token
   * @returns {*}
   */
  newRecipe(remoteConfig: any): CheRemoteRecipe {
    return new CheRemoteRecipe(this.$resource, remoteConfig);
  }
}
