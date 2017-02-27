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

/**
 * Controller for a factory information.
 * @author Oleksii Orel
 */
export class FactoryInformationCtrl {

  private  confirmDialogService: any;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($scope, cheAPI,  cheNotification, $location, $mdDialog, $log, $timeout, lodash, $filter, $q, confirmDialogService: any) {
    this.cheAPI = cheAPI;
    this.cheNotification = cheNotification;
    this.$location = $location;
    this.$mdDialog = $mdDialog;
    this.$log = $log;
    this.$timeout = $timeout;
    this.lodash = lodash;
    this.$filter = $filter;
    this.confirmDialogService = confirmDialogService;

    this.timeoutPromise = null;
    $scope.$on('$destroy', () => {
      if (this.timeoutPromise) {
        $timeout.cancel(this.timeoutPromise);
      }
    });

    let editorLoadedDefer = $q.defer();
    this.editorLoadedPromise = editorLoadedDefer.promise;
    this.editorOptions = {
      onLoad: ((instance) => {
        editorLoadedDefer.resolve(instance);
      })
    };

    this.factoryInformationForm;
    this.stackRecipeMode = 'current-recipe';

    this.updateData();
    $scope.$watch(() => {
      return this.factory;
    }, () => {
      this.updateData();
    });
  }


  /**
   * Update factory content data for editor
   */
  updateData() {
    if (!this.factory) {
      return;
    }

    this.copyOriginFactory = angular.copy(this.factory);
    if (this.copyOriginFactory.links) {
      delete this.copyOriginFactory.links;
    }

    let factoryContent = this.$filter('json')(this.copyOriginFactory);
    if (factoryContent !== this.factoryContent) {
      if (!this.factoryContent) {
        this.editorLoadedPromise.then((instance) => {
          this.$timeout(() => {
            instance.refresh();
          }, 500);
        })
      }
      this.factoryContent = factoryContent;
    }
  }

  getObjectKeys(targetObject) {
    return Object.keys(targetObject);
  }

  isFactoryChanged() {
    if (!this.copyOriginFactory) {
      return false;
    }

    let testFactory = angular.copy(this.factory);
    if (testFactory.links) {
      delete testFactory.links;
    }

    return angular.equals(this.copyOriginFactory, testFactory) !== true;
  }

  updateFactory() {
    this.factoryContent = this.$filter('json')(this.copyOriginFactory);

    if (this.factoryInformationForm.$invalid || !this.isFactoryChanged()) {
      return;
    }

    this.$timeout.cancel(this.timeoutPromise);
    this.timeoutPromise = this.$timeout(() => {
      this.doUpdateFactory(this.copyOriginFactory);
    }, 500);
  }

  /**
   * Returns the factory url based on id.
   * @returns {link.href|*} link value
   */
  getFactoryIdUrl() {
    return this.cheAPI.getFactory().getFactoryIdUrl(this.factory);
  }

  /**
   * Returns the factory url based on name.
   *
   * @returns {link.href|*} link value
   */
  getFactoryNamedUrl() {
    return this.cheAPI.getFactory().getFactoryNamedUrl(this.factory);
  }

  /**
   * Callback to update factory
   */
  doUpdateFactory(factory) {
    let promise = this.cheAPI.getFactory().setFactory(factory);

    promise.then((factory) => {
      this.factory = factory;
      this.cheNotification.showInfo('Factory information successfully updated.');
    }, (error) => {
      this.cheNotification.showError(error.data.message ? error.data.message : 'Update factory failed.');
      this.$log.log(error);
    });
  }

  factoryEditorOnFocus() {
    if (this.timeoutPromise) {
      this.$timeout.cancel(this.timeoutPromise);
      this.doUpdateFactory(this.copyOriginFactory);
    }
  }

  factoryEditorReset() {
    this.factoryContent = this.$filter('json')(this.copyOriginFactory, 2);
  }

