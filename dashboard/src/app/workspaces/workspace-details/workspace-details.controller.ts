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
import {CheEnvironmentRegistry} from '../../../components/api/environment/che-environment-registry.factory';
import {CheNotification} from '../../../components/notification/che-notification.factory';
import {CheWorkspace} from '../../../components/api/che-workspace.factory';
import IdeSvc from '../../ide/ide.service';
import {WorkspaceDetailsService} from './workspace-details.service';
import {CheNamespaceRegistry} from '../../../components/api/namespace/che-namespace-registry.factory';
import {ConfirmDialogService} from '../../../components/service/confirm-dialog/confirm-dialog.service';
import {CheUser} from '../../../components/api/che-user.factory';


/**
 * @ngdoc controller
 * @name workspaces.workspace.details.controller:WorkspaceDetailsController
 * @description This class is handling the controller for workspace to create and edit.
 * @author Ann Shumilova
 * @author Oleksii Kurinnyi
 */

const TAB: Array<string> = ['Overview', 'Machines', 'Settings', 'Config', 'Runtime'];

export class WorkspaceDetailsController {
  $location: ng.ILocationService;
  $log: ng.ILogService;
  $mdDialog: ng.material.IDialogService;
  $q: ng.IQService;
  $route: ng.route.IRouteService;
  $rootScope: ng.IRootScopeService;
  $scope: ng.IScope;
  $timeout: ng.ITimeoutService;
  lodash: any;
  cheEnvironmentRegistry: CheEnvironmentRegistry;
  cheNotification: CheNotification;
  cheWorkspace: CheWorkspace;
  cheNamespaceRegistry: CheNamespaceRegistry;
  ideSvc: IdeSvc;
  workspaceDetailsService: WorkspaceDetailsService;
  cheUser: CheUser;

  loading: boolean = false;
  isCreationFlow: boolean = true;
  selectedTabIndex: number;

  namespaceId: string = '';
  namespaceLabel: string;
  namespaceLabels: Array<string>;
  namespaceInfo: String;
  onNamespaceChanged: Function;
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
  workspaceImportedRecipe: che.IRecipe;

  usedNamesList: any = [];

  forms: Map<string, any> = new Map();
  tab: {[key: string]: string} = {};

  private confirmDialogService: ConfirmDialogService;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($location: ng.ILocationService, $log: ng.ILogService, $mdDialog: ng.material.IDialogService, $q: ng.IQService,
              $route: ng.route.IRouteService, $rootScope: ng.IRootScopeService, $scope: ng.IScope, $timeout: ng.ITimeoutService,
              cheEnvironmentRegistry: CheEnvironmentRegistry, cheNotification: CheNotification, cheWorkspace: CheWorkspace,
              ideSvc: IdeSvc, workspaceDetailsService: WorkspaceDetailsService, cheNamespaceRegistry: CheNamespaceRegistry,
              confirmDialogService: ConfirmDialogService, lodash: any, cheUser: CheUser) {
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
    this.confirmDialogService = confirmDialogService;
    this.lodash = lodash;
    this.cheUser = cheUser;

    this.init();

    const workspaceStatusDeRegistrationFn = $scope.$watch(() => {
      return this.getWorkspaceStatus();
    }, (newStatus: string, oldStatus: string) => {
      if (oldStatus === 'SNAPSHOTTING') {
        // status was not changed
        return;
      }
      if (newStatus === 'RUNNING' || newStatus === 'STOPPED') {
        this.cheWorkspace.fetchWorkspaceDetails(this.workspaceId).then(() => {
          this.workspaceDetails = this.cheWorkspace.getWorkspaceByName(this.namespaceId, this.workspaceName);
          this.updateWorkspaceData();
        });
      }
    });

    this.updateSelectedTab(this.$location.search().tab);
    const searchDeRegistrationFn = $scope.$watch(() => {
      return $location.search().tab;
    }, (tab: string) => {
      if (angular.isDefined(tab)) {
        this.updateSelectedTab(tab);
      }
    }, true);
    $scope.$on('$destroy', () => {
      workspaceStatusDeRegistrationFn();
      searchDeRegistrationFn();
    });

  }

