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
import {CheAPI} from '../../../../../components/api/che-api.factory';
import {CheNotification} from '../../../../../components/notification/che-notification.factory';
import {ConfirmDialogService} from '../../../../../components/service/confirm-dialog/confirm-dialog.service';
import {CheBranding} from '../../../../../components/branding/che-branding.factory';

/**
 * Controller for a factory information.
 * @author Oleksii Orel
 */
export class FactoryInformationController {

  static $inject = ['$scope', 'cheAPI', 'cheNotification', '$location', '$log', '$timeout', 'lodash', '$filter', '$q', 'cheBranding', 'confirmDialogService'];

  private confirmDialogService: ConfirmDialogService;
  private cheAPI: CheAPI;
  private cheNotification: CheNotification;
  private $location: ng.ILocationService;
  private $log: ng.ILogService;
  private $q: ng.IQService;
  private $timeout: ng.ITimeoutService;
  private lodash: any;
  private $filter: ng.IFilterService;

  private timeoutPromise: ng.IPromise<any>;
  private editorLoadedPromise: ng.IPromise<any>;
  private editorOptions: any;
  private factoryInformationForm: ng.IFormController;
  private stackRecipeMode: string;
  private factory: che.IFactory;
  private copyOriginFactory: che.IFactory;
  private factoryContent: string;
  // private workspaceImportedRecipe: any;
  private environmentName: string;
  private workspaceName: string;
  private stackId: string;
  private workspaceConfig: any;
  private origName: string;
  private isEditorContentChanged: boolean = false;
  private factoryDocs: string;

