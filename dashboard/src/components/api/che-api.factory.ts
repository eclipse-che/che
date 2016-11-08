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

import {CheSsh} from './che-ssh.factory';
'use strict';


/**
 * This class is providing the entry point for accessing to Che API
 * It handles workspaces, projects, etc.
 * @author Florent Benoit
 */
export class CheAPI {


  private cheSsh : CheSsh;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(cheWorkspace, cheProfile, chePreferences, cheProjectTemplate, cheWebsocket, cheService,
              cheAdminPlugins, cheAdminService, cheRecipe, cheRecipeTemplate, cheStack, cheOAuthProvider, cheAgent, cheSsh : CheSsh) {
    this.cheWorkspace = cheWorkspace;
    this.cheProfile = cheProfile;
    this.chePreferences = chePreferences;
    this.cheProjectTemplate = cheProjectTemplate;
    this.cheWebsocket = cheWebsocket;
    this.cheService = cheService;
    this.cheAdminPlugins = cheAdminPlugins;
    this.cheAdminService = cheAdminService;
    this.cheRecipe = cheRecipe;
    this.cheRecipeTemplate = cheRecipeTemplate;
    this.cheStack = cheStack;
    this.cheOAuthProvider = cheOAuthProvider;
    this.cheAgent = cheAgent;
    this.cheSsh = cheSsh;
  }


  /**
   * The Che Workspace API
   * @returns {CheAPI.cheWorkspace|*}
   */
  getWorkspace() {
    return this.cheWorkspace;
  }

  /**
   * The Che oAuth Provider API
   * @returns {CheOAuthProvider|*}
   */
  getOAuthProvider() {
    return this.cheOAuthProvider;
  }

  /**
   * The Che Profile API
   * @returns {CheProfile|*}
   */
  getProfile() {
    return this.cheProfile;
  }

  /**
   * The Che Preferences API
   * @returns {ChePreferences|*}
   */
  getPreferences() {
    return this.chePreferences;
  }

  /**
   * The Che Project Template API
   * @returns {CheProjectTemplate|*}
   */
  getProjectTemplate() {
    return this.cheProjectTemplate;
  }

  /**
   * The Che Websocket API
   * @returns {CheWebsocket|*}
   */
  getWebsocket() {
    return this.cheWebsocket;
  }

  /**
   * The Che Services API
   * @returns {CheService|*}
   */
  getService() {
    return this.cheService;
  }

  /**
   * The Che Admin Services API
   * @returns {CheAdminService|*}
   */
  getAdminService() {
    return this.cheAdminService;
  }


  /**
   * The Che Admin plugins API
   * @returns {CheAdminPlugins|*}
   */
  getAdminPlugins() {
    return this.cheAdminPlugins;
  }

  /**
   * The Che Recipe API
   * @returns {CheRecipe|*}
   */
  getRecipe() {
    return this.cheRecipe;
  }

  /**
   * The Che Recipe Template API
   * @returns {CheRecipeTemplate|*}
   */
  getRecipeTemplate() {
    return this.cheRecipeTemplate;
  }

  /**
   * The Che Stack API
   * @returns {CheAPI.cheStack|*}
   */
  getStack() {
    return this.cheStack;
  }

  /**
   * The Che Agent API
   * @returns {CheAPI.cheAgent|*}
   */
  getAgent() {
    return this.cheAgent;
  }

  /**
   * Gets Che ssh API
   * @returns {CheSsh}
     */
  getSsh() {
    return this.cheSsh;
  }

}
