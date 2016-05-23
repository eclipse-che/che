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
 * @name workspaces.create.workspace.controller:CreateWorkspaceCtrl
 * @description This class is handling the controller for workspace creation
 * @author Ann Shumilova
 * @author Oleksii Orel
 */
export class CreateWorkspaceCtrl {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($location, cheAPI, cheNotification, lodash, $rootScope) {
    this.$location = $location;
    this.cheAPI = cheAPI;
    this.cheNotification = cheNotification;
    this.lodash = lodash;

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
    if (this.isCustomStack) {
      this.stack = null;
      if (this.recipeUrl && this.recipeUrl.length > 0) {
        this.submitWorkspace();
      } else {
        let recipeName = 'rcp-' + (('0000' + (Math.random() * Math.pow(36, 4) << 0).toString(36)).slice(-4)); // jshint ignore:line
        // needs to get recipe URL from custom recipe
        let promise = this.submitRecipe(recipeName, this.recipeScript);
        promise.then((recipe) => {
          let findLink = this.lodash.find(recipe.links, function (link) {
            return link.rel === 'get recipe script';
          });
          if (findLink) {
            this.recipeUrl = findLink.href;
            this.submitWorkspace();
          }
        }, (error) => {
          this.cheNotification.showError(error.data.message ? error.data.message : 'Error during recipe creation.');
        });
      }
    } else if (this.selectSourceOption === 'select-source-import') {
      let workspaceConfig = this.importWorkspace.length > 0 ? angular.fromJson(this.importWorkspace).config : {};
      let creationPromise = this.cheAPI.getWorkspace().createWorkspaceFromConfig(null, workspaceConfig);
      this.redirectAfterSubmitWorkspace(creationPromise);
    } else {
      //check predefined recipe location
      if (this.stack && this.stack.source && this.stack.source.type === 'location') {
        this.recipeUrl = this.stack.source.origin;
        this.submitWorkspace();
      } else {
        // needs to get recipe URL from stack
        let promise = this.computeRecipeForStack(this.stack);
        promise.then((recipe) => {
          let findLink = this.lodash.find(recipe.links, function (link) {
            return link.rel === 'get recipe script';
          });
          if (findLink) {
            this.recipeUrl = findLink.href;
            this.submitWorkspace();
          }
        }, (error) => {
          this.cheNotification.showError(error.data.message ? error.data.message : 'Error during recipe creation.');
        });
      }
    }
  }

  /**
   * User has selected a stack. needs to find or add recipe for that stack
   * @param stack the selected stack
   * @returns {*} the promise
   */
  computeRecipeForStack(stack) {
    let recipeSource = stack.source;
    // look at recipe
    let recipeName = 'generated-' + stack.name;
    let recipeScript;
    // what is type of source ?
    switch (recipeSource.type.toLowerCase()) {
      case 'image':
        recipeScript = 'FROM ' + recipeSource.origin;
        break;
      case 'dockerfile':
        recipeScript = recipeSource.origin;
        break;
      default:
        throw 'Not implemented';
    }

    let promise = this.submitRecipe(recipeName, recipeScript);

    return promise;
  }

  /**
   * Create a new recipe
   * @param recipeName the recipe name
   * @param recipeScript the recipe script
   * @returns {*} the promise
   */
  submitRecipe(recipeName, recipeScript) {
    let recipe = angular.copy(this.cheAPI.getRecipeTemplate().getDefaultRecipe());
    if (!recipe) {
      return;
    }
    recipe.name = recipeName;
    recipe.script = recipeScript;

    let promise = this.cheAPI.getRecipe().create(recipe);

    return promise;
  }

  /**
   * Submit a new workspace from current workspace name, recipe url and workspace ram
   */
  submitWorkspace() {
    let attributes = this.stack ? {stackId: this.stack.id} : {};
    let creationPromise = this.cheAPI.getWorkspace().createWorkspace(null, this.workspaceName, this.recipeUrl, this.workspaceRam, attributes);
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
      this.cheAPI.cheWorkspace.fetchWorkspaces();

      let infoMessage = 'Workspace ' + workspaceData.config.name + ' successfully created.';
      this.cheNotification.showInfo(infoMessage);
      this.$location.path('/workspace/' + workspaceData.id);
    }, (error) => {
      let errorMessage = error.data.message ? error.data.message : 'Error during workspace creation.';
      this.cheNotification.showError(errorMessage);
    });
  }

}