  /**
   * Update tabs.
   */
  updateTabs(): void {
    this.tab = {};
    TAB.forEach((tab: string, $index: number) => {
      const index = $index.toString();
      this.tab[tab] = index;
      this.tab[index] = tab;
    });
  }

  /**
   * Add a new tab.
   *
   * @param tab
   */
  addTab(tab: string): void {
    if (this.tab[tab]) {
      return;
    }
    const pos = (Object.keys(this.tab).length / 2).toString();
    this.tab[tab] = pos;
    this.tab[pos] = tab;
  }

  /**
   * Update selected tab index by search part of URL.
   *
   * @param {string} tab
   */
  updateSelectedTab(tab: string): void {
    const value = this.tab[tab];
    if (angular.isDefined(value)) {
      this.selectedTabIndex = parseInt(value, 10);
    }
  }

  /**
   * Changes search part of URL.
   *
   * @param {number} tabIndex
   */
  onSelectTab(tabIndex?: number): void {
    let param: { tab?: string } = {};
    if (angular.isDefined(tabIndex)) {
      param.tab = this.tab[tabIndex.toString()];
    }
    if (angular.isUndefined(this.$location.search().tab)) {
      this.$location.replace().search(param);
    } else {
      this.$location.search(param);
    }
  }

  init(): void {
    this.updateTabs();
    let routeParams = this.$route.current.params;
    if (routeParams && routeParams.namespace && routeParams.workspaceName) {
      this.isCreationFlow = false;
      this.namespaceId = routeParams.namespace;
      this.workspaceName = routeParams.workspaceName;

      this.fetchWorkspaceDetails().then(() => {
        this.workspaceDetails = this.cheWorkspace.getWorkspaceByName(this.namespaceId, this.workspaceName);
        this.updateWorkspaceData();
      }).finally(() => {
        this.loading = false;
      });

      this.fillInListOfUsedNames();

    } else {
      this.isCreationFlow = true;
      this.editMode = true;
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
      this.cheNamespaceRegistry.fetchNamespaces().then(() => {
        // check provided namespace exists:
        let namespace = this.$location.search().namespace ? this.getNamespace(this.$location.search().namespace) : null;

        this.namespaceId = namespace ? namespace.id : (this.getNamespaces().length ? this.getNamespaces()[0].id : undefined);
        this.namespaceLabel = namespace ? namespace.label : (this.getNamespaces().length ? this.getNamespaces()[0].label : undefined);
        this.namespaceLabels = this.getNamespaces().length ? this.lodash.pluck(this.getNamespaces(), 'label') : [];
        this.fetchNamespaceInfo();

        this.onNamespaceChanged = (label: string) => {
          let namespace = this.getNamespaces().find((namespace: any) => {
            return namespace.label === label;
          });
          this.namespaceId = namespace ? namespace.id : this.namespaceId;
          this.fetchNamespaceInfo();
          this.fillInListOfUsedNames();
        };
      }).finally(() => {
        this.fillInListOfUsedNames();
      });
    }
    this.newName = this.workspaceName;
  }

  /**
   * Fills in list of workspace's name in current namespace,
   * and triggers validation of entered workspace's name
   */
  fillInListOfUsedNames(): void {
    const defer = this.$q.defer();
    if (this.namespaceId) {
      defer.resolve();
    }
    if (!this.namespaceId) {
      const user = this.cheUser.getUser();
      if (user) {
        this.namespaceId = user.name;
        defer.resolve();
      } else {
        this.cheUser.fetchUser().then(() => {
          this.namespaceId = this.cheUser.getUser().name;
          defer.resolve();
        }, (error: any) => {
          defer.reject(error);
        });
      }
    }
    defer.promise.then(() => {
      return this.getOrFetchWorkspacesByNamespace();
    }).catch(() => {
      // user is not authorized to get workspaces by namespace
      return this.getOrFetchWorkspaces();
    }).then((workspaces: Array<che.IWorkspace>) => {
      this.usedNamesList = this.buildInListOfUsedNames(workspaces);
      this.reValidateName();
    });
  }

