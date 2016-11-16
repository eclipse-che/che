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
import {CheEnvironmentRegistry} from '../../../components/api/environment/che-environment-registry.factory';
import {CheNotification} from '../../../components/notification/che-notification.factory';
import {CheWorkspace} from '../../../components/api/che-workspace.factory';
import IdeSvc from '../../ide/ide.service';
import {WorkspaceDetailsService} from './workspace-details.service';
import {CheNamespaceRegistry} from '../../../components/api/namespace/che-namespace-registry.factory';

/**
 * @ngdoc controller
 * @name workspaces.workspace.details.controller:WorkspaceDetailsController
 * @description This class is handling the controller for workspace to create and edit.
 * @author Ann Shumilova
 * @author Oleksii Kurinnyi
 */

enum Tab {Settings, Config, Runtime}

export class WorkspaceDetailsController {
  $location: ng.ILocationService;
  $log: ng.ILogService;
  $mdDialog: ng.material.IDialogService;
  $q: ng.IQService;
  $route: ng.route.IRouteService;
  $rootScope: ng.IRootScopeService;
  $scope: ng.IScope;
  $timeout: ng.ITimeoutService;
  cheEnvironmentRegistry: CheEnvironmentRegistry;
  cheNotification: CheNotification;
  cheWorkspace: CheWorkspace;
  cheNamespaceRegistry: CheNamespaceRegistry;
  ideSvc: IdeSvc;
  workspaceDetailsService: WorkspaceDetailsService;

  loading: boolean = false;
  isCreationFlow: boolean = true;
  selectedTabIndex: number;

  namespace: string = '';
  workspaceId: string = '';
  workspaceName: string = '';
  newName: string = '';
  stackId: string = '';
  workspaceDetails: any = {};
  copyWorkspaceDetails: any = {};
  machinesViewStatus: any = {};
  errorMessage: string = '';
  invalidWorkspace: string = '';
  editMode: boolean = false;
  showApplyMessage: boolean = false;
  workspaceNamespace: string = undefined;
  workspaceImportedRecipe: {
    type: string,
    content: string,
    location: string
  };

  usedNamesList: any = [];

  forms: Map<number, any> = new Map();
  tab: Object = Tab;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($location: ng.ILocationService, $log: ng.ILogService, $mdDialog: ng.material.IDialogService, $q: ng.IQService,
              $route: ng.route.IRouteService, $rootScope: ng.IRootScopeService, $scope: ng.IScope, $timeout: ng.ITimeoutService,
              cheEnvironmentRegistry: CheEnvironmentRegistry, cheNotification: CheNotification, cheWorkspace: CheWorkspace,
              ideSvc: IdeSvc, workspaceDetailsService: WorkspaceDetailsService, cheNamespaceRegistry: CheNamespaceRegistry) {
    this.$log = $log;
    this.$location = $location;
    this.$mdDialog = $mdDialog;
    this.$q = $q;
    this.$route = $route;
    this.$rootScope = $rootScope;
    this.$scope = $scope;
    this.$timeout = $timeout;
    this.cheEnvironmentRegistry = cheEnvironmentRegistry;
    this.cheNotification = cheNotification;
    this.cheWorkspace = cheWorkspace;
    this.cheNamespaceRegistry = cheNamespaceRegistry;
    this.ideSvc = ideSvc;
    this.workspaceDetailsService = workspaceDetailsService;

    cheWorkspace.fetchWorkspaces().then(() => {
      let workspaces: any[] = cheWorkspace.getWorkspaces();
      workspaces.forEach((workspace: any) => {
        this.usedNamesList.push(workspace.config.name);
      });
    });

    (this.$rootScope as any).showIDE = false;

    this.init();
  }