  /**
   * Default constructor that is using resource injection
   */
  constructor($scope: ng.IScope,
              cheAPI: CheAPI,
              cheNotification: CheNotification,
              $location: ng.ILocationService,
              $log: ng.ILogService,
              $timeout: ng.ITimeoutService,
              lodash: any,
              $filter: ng.IFilterService,
              $q: ng.IQService,
              cheBranding: CheBranding,
              confirmDialogService: ConfirmDialogService) {
    this.cheAPI = cheAPI;
    this.cheNotification = cheNotification;
    this.$location = $location;
    this.$log = $log;
    this.$timeout = $timeout;
    this.$q = $q;
    this.lodash = lodash;
    this.$filter = $filter;
    this.confirmDialogService = confirmDialogService;
    this.factoryDocs = cheBranding.getDocs().factory;

    this.timeoutPromise = null;
    $scope.$on('$destroy', () => {
      if (this.timeoutPromise) {
        $timeout.cancel(this.timeoutPromise);
      }
    });

    let editorLoadedDefer = $q.defer();
    this.editorLoadedPromise = editorLoadedDefer.promise;
    this.editorOptions = {
      onLoad: ((instance: any) => {
        editorLoadedDefer.resolve(instance);
        if (instance) {
          instance.on('blur', () => {
            this.showUpdateIfNecessaryDialog();
          });
        }
      })
    };

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
  updateData(): void {
    if (!this.factory) {
      return;
    }

    this.isEditorContentChanged = false;

    this.workspaceName = this.factory.workspace.name;
    this.environmentName = this.factory.workspace.defaultEnv;

    this.copyOriginFactory = angular.copy(this.factory);
    this.origName = this.factory.name;
    if (this.copyOriginFactory.links) {
      delete this.copyOriginFactory.links;
    }

    let factoryContent = this.$filter('json')(this.copyOriginFactory);
    if (factoryContent !== this.factoryContent) {
      if (!this.factoryContent) {
        this.editorLoadedPromise.then((instance: any) => {
          this.$timeout(() => {
            instance.refresh();
          }, 500);
        });
      }
      this.factoryContent = factoryContent;
    }
  }

  /**
   * Returns object's attributes.
   *
   * @param targetObject object to process
   * @returns {string[]}
   */
  getObjectKeys(targetObject: any): Array<string> {
    return Object.keys(targetObject);
  }

  /**
   * Returns the factory's data changed state.
   *
   * @returns {boolean}
   */
  isFactoryChanged(): boolean {
    if (!this.copyOriginFactory) {
      return false;
    }

    let testFactory = angular.copy(this.factory);
    if (testFactory.links) {
      delete testFactory.links;
    }

    return angular.equals(this.copyOriginFactory, testFactory) !== true;
  }

  /**
   * Update factory name.
   *
   * @param {string} name new factory name.
   */
  updateFactoryName(name: string): void {
    this.copyOriginFactory.name = name;
    this.updateFactory();
  }

  /**
   * Update workspace name.
   *
   * @param {string} name new workspace name.
   */
  updateWorkspaceName(name: string): void {
    this.copyOriginFactory.workspace.name = name;
    this.updateFactory();
  }

  /**
   * Save factory data.
   */
  updateFactory(): void {
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
   * Shows confirmation dialog when editor's content is changes.
   *
   * @returns {angular.IPromise<any>}
   */
  showUpdateIfNecessaryDialog(): ng.IPromise<any> {
    if (this.isEditorContentChanged === false) {
      return this.$q.when();
    }

    const title = 'Warning',
      content = `You have unsaved changes in JSON configuration. Would you like to save changes now?`;
    return this.confirmDialogService.showConfirmDialog(title, content, 'Continue').then(() => {
      this.updateFactoryContent();
    });
  }

  /**
   * Returns the factory url based on id.
   *
   * @returns {link.href|*} link value
   */
  getFactoryIdUrl(): string {
    return this.cheAPI.getFactory().getFactoryIdUrl(this.factory);
  }

  /**
   * Returns the factory url based on name.
   *
   * @returns {link.href|*} link value
   */
  getFactoryNamedUrl(): string {
    return this.cheAPI.getFactory().getFactoryNamedUrl(this.factory);
  }

  /**
   * Callback to update factory
   */
  doUpdateFactory(factory: che.IFactory): void {
    let promise = this.cheAPI.getFactory().setFactory(factory);

    promise.then((factory: che.IFactory) => {
      this.factory = factory;
      this.cheNotification.showInfo('Factory information successfully updated.');
    }, (error: any) => {
      this.cheNotification.showError(error.data.message ? error.data.message : 'Update factory failed.');
      this.$log.log(error);
    });
  }

  /**
   * Handler for factory editor 'change' event.
   */
  factoryEditorOnChange(): void {
    if (this.timeoutPromise) {
      this.$timeout.cancel(this.timeoutPromise);
    }
    this.timeoutPromise = this.$timeout(() => {
      this.isEditorContentChanged = this.factoryContent !== this.$filter('json')(this.copyOriginFactory);
    }, 200);
  }

  /**
   * Resets factory editor.
   */
  factoryEditorReset(): void {
    this.factoryContent = this.$filter('json')(this.copyOriginFactory, 2);
  }

  /**
   * Updates factory's content.
   */
  updateFactoryContent(): void {
    let promise = this.cheAPI.getFactory().setFactoryContent(this.factory.id, this.factoryContent);

    promise.then((factory: che.IFactory) => {
      this.factory = factory;
      this.cheNotification.showInfo('Factory information successfully updated.');
    }, (error: any) => {
      this.factoryContent = this.$filter('json')(this.copyOriginFactory, 2);
      this.cheNotification.showError(error.data.message ? error.data.message : 'Update factory failed.');
      this.$log.error(error);
    });
  }

  /**
   * Deletes factory with confirmation.
   */
  deleteFactory(): void {
    let content = 'Please confirm removal for the factory \'' + (this.factory.name ? this.factory.name : this.factory.id) + '\'.';
    let promise = this.confirmDialogService.showConfirmDialog('Remove the factory', content, 'Delete');

    promise.then(() => {
      // remove it !
      let promise = this.cheAPI.getFactory().deleteFactoryById(this.factory.id);
      promise.then(() => {
        this.$location.path('/factories');
      }, (error: any) => {
        this.cheNotification.showError(error.data.message ? error.data.message : 'Delete failed.');
        this.$log.log(error);
      });
    });
  }

  /**
   * Returns the recipe value of the environment.
   *
   * @returns {any}
   */
  getRecipe(): string {
    if (this.copyOriginFactory && this.copyOriginFactory.workspace && this.copyOriginFactory.workspace.defaultEnv) {
      let environement = this.copyOriginFactory.workspace.environments[this.copyOriginFactory.workspace.defaultEnv];
      return environement.recipe.location || environement.recipe.content;
    }
    return null;
  }

  /**
   * Handles stack and workspace config changes.
   *
   * @param config workspace config
   * @param stackId stack id
   */
  onWorkspaceStackChanged(config: any, stackId: string): void {
    this.stackId = stackId;
    this.workspaceConfig = config;
  }

  /**
   * Saves stacks changes in workspace config inside factory.
   */
  saveStack(): void {
    if (!this.copyOriginFactory) {
      return;
    }
    this.copyOriginFactory.workspace.environments[this.factory.workspace.defaultEnv] = this.workspaceConfig.environments[this.workspaceConfig.defaultEnv];

    this.updateFactory();
  }
}
