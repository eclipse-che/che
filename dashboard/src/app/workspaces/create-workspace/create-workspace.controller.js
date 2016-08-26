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
 * @ngdoc controller
 * @name workspaces.create.workspace.controller:CreateWorkspaceController
 * @description This class is handling the controller for workspace creation
 * @author Ann Shumilova
 * @author Oleksii Orel
 */
export class CreateWorkspaceController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($location, cheAPI, cheNotification, lodash, $rootScope) {
    this.$location = $location;
    this.cheAPI = cheAPI;
    this.cheNotification = cheNotification;
    this.lodash = lodash;
    this.$rootScope = $rootScope;

    this.selectSourceOption = 'select-source-recipe';

    this.stack = {};
    this.workspace = {};

    // default RAM value for workspaces
    this.workspaceRam = 1000;

    this.editorOptions = {
      lineWrapping: true,
      lineNumbers: false,
      matchBrackets: true,
      mode: 'application/json'
    };

    // fetch default recipe if we haven't one
    if (!cheAPI.getRecipeTemplate().getDefaultRecipe()) {
      cheAPI.getRecipeTemplate().fetchDefaultRecipe();
    }

    this.stack = null;
    this.recipeUrl = null;
    this.recipeScript = null;
    this.importWorkspace = '';
    this.defaultWorkspaceName = null;

    cheAPI.cheWorkspace.fetchWorkspaces();

    $rootScope.showIDE = false;
  }

  /**
   * Callback when tab has been change
   * @param tabName  the select tab name
   */
  setStackTab(tabName) {
    if (tabName === 'custom-stack') {
      this.isCustomStack = true;
      this.generateWorkspaceName();
    }
  }

  /**
   * Callback when stack has been set
   * @param stack  the selected stack
   */
  cheStackLibrarySelecter(stack) {
    if (stack) {
      this.isCustomStack = false;
      this.recipeUrl = null;
    }
    if (this.stack !== stack && stack && stack.workspaceConfig && stack.workspaceConfig.name) {
      this.setWorkspaceName(stack.workspaceConfig.name);
    } else {
      this.generateWorkspaceName();
    }
    this.stack = stack;
  }

  /**
   * Set workspace name
   * @param name
   */
  setWorkspaceName(name) {
    if (!name) {
      return;
    }
    if (!this.defaultWorkspaceName || this.defaultWorkspaceName === this.workspaceName) {
      this.defaultWorkspaceName = name;
      this.workspaceName = angular.copy(name);
    }
  }

  /**
   * Generates a default workspace name
   */
  generateWorkspaceName() {
    // starts with wksp
    let name = 'wksp';
    name += '-' + (('0000' + (Math.random() * Math.pow(36, 4) << 0).toString(36)).slice(-4)); // jshint ignore:line
    this.setWorkspaceName(name);
  }

  /**
   * Create a new workspace
   */
  createWorkspace() {
    let source = {};
    source.type = 'dockerfile';
    //User provides recipe URL or recipe's content:
    if (this.isCustomStack) {
      this.stack = null;
      if (this.recipeUrl && this.recipeUrl.length > 0) {
        source.location = this.recipeUrl;
        this.submitWorkspace(source);
      } else {
        source.content = this.recipeScript;
        this.submitWorkspace(source);
      }
    } else if (this.selectSourceOption === 'select-source-import') {
      let workspaceConfig = this.importWorkspace.length > 0 ? angular.fromJson(this.importWorkspace) : {};
      workspaceConfig.name = this.workspaceName;
      let creationPromise = this.cheAPI.getWorkspace().createWorkspaceFromConfig(null, workspaceConfig);
      this.redirectAfterSubmitWorkspace(creationPromise);
    } else {
      //check predefined recipe location
      if (this.stack && this.stack.source && this.stack.source.type === 'location') {
        this.recipeUrl = this.stack.source.origin;
        source.location = this.recipeUrl;
        this.submitWorkspace(source);
      } else {
        source = this.getSourceFromStack(this.stack);
        this.submitWorkspace(source);
      }
    }
  }

  /**
   * Detects machine source from pointed stack.
   *
   * @param stack to retrieve described source
   * @returns {source} machine source config
   */
  getSourceFromStack(stack) {
    let source = {};
    source.type = 'dockerfile';

    switch (stack.source.type.toLowerCase()) {
      case 'image':
        source.content = 'FROM ' + stack.source.origin;
        break;
      case 'dockerfile':
        source.content = stack.source.origin;
        break;
      default:
        throw 'Not implemented';
    }

    return source;
  }

  /**
   * Submit a new workspace from current workspace name, source and workspace ram
   *
   * @param source machine source
   */
  submitWorkspace(source) {
    let attributes = this.stack ? {stackId: this.stack.id} : {};
    let stackWorkspaceConfig = this.stack ? this.stack.workspaceConfig : {};
    let workspaceConfig = this.cheAPI.getWorkspace().formWorkspaceConfig(stackWorkspaceConfig, this.workspaceName, source, this.workspaceRam);

    let creationPromise = this.cheAPI.getWorkspace().createWorkspaceFromConfig(null, workspaceConfig, attributes);
    this.redirectAfterSubmitWorkspace(creationPromise);
  }


  /**
   * Handle the redirect for the given promise after workspace has been created
   * @param promise used to gather workspace data
   */
  redirectAfterSubmitWorkspace(promise) {
    promise.then((workspaceData) => {
      // update list of workspaces
      // for new workspace to show in recent workspaces
      this.updateRecentWorkspace(workspaceData.id);

      let infoMessage = 'Workspace ' + workspaceData.config.name + ' successfully created.';
      this.cheNotification.showInfo(infoMessage);
      this.cheAPI.cheWorkspace.fetchWorkspaces().then(() => {
        this.$location.path('/workspace/' + workspaceData.namespace + '/' +  workspaceData.config.name);
      });
    }, (error) => {
      let errorMessage = error.data.message ? error.data.message : 'Error during workspace creation.';
      this.cheNotification.showError(errorMessage);
    });
  }

  /**
   * Emit event to move workspace immediately
   * to top of the recent workspaces list
   *
   * @param workspaceId
   */
  updateRecentWorkspace(workspaceId) {
    this.$rootScope.$broadcast('recent-workspace:set', workspaceId);
  }
}