  init(): void {
    let routeParams = this.$route.current.params;
    if (routeParams && routeParams.namespace && routeParams.workspaceName) {
      this.isCreationFlow = false;
      this.namespace = routeParams.namespace;
      this.workspaceName = routeParams.workspaceName;

      this.fetchWorkspaceDetails().then(() => {
        this.workspaceDetails = this.cheWorkspace.getWorkspaceByName(this.namespace, this.workspaceName);
        this.updateWorkspaceData();
      }).finally(() => {
        this.loading = false;
      });

      // search the selected page
      let page = this.$route.current.params.page;
      if (!page) {
        this.$location.path('/workspace/' + this.namespace + '/' + this.workspaceName);
      } else {
        let selectedTabIndex = Tab.Settings;
        switch (page) {
          case 'info' || 'Settings':
            selectedTabIndex = Tab.Settings;
            break;
          case 'Config':
            selectedTabIndex = Tab.Config;
            break;
          case 'Runtime':
            selectedTabIndex = Tab.Runtime;
            break;
          case 'projects':
            selectedTabIndex = 3;
            break;
          case 'share':
            selectedTabIndex = 4;
            break;
          default:
            this.$location.path('/workspace/' + this.namespace + '/' + this.workspaceName);
            break;
        }
        this.$timeout(() => {
          this.selectedTabIndex = selectedTabIndex;
        });
      }
    } else {
      this.isCreationFlow = true;
      this.editMode = true;
      this.namespace = '';
      this.workspaceName = this.generateWorkspaceName();
      this.workspaceDetails = {
        config: {
          environments: {
            [this.workspaceName]: {}
          },
          defaultEnv: this.workspaceName,
          name: this.workspaceName
        }
      };
      this.copyWorkspaceDetails = angular.copy(this.workspaceDetails);
    }
    this.newName = this.workspaceName;
  }

  /**
   * Fetches the workspace details.
   *
   * @returns {Promise}
   */
  fetchWorkspaceDetails(): ng.IPromise<any> {
    let defer = this.$q.defer();

    if (this.cheWorkspace.getWorkspaceByName(this.namespace, this.workspaceName)) {
      defer.resolve();
    } else {
      this.cheWorkspace.fetchWorkspaceDetails(this.namespace + ':' + this.workspaceName).then(() => {
        defer.resolve();
      }, (error: any) => {
        if (error.status === 304) {
          defer.resolve();
        } else {
          this.invalidWorkspace = error.statusText;
          defer.reject(error);
        }
      });
    }
    return defer.promise;
  }

  /**
   * Creates copy of workspace config.
   */
  updateWorkspaceData(): void {
    if (this.loading) {
      this.loading = false;
    }

    angular.copy(this.workspaceDetails, this.copyWorkspaceDetails);

    this.workspaceId = this.workspaceDetails.id;
    this.newName = this.workspaceDetails.config.name;
  }

  /**
   * Returns true if name of workspace is changed.
   *
   * @returns {boolean}
   */
  isNameChanged(): boolean {
    if (this.newName && this.workspaceDetails && this.workspaceDetails.config) {
      return this.workspaceDetails.config.name !== this.newName;
    }
    return false;
  }

  /**
   * Updates name of workspace in config.
   *
   * @param form {any}
   */
  updateName(form: any): void {
    if (form.$invalid) {
      return;
    }

    this.copyWorkspaceDetails.config.name = this.newName;
    this.switchEditMode();
  }

  /**
   * Returns current status of workspace.
   *
   * @returns {String}
   */
  getWorkspaceStatus(): string {
    if (this.isCreationFlow) {
      return 'CREATING';
    }

    let unknownStatus = 'unknown';
    let workspace = this.cheWorkspace.getWorkspaceById(this.workspaceId);
    return workspace ? workspace.status : unknownStatus;
  }

  /**
   * Returns the list of available namespaces.
   *
   * @returns {Array} array of namespaces
   */
  getNamespaces(): Array<string> {
    return this.cheNamespaceRegistry.getNamespaces();
  }

  /**
   * Returns workspace details sections (tabs, example - projects)
   *
   * @returns {*}
   */
  getSections(): any {
    return this.workspaceDetailsService.getSections();
  }

  /**
   * Callback when workspace config has been changed in editor.
   *
   * @param config {object} workspace config
   */
  updateWorkspaceConfigImport(config: any): void {
    if (!config) {
      return;
    }

    if (this.newName !== config.name) {
      this.newName = config.name;
    }

    this.copyWorkspaceDetails.config = config;
    this.workspaceImportedRecipe = config.environments[config.defaultEnv].recipe;

    this.switchEditMode();
  }

  /**
   * Callback when environment has been changed.
   */
  updateWorkspaceConfigEnvironment(): void {
    delete this.workspaceImportedRecipe;
    this.switchEditMode();
  }

  switchEditMode(): void {
    if (!this.isCreationFlow) {
      this.editMode = !angular.equals(this.copyWorkspaceDetails.config, this.workspaceDetails.config);

      let status = this.getWorkspaceStatus();
      if (status === 'STOPPED' || status === 'STOPPING') {
        this.showApplyMessage = false;
      } else {
        this.showApplyMessage = true;
      }
    }
  }

  saveWorkspace(): void {
    if (this.isCreationFlow) {
      this.createWorkspace();
    } else {
      this.applyConfigChanges();
    }
  }

