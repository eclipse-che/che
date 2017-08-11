/*
 * Copyright (c) 2015-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {CheEnvironmentRegistry} from '../../../components/api/environment/che-environment-registry.factory';
import {CheNotification} from '../../../components/notification/che-notification.factory';
import {CheWorkspace, WorkspaceStatus} from '../../../components/api/che-workspace.factory';
import IdeSvc from '../../ide/ide.service';
import {WorkspaceDetailsService} from './workspace-details.service';
import {CheUser} from '../../../components/api/che-user.factory';

/**
 * @ngdoc controller
 * @name workspaces.workspace.details.controller:WorkspaceDetailsController
 * @description This class is handling the controller for workspace to create and edit.
 * @author Ann Shumilova
 * @author Oleksii Kurinnyi
 */

const TAB: Array<string> = ['Overview', 'Projects', 'Machines', 'Agents', 'Servers', 'Env_Variables', 'Config'];

const STARTING = WorkspaceStatus[WorkspaceStatus.STARTING];
const RUNNING = WorkspaceStatus[WorkspaceStatus.RUNNING];
const STOPPING = WorkspaceStatus[WorkspaceStatus.STOPPING];
const STOPPED = WorkspaceStatus[WorkspaceStatus.STOPPED];
const SNAPSHOTTING = WorkspaceStatus[WorkspaceStatus.SNAPSHOTTING];

export class WorkspaceDetailsController {
  $location: ng.ILocationService;
  $log: ng.ILogService;
  $q: ng.IQService;
  $scope: ng.IScope;
  cheEnvironmentRegistry: CheEnvironmentRegistry;
  cheNotification: CheNotification;
  cheWorkspace: CheWorkspace;
  ideSvc: IdeSvc;
  workspaceDetailsService: WorkspaceDetailsService;
  cheUser: CheUser;
  loading: boolean = false;
  selectedTabIndex: number;
  namespaceId: string = '';
  onNamespaceChanged: Function;
  workspaceId: string = '';
  workspaceName: string = '';
  newName: string = '';
  stackId: string = '';
  workspaceDetails: any = {};
  originWorkspaceDetails: any = {};
  machinesViewStatus: any = {};
  errorMessage: string = '';
  invalidWorkspace: string = '';
  editMode: boolean = false;
  showApplyMessage: boolean = false;
  workspaceImportedRecipe: che.IRecipe;
  forms: Map<string, any> = new Map();
  tab: { [key: string]: string } = {};

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($location: ng.ILocationService, $log: ng.ILogService, $q: ng.IQService, $route: ng.route.IRouteService, $scope: ng.IScope, cheNotification: CheNotification, cheWorkspace: CheWorkspace, ideSvc: IdeSvc, workspaceDetailsService: WorkspaceDetailsService, cheUser: CheUser) {
    this.$log = $log;
    this.$location = $location;
    this.$q = $q;
    this.$scope = $scope;
    this.cheNotification = cheNotification;
    this.cheWorkspace = cheWorkspace;
    this.ideSvc = ideSvc;
    this.workspaceDetailsService = workspaceDetailsService;
    this.cheUser = cheUser;

    // prepare data
    const {namespace, workspaceName} = $route.current.params;
    this.namespaceId = namespace;
    this.workspaceName = workspaceName;
    this.updateTabs();
    this.loading = true;
    this.fetchWorkspaceDetails().then(() => {
      this.workspaceDetails = this.cheWorkspace.getWorkspaceByName(this.namespaceId, this.workspaceName);
      this.updateWorkspaceData();
    }).finally(() => {
      this.loading = false;
    });

    // watching
    const workspaceStatusDeRegistrationFn = $scope.$watch(() => {
      return this.getWorkspaceStatus();
    }, (newStatus: string, oldStatus: string) => {
      if (oldStatus === SNAPSHOTTING) {
        // status was not changed
        return;
      }
      if (newStatus === RUNNING || newStatus === STOPPED) {
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
    this.originWorkspaceDetails = angular.copy(this.workspaceDetails);
    this.switchEditMode();
  }

  /**
   * Returns current status of workspace.
   *
   * @returns {string}
   */
  getWorkspaceStatus(): string {
    const unknownStatus = 'unknown';
    const workspace = this.cheWorkspace.getWorkspaceById(this.workspaceId);
    return workspace ? workspace.status : unknownStatus;
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
    this.editMode = !angular.equals(this.originWorkspaceDetails.config, this.workspaceDetails.config);

    let status = this.getWorkspaceStatus();
    this.showApplyMessage = [STOPPED, STOPPING].indexOf(status) === -1;
  }

  saveWorkspace(): void {
    this.applyConfigChanges();
  }

  /**
   * Updates workspace config and restarts workspace if it's necessary
   */
  applyConfigChanges(): void {
    this.editMode = false;
    this.showApplyMessage = false;

    const status = this.getWorkspaceStatus();
    if ([STOPPED, STOPPING].indexOf(status) === -1) {
      this.doUpdateWorkspace();
      return;
    }

    this.selectedTabIndex = 0;
    this.loading = true;

    const stoppedStatusPromise = this.cheWorkspace.fetchStatusChange(this.workspaceId, STOPPED);
    if (status === RUNNING) {
      this.stopWorkspace();
      stoppedStatusPromise.then(() => {
        return this.doUpdateWorkspace();
      }).then(() => {
        this.runWorkspace();
      });
      return;
    }

    const runningStatusPromise = this.cheWorkspace.fetchStatusChange(this.workspaceId, RUNNING);
    if (status === STARTING) {
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
    this.workspaceDetails = angular.copy(this.originWorkspaceDetails);
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
      return this.$location.path('/workspace/' + this.namespaceId + '/' + this.workspaceName).search({tab: this.tab[this.selectedTabIndex]});
    }, (error: any) => {
      this.loading = false;
      this.cheNotification.showError('Update workspace failed.', error);
      this.$log.error(error);
    });

    return promise;
  }

  runWorkspace(): void {
    const promise = this.ideSvc.startIde(this.workspaceDetails);
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
    });
  }

  stopWorkspace(isCreateSnapshot?: boolean): void {
    if (this.getWorkspaceStatus() === STARTING) {
      isCreateSnapshot = false;
    } else if (angular.isUndefined(isCreateSnapshot)) {
      isCreateSnapshot = this.getAutoSnapshot();
    }
    let promise = this.cheWorkspace.stopWorkspace(this.workspaceId, isCreateSnapshot);

    promise.catch((error: any) => {
      this.cheNotification.showError('Stop workspace failed.', error);
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

    return tabs.some((tabKey: string) => this.checkFormsNotValid(tabKey));
  }
}