  updateFactoryContent() {
    let promise = this.cheAPI.getFactory().setFactoryContent(this.factory.id, this.factoryContent);

    promise.then((factory) => {
      this.factory = factory;
      this.cheNotification.showInfo('Factory information successfully updated.');
    }, (error) => {
      this.factoryContent = this.$filter('json')(this.copyOriginFactory, 2);
      this.cheNotification.showError(error.data.message ? error.data.message : 'Update factory failed.');
      this.$log.error(error);
    });
  }

  // perform factory deletion.
  deleteFactory(): void {
    let content = 'Please confirm removal for the factory \'' + (this.factory.name ? this.factory.name : this.factory.id) + '\'.';
    let promise = this.confirmDialogService.showConfirmDialog('Remove the factory', content, 'Delete');

    promise.then(() => {
      // remove it !
      let promise = this.cheAPI.getFactory().deleteFactoryById(this.factory.id);
      promise.then(() => {
        this.$location.path('/factories');
      }, (error) => {
        this.cheNotification.showError(error.data.message ? error.data.message : 'Delete failed.');
        this.$log.log(error);
      });
    });
  }

  /**
   * Callback when changing stack tab
   */
  setStackTab() {
  }


  /**
   * Callback when stack has been set
   * @param stack  the selected stack
   */
  cheStackLibrarySelecter(stack) {
    this.stack = stack
  }

  /**
   * Callback when user ask to validate a stack
   * We need then to create (if required) recipe and update JSON factory configuration
   */

  validateStack() {
    //check predefined recipe location
    if (this.stack) {
      // needs to get recipe URL from stack
      let promise = this.computeRecipeForStack(this.stack);
      promise.then((recipe) => {
        this.createRecipe(recipe);
      }, (error) => {
        this.cheNotification.showError(error.data.message ? error.data.message : 'Error during recipe creation.');
      });
    } else if (this.recipeUrl) {
      this.updateMachineRecipeLocation(this.recipeUrl);
    } else if (this.recipeScript) {
      // create recipe from script
      let promise = this.submitRecipe('generated-script', this.recipeScript);
      promise.then((recipe) => {
        this.createRecipe(recipe);
      }, (error) => {
        this.cheNotification.showError(error.data.message ? error.data.message : 'Error during recipe creation.');
      });
    }
  }

  /**
   * Get recipe link from newly created recipe
   * @param recipe the recipe result
   */
  createRecipe(recipe) {
    let findLink = this.lodash.find(recipe.links, (link) => {
      return link.rel === 'get recipe script';
    });
    if (findLink) {
      this.updateMachineRecipeLocation(findLink.href);
    }
  }

  /**
   * User has selected a stack. needs to find or add recipe for that stack
   */
  computeRecipeForStack(stack) {
    // look at recipe
    let recipeSource = stack.source;

    let promise;

    // what is type of source ?
    if ('image' === recipeSource.type) {
      // needs to add recipe for that script
      promise = this.submitRecipe('generated-' + stack.name, 'FROM ' + recipeSource.origin);
    } else if ('recipe' === recipeSource.type) {
      promise = this.submitRecipe('generated-' + stack.name, recipeSource.origin);
    } else {
      throw 'Not implemented';
    }

    return promise;
  }

  /**
   * Create a new recipe based on a given name and a given script
   * @param recipeName the name of the recipe
   * @param recipeScript the content of the docker script for example
   * @returns {recipe} promise
   */
  submitRecipe(recipeName, recipeScript) {
    let recipe = {
      type: 'docker',
      name: recipeName,
      permissions: {
        groups: [
          {
            name: 'public',
            acl: [
              'read'
            ]
          }
        ],
        users: {}
      },
      script: recipeScript
    };

    return this.cheAPI.getRecipe().create(recipe);
  }

  /**
   * Update the machine recipe URL
   * @param recipeURL
   */
  updateMachineRecipeLocation(recipeURL) {
    if (!this.copyOriginFactory) {
      return;
    }
    let machineConfig = this.copyOriginFactory.workspace.environments[0].machines[0];
    machineConfig.source.type = 'recipe';
    machineConfig.source.location = recipeURL;

    this.updateFactory();
  }

}