  /**
   * Updates workspace config and restarts workspace if it's necessary
   */
  applyConfigChanges(): void {
    this.editMode = false;
    this.showApplyMessage = false;

    let status = this.getWorkspaceStatus();

    if (status !== 'RUNNING' && status !== 'STARTING') {
      this.doUpdateWorkspace();
      return;
    }

    this.selectedTabIndex = 0;
    this.loading = true;

    let stoppedStatusPromise = this.cheWorkspace.fetchStatusChange(this.workspaceId, 'STOPPED');
    if (status === 'RUNNING') {
      this.stopWorkspace();
      stoppedStatusPromise.then(() => {
        return this.doUpdateWorkspace();
      }).then(() => {
        this.runWorkspace();
      });
      return;
    }

    let runningStatusPromise = this.cheWorkspace.fetchStatusChange(this.workspaceId, 'RUNNING');
    if (status === 'STARTING') {
      runningStatusPromise.then(() => {
        this.stopWorkspace();
        return stoppedStatusPromise;
      }).then(() => {
        return this.doUpdateWorkspace();
      }).then(() => {
        this.runWorkspace();
      });
    }
  }

  /**
   * Cancels workspace config changes that weren't stored
   */
  cancelConfigChanges(): void {
    if (this.isCreationFlow) {
      this.$location.path('/workspaces');
    }

    this.editMode = false;
    this.updateWorkspaceData();
  }

  /**
   * Updates workspace info.
   *
   * @returns {ng.IPromise<any>}
   */
  doUpdateWorkspace(): ng.IPromise<any> {
    delete this.copyWorkspaceDetails.links;

    let promise = this.cheWorkspace.updateWorkspace(this.workspaceId, this.copyWorkspaceDetails);
    promise.then((data: any) => {
      this.workspaceName = data.config.name;
      this.workspaceDetails = this.cheWorkspace.getWorkspaceByName(this.namespace, this.workspaceName);
      this.updateWorkspaceData();
      this.cheNotification.showInfo('Workspace updated.');
      return this.$location.path('/workspace/' + this.namespace + '/' + this.workspaceName);
    }, (error: any) => {
      this.loading = false;
      this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Update workspace failed.');
      this.$log.error(error);
    });

    return promise;
  }

  /**
   * Generates a default workspace name
   *
   * @returns {String} name of workspace
   */
  generateWorkspaceName(): string {
    let name,
        iterations = 100;
    while (iterations--) {
      name = 'wksp-' + (('0000' + (Math.random() * Math.pow(36, 4) << 0).toString(36)).slice(-4)); // jshint ignore:line
      if (!this.usedNamesList.includes(name)) {
        break;
      }
    }
    return name;
  }

  /**
   * Submit a new workspace
   */
  createWorkspace(): void {
    let attributes = this.stackId ? {stackId: this.stackId} : {};
    let creationPromise = this.cheWorkspace.createWorkspaceFromConfig(this.workspaceNamespace, this.copyWorkspaceDetails.config, attributes);
    this.redirectAfterSubmitWorkspace(creationPromise);
  }


  /**
   * Handle the redirect for the given promise after workspace has been created
   *
   * @param promise {ng.IPromise<any>} used to gather workspace data
   */
  redirectAfterSubmitWorkspace(promise: ng.IPromise<any>): void {
    promise.then((workspaceData: any) => {
      // update list of workspaces
      // for new workspace to show in recent workspaces
      this.updateRecentWorkspace(workspaceData.id);
      this.cheWorkspace.fetchWorkspaces().then(() => {
        this.$location.path('/workspace/' + workspaceData.namespace + '/' +  workspaceData.config.name);
        this.$location.search({page: this.tab[this.selectedTabIndex]});
      });
    }, (error: any) => {
      let errorMessage = error.data.message ? error.data.message : 'Error during workspace creation.';
      this.cheNotification.showError(errorMessage);
    });
  }

  /**
   * Emit event to move workspace immediately
   * to top of the recent workspaces list in left navbar
   *
   * @param workspaceId {string}
   */
  updateRecentWorkspace(workspaceId: string): void {
    this.$rootScope.$broadcast('recent-workspace:set', workspaceId);
  }

  /**
   * Updates the workspace's environment with data entered by user.
   *
   * @param workspace workspace to update
   */
  setEnvironment(workspace: any): void {
    if (!workspace.defaultEnv || !workspace.environments || workspace.environments.length === 0) {
      return;
    }

    let environment = workspace.environments[workspace.defaultEnv];
    if (!environment) {
      return;
    }

    let recipeType = environment.recipe.type;
    let environmentManager = this.cheEnvironmentRegistry.getEnvironmentManager(recipeType);
    let machinesList = environmentManager.getMachines(environment);
    workspace.environments[workspace.defaultEnv] = environmentManager.getEnvironment(environment, machinesList);
  }

