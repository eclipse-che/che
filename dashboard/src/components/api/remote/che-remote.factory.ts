/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

import {CheRemoteLogin} from './che-remote-login';
import {CheRemoteRecipe} from './che-remote-recipe';
import {CheRemoteWorkspace} from './che-remote-workspace';
import {CheRemoteProject} from './che-remote-project';

/**
 * This class is handling the call to remote API
 * @author Florent Benoit
 */
export class CheRemote {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($resource, $q, cheWebsocket) {
    this.$resource = $resource;
    this.$q = $q;
    this.cheWebsocket = cheWebsocket;
  }

  /**
   * Build a new remote authenticator
   * @param url the URL of the remote server
   * @param login the login on the remote server
   * @param password the password on the remote server
   * @returns {*|promise|N|n}
   */
  newAuth(url, login, password) {
    let remoteLogin = new CheRemoteLogin(this.$resource, url);
    return remoteLogin.authenticate(login, password);
  }

  /**
   * Build a new remote workspace handler
   * @param url the URL
   * @param token
   * @returns {*}
   */
  newWorkspace(remoteConfig) {
    return new CheRemoteWorkspace(this.$resource, this.$q, this.cheWebsocket, remoteConfig);
  }

  /**
   * Build a new remote workspace handler
   * @param url the URL
   * @param token
   * @returns {*}
   */
  newProject(remoteConfig) {
    return new CheRemoteProject(this.$resource, remoteConfig);
  }

  /**
   * Build a new remote recipe handler
   * @param url the URL
   * @param token
   * @returns {*}
   */
  newRecipe(remoteConfig) {
    return new CheRemoteRecipe(this.$resource, remoteConfig)
  }
}
