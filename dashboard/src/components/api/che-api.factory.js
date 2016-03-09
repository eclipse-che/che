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
 * This class is providing the entry point for accessing to Che API
 * It handles workspaces, projects, etc.
 * @author Florent Benoit
 */
export class CheAPI {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(cheProject, cheWorkspace, cheUser, cheProfile, cheProjectType, cheProjectTemplate, cheWebsocket, cheGit, cheSvn,
              cheService, cheAdminPlugins, cheAdminService, cheRecipe, cheRecipeTemplate, cheStack, cheOAuthProvider) {
    this.cheProject = cheProject;
    this.cheWorkspace = cheWorkspace;
    this.cheUser = cheUser;
    this.cheProfile = cheProfile;
    this.cheProjectType = cheProjectType;
    this.cheProjectTemplate = cheProjectTemplate;
    this.cheWebsocket = cheWebsocket;
    this.cheGit = cheGit;
    this.cheSvn = cheSvn;
    this.cheService = cheService;
    this.cheAdminPlugins = cheAdminPlugins;
    this.cheAdminService = cheAdminService;
    this.cheRecipe = cheRecipe;
    this.cheRecipeTemplate = cheRecipeTemplate;
    this.cheStack = cheStack;
    this.cheOAuthProvider = cheOAuthProvider;

    // register listener of projects onto workspaces
    this.cheWorkspace.addListener(this.cheProject);

  }


  /**
   * The Che Project API
   * @returns {CheAPI.cheProject|*}
   */
  getProject() {
    return this.cheProject;
  }

  /**
   * The Che Workspace API
   * @returns {CheAPI.cheWorkspace|*}
   */
  getWorkspace() {
    return this.cheWorkspace;
  }

  /**
   * The Che User API
   * @returns {CheUser|*}
   */
  getUser() {
    return this.cheUser;
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
   * The Che Project Type API
   * @returns {CheProjectType|*}
   */
  getProjectType() {
    return this.cheProjectType;
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
   * The Che Git API
   * @returns {CheGit|*}
   */
  getGit() {
    return this.cheGit;
  }

  /**
   * The Che Svn API
   * @returns {CheSvn|*}
   */
  getSvn() {
    return this.cheSvn;
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
}