  // perform workspace deletion.
  deleteWorkspace(event: MouseEvent): void {
    let confirm = this.$mdDialog.confirm()
      .title('Would you like to delete workspace \'' + this.workspaceDetails.config.name + '\'?')
      .ariaLabel('Delete workspace')
      .ok('Delete it!')
      .cancel('Cancel')
      .clickOutsideToClose(true)
      .targetEvent(event);
    this.$mdDialog.show(confirm).then(() => {
      if (this.workspaceDetails.status === 'STOPPED' || this.workspaceDetails.status === 'ERROR') {
        this.removeWorkspace();
      } else if (this.workspaceDetails.status === 'RUNNING') {
        this.cheWorkspace.stopWorkspace(this.workspaceId);
        this.cheWorkspace.fetchStatusChange(this.workspaceId, 'STOPPED').then(() => {
          this.removeWorkspace();
        });
      }
    });
  }

  removeWorkspace(): ng.IPromise<any> {
    let promise = this.cheWorkspace.deleteWorkspaceConfig(this.workspaceId);

    promise.then(() => {
      this.$location.path('/workspaces');
    }, (error: any) => {
      this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Delete workspace failed.');
      this.$log.error(error);
    });

    return promise;
  }

  runWorkspace(): void {
    this.errorMessage = '';

    let promise = this.ideSvc.startIde(this.workspaceDetails);
    promise.then(() => {}, (error: any) => {
      let errorMessage;

      if (!error || !(error.data || error.error)) {
        errorMessage = 'Unable to start this workspace.';
      } else if (error.error) {
        errorMessage = error.error;
      } else if (error.data.errorCode === 10000 && error.data.attributes) {
        let attributes = error.data.attributes;

        errorMessage = 'Unable to start this workspace.' +
          ' There are ' + attributes.workspaces_count + ' running workspaces consuming ' +
          attributes.used_ram + attributes.ram_unit + ' RAM.' +
          ' Your current RAM limit is ' + attributes.limit_ram + attributes.ram_unit +
          '. This workspace requires an additional ' +
          attributes.required_ram + attributes.ram_unit + '.' +
          '  You can stop other workspaces to free resources.';
      } else {
        errorMessage = error.data.message;
      }

      this.cheNotification.showError(errorMessage);
      this.$log.error(error);

      this.errorMessage = errorMessage;
    });
  }

  stopWorkspace(): void {
    let promise = this.cheWorkspace.stopWorkspace(this.workspaceId);

    promise.then(() => {}, (error: any) => {
      this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Stop workspace failed.');
      this.$log.error(error);
    });
  }

  /**
   * Register form for corresponding tab.
   *
   * @param tabIndex {Number}
   * @param form
   */
  setForm(tabIndex: number, form: ng.IFormController): void {
    this.forms.set(tabIndex, form);
  }

  /**
   * Returns false if all forms from specified tabs are valid
   *
   * @param tabIndex {Number}
   * @returns {Boolean}
   */
  checkFormsNotValid(tabIndex: number): boolean {
    let form = this.forms.get(tabIndex);
    return form && form.$invalid;
  }

  /**
   * Returns true when 'Save' button should be disabled
   *
   * @returns {boolean}
   */
  isSaveButtonDisabled(): boolean {
    let tabs = [Tab.Settings, Tab.Config, Tab.Runtime];

    return tabs.some((tabIndex: number) => {
      return this.checkFormsNotValid(tabIndex);
    });
  }

  /**
   * Returns true when specified tab should be disabled.
   *
   * @param tabIndex {Number}
   * @returns {boolean}
   */
  isTabDisabled(tabIndex: number): boolean {
    if (tabIndex === Tab.Settings) {
      // never disable 'Settings' tab
      return false;
    }

    // activate tab which contains invalid form
    // to let user see the problem
    if (this.checkFormsNotValid(Tab.Settings)) {
      this.selectedTabIndex = Tab.Settings;
    } else if (this.checkFormsNotValid(Tab.Runtime)) {
      this.selectedTabIndex = Tab.Runtime;
    }

    if (tabIndex === Tab.Runtime) {
      return this.checkFormsNotValid(Tab.Settings);
    } else {
      return this.checkFormsNotValid(Tab.Settings) || this.checkFormsNotValid(Tab.Runtime);
    }
  }

}