  /**
   * Triggers form validation on Settings tab.
   */
  reValidateName(): void {
    const form: ng.IFormController = this.forms.get((<any>this.tab).Settings);

    if (!form) {
      return;
    }

    ['name', 'deskname'].forEach((inputName: string) => {
      const model = form[inputName] as ng.INgModelController;
      model.$validate();
    });
  }

  /**
   * Returns promise for getting list of workspaces by namespace.
   *
   * @return {ng.IPromise<any>}
   */
  getOrFetchWorkspacesByNamespace(): ng.IPromise<any> {
    const defer = this.$q.defer();

    if (!this.namespaceId) {
      defer.reject([]);
      return defer.promise;
    }

    const workspacesByNamespaceList = this.cheWorkspace.getWorkspacesByNamespace(this.namespaceId) || [];
    if (workspacesByNamespaceList.length) {
      defer.resolve(workspacesByNamespaceList);
    } else {
      this.cheWorkspace.fetchWorkspacesByNamespace(this.namespaceId).then(() => {
        defer.resolve(this.cheWorkspace.getWorkspacesByNamespace(this.namespaceId) || []);
      }, (error: any) => {
        // not authorized
        defer.reject(error);
      });
    }

    return defer.promise;
  }

  /**
   * Returns promise for getting list of workspaces owned by user
   *
   * @return {ng.IPromise<any>}
   */
  getOrFetchWorkspaces(): ng.IPromise<any> {
    const defer = this.$q.defer();
    const workspacesList = this.cheWorkspace.getWorkspaces();
    if (workspacesList.length) {
      defer.resolve(workspacesList);
    } else {
      this.cheWorkspace.fetchWorkspaces().finally(() => {
        defer.resolve(this.cheWorkspace.getWorkspaces());
      });
    }

    return defer.promise;
  }

  /**
   * Filters list of workspaces by current namespace and
   * builds list of names for current namespace.
   *
   * @param {Array<che.IWorkspace>} workspaces list of workspaces
   * @return {string[]}
   */
  buildInListOfUsedNames(workspaces: Array<che.IWorkspace>): string[] {
    return workspaces.filter((workspace: che.IWorkspace) => {
      return workspace.namespace === this.namespaceId && workspace.config.name !== this.workspaceName;
    }).map((workspace: che.IWorkspace) => {
      return workspace.config.name;
    });
  }

  /**
   * Returns <code>false</code> if workspace's name is not unique in the namespace.
   * Only member with 'manageWorkspaces' permission can definitely know whether
   * name is unique or not.
   *
   * @param {string} name workspace's name
   */
  isNameUnique(name: string): boolean {
    return this.usedNamesList.indexOf(name) === -1;
  }

  /**
   * Returns the value of workspace auto-snapshot property.
   *
   * @returns {boolean} workspace auto-snapshot property value
   */
  getAutoSnapshot(): boolean {
    return this.cheWorkspace.getAutoSnapshotSettings();
  }

