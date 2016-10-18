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

/**
 * @ngdoc controller
 * @name workspaces.workspace.details.controller:WorkspaceDetailsController
 * @description This class is handling the controller for workspace to create and edit.
 * @author Ann Shumilova
 * @author Oleksii Kurinnyi
 */

enum Tab {Settings, Runtime}

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
  ideSvc: IdeSvc;
  workspaceDetailsService: WorkspaceDetailsService;

  loading: boolean = false;
  isCreationFlow: boolean = true;
  selectedTabIndex: number;

  namespace: string;
  workspaceId: string;
  workspaceName: string;
  newName: string;
  stack: any;
  workspaceDetails: any = {};
  copyWorkspaceDetails: any = {};
  machinesViewStatus: any = {};
  errorMessage: string;
  invalidWorkspace: string;
  editMode: boolean = false;
  showApplyMessage: boolean = false;

  usedNamesList: any = [];

  forms: Map<number, any> = new Map();
  tab: Object;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($location: ng.ILocationService, $log: ng.ILogService, $mdDialog: ng.material.IDialogService, $q: ng.IQService, $route: ng.route.IRouteService, $rootScope: ng.IRootScopeService, $scope: ng.IScope, $timeout: ng.ITimeoutService, cheEnvironmentRegistry: CheEnvironmentRegistry, cheNotification: CheNotification, cheWorkspace: CheWorkspace, ideSvc: IdeSvc, workspaceDetailsService: WorkspaceDetailsService) {
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
    this.ideSvc = ideSvc;
    this.workspaceDetailsService = workspaceDetailsService;

    this.tab = Tab;

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
          case 'info':
            selectedTabIndex = Tab.Settings;
            break;
          case 'projects':
            selectedTabIndex = 2;
            break;
          case 'share':
            selectedTabIndex = 3;
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
      this.namespace = '';
      this.workspaceName = this.generateWorkspaceName();
      this.copyWorkspaceDetails = {config: {}};
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
    if (this.workspaceDetails && this.workspaceDetails.config) {
      return this.workspaceDetails.config.name !== this.newName;
    }
    return false;
  }

  /**
   * Updates name of workspace in config.
   *
   * @param isFormValid {boolean}
   */
  updateName(isFormValid: boolean): void {
    if (isFormValid === false || !this.isNameChanged()) {
      return;
    }

    this.copyWorkspaceDetails.config.name = this.newName;

    if (!this.isCreationFlow) {
      this.doUpdateWorkspace();
    }
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
   * Returns workspace details sections (tabs, example - projects)
   *
   * @returns {*}
   */
  getSections(): any {
    return this.workspaceDetailsService.getSections();
  }

  /**
   * Callback when environment has been changed.
   *
   * @returns {ng.IPromise<any>}
   */
  updateWorkspaceConfig(): ng.IPromise<any> {
    if (!this.isCreationFlow) {
      this.editMode = !angular.equals(this.copyWorkspaceDetails.config, this.workspaceDetails.config);

      let status = this.getWorkspaceStatus();
      if (status === 'STOPPED' || status === 'STOPPING') {
        this.showApplyMessage = false;
      } else {
        this.showApplyMessage = true;
      }
    }

    let defer = this.$q.defer();
    defer.resolve();
    return defer.promise;
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
    let attributes = this.stack ? {stackId: this.stack.id} : {};
    let creationPromise = this.cheWorkspace.createWorkspaceFromConfig(null, this.copyWorkspaceDetails.config, attributes);
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

      let infoMessage = 'Workspace ' + workspaceData.config.name + ' successfully created.';
      this.cheNotification.showInfo(infoMessage);
      this.cheWorkspace.fetchWorkspaces().then(() => {
        this.$location.path('/workspace/' + workspaceData.namespace + '/' +  workspaceData.config.name);
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
    delete this.errorMessage;

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
   * Creates snapshot of workspace
   */
  createSnapshotWorkspace(): void {
    this.cheWorkspace.createSnapshot(this.workspaceId).then(() => {}, (error: any) => {
      this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Creating snapshot failed.');
      this.$log.error(error);
    });
  }

  /**
   * Register forms
   *
   * @param tab {number} tab number
   * @param form
   */
  setForm(tab: number, form: ng.IFormController): void {
    this.forms.set(tab, form);
  }

  /**
   * Returns false if all forms from specified tabs are valid
   *
   * @param tabs {Array} list of tab IDs
   * @returns {boolean}
   */
  checkFormsNotValid(tabs: number[]): boolean {
    return tabs.some((tab: number) => {
      let form = this.forms.get(tab);
      return form && form.$invalid;
    });
  }

  /**
   * Returns true when 'Create' button should be disabled
   *
   * @returns {boolean}
   */
  isCreateButtonDisabled(): boolean {
    let tabs = [Tab.Settings, Tab.Runtime];

    return this.checkFormsNotValid(tabs);
  }

  /**
   * Returns true when 'Runtime' tab should be disabled
   *
   * @returns {boolean}
   */
  isRuntimeTabDisabled(): boolean {
    return this.checkFormsNotValid([Tab.Settings]);
  }
}