  /**
   * Fetches the workspace details.
   *
   * @returns {Promise}
   */
  fetchWorkspaceDetails(): ng.IPromise<any> {
    let defer = this.$q.defer();

    if (this.cheWorkspace.getWorkspaceByName(this.namespaceId, this.workspaceName)) {
      defer.resolve();
    } else {
      this.cheWorkspace.fetchWorkspaceDetails(this.namespaceId + '/' + this.workspaceName).then(() => {
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
    this.workspaceId = this.workspaceDetails.id;
    this.copyWorkspaceDetails = angular.copy(this.workspaceDetails);
    this.switchEditMode();
  }

  /**
   * Updates name of workspace in config.
   */
  updateName(): void {
    if (this.workspaceDetails && this.workspaceDetails.config) {
      this.workspaceDetails.config.name = this.newName;
    }

    this.switchEditMode();
  }

  /**
   * Returns current status of workspace.
   *
   * @returns {string}
   */
  getWorkspaceStatus(): string {
    if (this.isCreationFlow) {
      return 'New';
    }

    let unknownStatus = 'unknown';
    let workspace = this.cheWorkspace.getWorkspaceById(this.workspaceId);
    return workspace ? workspace.status : unknownStatus;
  }

  /**
   * Returns the list of available namespaces.
   *
   * @returns {INamespace[]} array of namespaces
   */
  getNamespaces(): che.INamespace[] {
    return this.cheNamespaceRegistry.getNamespaces();
  }

  /**
   * Returns namespace by its ID
   *
   * @param {string} namespaceId
   * @return {INamespace|{}}
   */
  getNamespace(namespaceId: string): che.INamespace {
    return this.getNamespaces().find((namespace: any) => {
      return namespace.id === namespaceId;
    });
  }

  /**
   * Returns namespace's label
   *
   * @param {string} namespaceId
   * @return {string}
   */
  getNamespaceLabel(namespaceId: string): string {
    let namespace = this.getNamespace(namespaceId);
    if (namespace) {
      return namespace.label;
    } else {
      return namespaceId;
    }
  }

  fetchNamespaceInfo() {
    if (!this.cheNamespaceRegistry.getAdditionalInfo()) {
      this.namespaceInfo = null;
      return;
    }

    this.cheNamespaceRegistry.getAdditionalInfo()(this.namespaceId).then((info: string) => {
      this.namespaceInfo = info;
    });
  }

  /**
   * Callback when Team button is clicked in Edit mode.
   * Redirects to billing details or team details.
   *
   * @param {string} namespaceId
   */
  namespaceOnClick(namespaceId: string): void {
    let namespace = this.getNamespace(namespaceId);
    this.$location.path(namespace.location);
  }

  /**
   * Returns workspace details pages (tabs, example - projects)
   *
   * @returns {*}
   */
  getPages(): any {
    return this.workspaceDetailsService.getPages();
  }

  /**
   * Returns workspace details section.
   *
   * @returns {*}
   */
  getSections(): any {
    return this.workspaceDetailsService.getSections();
  }

  /**
   * Callback when workspace config has been changed in editor.
   *
   * @param config {che.IWorkspaceConfig} workspace config
   */
  updateWorkspaceConfigImport(config: che.IWorkspaceConfig): void {
    if (!config) {
      return;
    }

    if (this.newName !== config.name) {
      this.newName = config.name;
    }
    if (!config.environments[config.defaultEnv]) {
      return;
    }
    this.workspaceDetails.config = config;
    this.workspaceImportedRecipe = config.environments[config.defaultEnv].recipe;
    this.switchEditMode();
  }

  /**
   * Callback when environment has been changed.
   */
  updateWorkspaceConfigEnvironment(): void {
    this.workspaceImportedRecipe = null;
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
    this.workspaceDetails = angular.copy(this.copyWorkspaceDetails);
    this.updateWorkspaceData();
  }

  /**
   * Updates workspace info.
   *
   * @returns {ng.IPromise<any>}
   */
  doUpdateWorkspace(): ng.IPromise<any> {
    delete this.workspaceDetails.links;

    let promise = this.cheWorkspace.updateWorkspace(this.workspaceId, this.workspaceDetails);
    promise.then((data: any) => {
      this.workspaceName = data.config.name;
      this.workspaceDetails = this.cheWorkspace.getWorkspaceByName(this.namespaceId, this.workspaceName);
      this.updateWorkspaceData();
      this.cheNotification.showInfo('Workspace updated.');
      return this.$location.path('/workspace/' + this.namespaceId + '/' + this.workspaceName);
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
    let name: string,
      iterations: number = 100;
    while (iterations--) {
      /* tslint:disable */
      name = 'wksp-' + (('0000' + (Math.random() * Math.pow(36, 4) << 0).toString(36)).slice(-4));
      /* tslint:enable */
      if (this.usedNamesList.indexOf(name) === -1) {
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
    let creationPromise = this.cheWorkspace.createWorkspaceFromConfig(this.namespaceId, this.workspaceDetails.config, attributes);
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
        this.$location.path('/workspace/' + workspaceData.namespace + '/' + workspaceData.config.name);
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

  // perform workspace deletion.
  deleteWorkspace(event: MouseEvent): void {
    let content = 'Would you like to delete workspace \'' + this.workspaceDetails.config.name + '\'?';
    this.confirmDialogService.showConfirmDialog('Delete workspace', content, 'Delete').then(() => {
      if (this.getWorkspaceStatus() === 'RUNNING') {
        this.cheWorkspace.stopWorkspace(this.workspaceId, false);
      }

      this.cheWorkspace.fetchStatusChange(this.workspaceId, 'STOPPED').then(() => {
        this.removeWorkspace();
      });
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
    promise.catch((error: any) => {
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
    let createSnapshot: boolean;
    if (this.getWorkspaceStatus() === 'STARTING') {
      createSnapshot = false;
    } else {
      createSnapshot = this.getAutoSnapshot();
    }
    let promise = this.cheWorkspace.stopWorkspace(this.workspaceId, createSnapshot);

    promise.catch((error: any) => {
      this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Stop workspace failed.');
      this.$log.error(error);
    });
  }

  /**
   * Register form for corresponding tab.
   *
   * @param tabIndex {string}
   * @param form
   */
  setForm(tabIndex: string, form: ng.IFormController): void {
    this.forms.set(tabIndex, form);
  }

  /**
   * Returns false if all forms from specified tabs are valid
   *
   * @param tabIndex {string}
   * @returns {Boolean}
   */
  checkFormsNotValid(tabIndex: string): boolean {
    const form = this.forms.get(tabIndex);
    return form && form.$invalid;
  }

  /**
   * Returns true when 'Save' button should be disabled
   *
   * @returns {boolean}
   */
  isSaveButtonDisabled(): boolean {
    const tabs = Object.keys(this.tab).filter((tabKey: string) => {
      return !isNaN(parseInt(tabKey, 10));
    });

    return tabs.some((tabKey: string) => this.checkFormsNotValid(tabKey)) || this.isDisableWorkspaceCreation();
  }

  /**
   * Returns true when specified tab should be disabled.
   *
   * @param tabIndex {Number}
   * @returns {boolean}
   */
  isTabDisabled(tabIndex: number): boolean {
    if (tabIndex === (<any>this.tab).Settings) {
      // never disable 'Settings' tab
      return false;
    }

    // activate tab which contains invalid form
    // to let user see the problem
    if (this.checkFormsNotValid((<any>this.tab).Settings)) {
      this.selectedTabIndex = (<any>this.tab).Settings;
    } else if (this.checkFormsNotValid((<any>this.tab).Runtime)) {
      this.selectedTabIndex = (<any>this.tab).Runtime;
    }

    if (tabIndex === (<any>this.tab).Runtime) {
      return this.checkFormsNotValid((<any>this.tab).Settings);
    } else {
      return this.checkFormsNotValid((<any>this.tab).Settings) || this.checkFormsNotValid((<any>this.tab).Runtime);
    }
  }

  /**
   * Returns namespaces empty message if set.
   *
   * @returns {string}
   */
  getNamespaceEmptyMessage(): string {
    return this.cheNamespaceRegistry.getEmptyMessage();
  }

  /**
   * Returns namespaces caption.
   *
   * @returns {string}
   */
  getNamespaceCaption(): string {
    return this.cheNamespaceRegistry.getCaption();
  }

  /**
   * Returns namespaces additional information.
   *
   * @returns {()=>Function}
   */
  getNamespaceAdditionalInfo(): Function {
    return this.cheNamespaceRegistry.getAdditionalInfo;
  }

  /**
   * Returns whether workspace creation should be disabled based on namespaces.
   *
   * @returns {boolean|string}
   */
  isDisableWorkspaceCreation(): boolean {
    const namespaces = this.cheNamespaceRegistry.getNamespaces();
    return this.isCreationFlow && (!namespaces || namespaces.length === 0) && this.cheNamespaceRegistry.getEmptyMessage() !== null;
  }
}

