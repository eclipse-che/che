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
import {CheAPI} from '../../../components/api/che-api.factory';
import {CheNotification} from '../../../components/notification/che-notification.factory';
import {CheWorkspace} from '../../../components/api/che-workspace.factory';
import {CheNamespaceRegistry} from '../../../components/api/namespace/che-namespace-registry.factory';

/**
 * @ngdoc controller
 * @name workspaces.list.controller:ListWorkspacesCtrl
 * @description This class is handling the controller for listing the workspaces
 * @author Ann Shumilova
 */
export class ListWorkspacesCtrl {
  $q: ng.IQService;
  $log: ng.ILogService;
  $mdDialog: ng.material.IDialogService;
  cheAPI: CheAPI;
  cheNotification: CheNotification;
  cheWorkspace: CheWorkspace;

  state: string;
  isInfoLoading: boolean;
  workspaceFilter: any;
  userWorkspaces: che.IWorkspace[];

  workspacesById: Map<string, che.IWorkspace>;
  workspaceUsedResources: Map<string, string>;

  workspacesSelectedStatus: {
    [workspaceId: string]: boolean;
  };
  isAllSelected: boolean;
  isBulkChecked: boolean;
  isNoSelected: boolean;

  cheNamespaceRegistry: CheNamespaceRegistry;
  private ALL_NAMESPACES: string = 'All Teams';

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($log: ng.ILogService, $mdDialog: ng.material.IDialogService, $q: ng.IQService, $rootScope: che.IRootScopeService,
              cheAPI: CheAPI, cheNotification: CheNotification, cheWorkspace: CheWorkspace, cheNamespaceRegistry: CheNamespaceRegistry) {
    this.cheAPI = cheAPI;
    this.$q = $q;
    this.$log = $log;
    this.$mdDialog = $mdDialog;
    this.cheNotification = cheNotification;
    this.cheWorkspace = cheWorkspace;
    this.cheNamespaceRegistry = cheNamespaceRegistry;

    this.state = 'loading';
    this.isInfoLoading = true;
    this.isExactMatch = false;
    this.workspaceFilter = {config: {name: ''}};
    this.namespaceFilter = {namespace: ''};

    // map of all workspaces with additional info by id:
    this.workspacesById = new Map();
    // map of workspaces' used resources (consumed GBH):
    this.workspaceUsedResources = new Map();

    this.getUserWorkspaces();

    this.workspacesSelectedStatus = {};

    this.isBulkChecked = false;
    this.isNoSelected = true;
    $rootScope.showIDE = false;

    this.namespaces = this.getNamespaces();

    this.onFilterChanged = (value :  string) => {
      this.namespaceFilter.namespace = (value === this.ALL_NAMESPACES) ? '' : value;
      this.isExactMatch = (value === this.ALL_NAMESPACES) ? false : true;
    }
  }

  /**
   * Fetch current user's workspaces (where he is a member):
   */
  getUserWorkspaces(): void {
    // fetch workspaces when initializing
    let promise = this.cheAPI.getWorkspace().fetchWorkspaces();

    promise.then(() => {
        this.updateSharedWorkspaces();
      },
      (error: any) => {
        if (error.status === 304) {
          // ok
          this.updateSharedWorkspaces();
          return;
        }
        this.state = 'error';
        this.isInfoLoading = false;
      });
  }

  /**
   * Update the info of all user workspaces:
   */
  updateSharedWorkspaces(): void {
    this.userWorkspaces = [];
    let workspaces = this.cheAPI.getWorkspace().getWorkspaces();
    if (workspaces.length === 0) {
      this.isInfoLoading = false;
    }
    workspaces.forEach((workspace: che.IWorkspace) => {
      // first check the list of already received workspace info:
      if (!this.workspacesById.get(workspace.id)) {
        this.cheAPI.getWorkspace().fetchWorkspaceDetails(workspace.id).then(() => {
          let userWorkspace = this.cheAPI.getWorkspace().getWorkspaceById(workspace.id);
          this.getWorkspaceInfo(userWorkspace);
          this.userWorkspaces.push(userWorkspace);
        });
      } else {
        let userWorkspace = this.workspacesById.get(workspace.id);
        this.userWorkspaces.push(userWorkspace);
        this.isInfoLoading = false;
      }
    });

    this.state = 'loaded';
  }

  /**
   * Represents given account resources as a map with workspace id as a key.
   *
   * @param {any[]} resources
   */
  processUsedResources(resources: any[]): void {
    resources.forEach((resource: any) => {
      this.workspaceUsedResources.set(resource.workspaceId, resource.memory.toFixed(2));
    });
  }

  /**
   * Gets all necessary workspace info to be displayed.
   *
   * @param {che.IWorkspace} workspace
   */
  getWorkspaceInfo(workspace: che.IWorkspace): void {
    let promises = [];
    this.workspacesById.set(workspace.id, workspace);

    workspace.isLocked = false;
    workspace.usedResources = this.workspaceUsedResources.get(workspace.id);

    // no access to runner resources if workspace is locked:
    if (!workspace.isLocked) {
      let promiseWorkspace = this.cheAPI.getWorkspace().fetchWorkspaceDetails(workspace.id);
      promises.push(promiseWorkspace);
    }

    this.$q.all(promises).finally(() => {
      this.isInfoLoading = false;
    });
  }

  /**
   * return true if all workspaces in list are checked
   * @returns {boolean}
   */
  isAllWorkspacesSelected(): boolean {
    return this.isAllSelected;
  }

  /**
   * returns true if all workspaces in list are not checked
   * @returns {boolean}
   */
  isNoWorkspacesSelected(): boolean {
    return this.isNoSelected;
  }

  /**
   * Check all workspaces in list
   */
  selectAllWorkspaces(): void {
    for (let key of this.workspacesById.keys()) {
      this.workspacesSelectedStatus[key] = true;
    }
  }

  /**
   * Uncheck all workspaces in list
   */
  deselectAllWorkspaces(): void {
    Object.keys(this.workspacesSelectedStatus).forEach((key: string) => {
      this.workspacesSelectedStatus[key] = false;
    });
  }

  /**
   * Change bulk selection value
   */
  changeBulkSelection(): void {
    if (this.isBulkChecked) {
      this.deselectAllWorkspaces();
      this.isBulkChecked = false;
    } else {
      this.selectAllWorkspaces();
      this.isBulkChecked = true;
    }
    this.updateSelectedStatus();
  }

  /**
   * Update workspace selected status
   */
  updateSelectedStatus(): void {
    this.isNoSelected = true;
    this.isAllSelected = true;

    Object.keys(this.workspacesSelectedStatus).forEach((key: string) => {
      if (this.workspacesSelectedStatus[key]) {
        this.isNoSelected = false;
      } else {
        this.isAllSelected = false;
      }
    });

    if (this.isNoSelected) {
      this.isBulkChecked = false;
      return;
    }

    if (this.isAllSelected) {
      this.isBulkChecked = true;
    }
  }

  /**
   * Delete all selected workspaces
   */
  deleteSelectedWorkspaces(): void {
    let workspacesSelectedStatusKeys = Object.keys(this.workspacesSelectedStatus);
    let checkedWorkspacesKeys = [];

    if (!workspacesSelectedStatusKeys.length) {
      this.cheNotification.showError('No such workspace.');
      return;
    }

    workspacesSelectedStatusKeys.forEach((key: string) => {
      if (this.workspacesSelectedStatus[key] === true) {
        checkedWorkspacesKeys.push(key);
      }
    });

    let queueLength = checkedWorkspacesKeys.length;
    if (!queueLength) {
      this.cheNotification.showError('No such workspace.');
      return;
    }

    let confirmationPromise = this.showDeleteWorkspacesConfirmation(queueLength);
    confirmationPromise.then(() => {
      let numberToDelete = queueLength;
      let isError = false;
      let deleteWorkspacePromises = [];
      let workspaceName;

      checkedWorkspacesKeys.forEach((workspaceId: string) => {
        this.workspacesSelectedStatus[workspaceId] = false;

        let workspace = this.cheWorkspace.getWorkspaceById(workspaceId);
        workspaceName = workspace.config.name;
        let stoppedStatusPromise = this.cheWorkspace.fetchStatusChange(workspaceId, 'STOPPED');

        // stop workspace if it's status is RUNNING
        if (workspace.status === 'RUNNING') {
          this.cheWorkspace.stopWorkspace(workspaceId, false);
        }

        // delete stopped workspace
        let promise = stoppedStatusPromise.then(() => {
          return this.cheWorkspace.deleteWorkspaceConfig(workspaceId);
        }).then(() => {
            this.workspacesById.delete(workspaceId);
            queueLength--;
          },
          (error: any) => {
            isError = true;
            this.$log.error('Cannot delete workspace: ', error);
          });
        deleteWorkspacePromises.push(promise);
      });

      this.$q.all(deleteWorkspacePromises).finally(() => {
        this.getUserWorkspaces();
        this.updateSelectedStatus();
        if (isError) {
          this.cheNotification.showError('Delete failed.');
        } else {
          if (numberToDelete === 1) {
            this.cheNotification.showInfo(workspaceName + ' has been removed.');
          } else {
            this.cheNotification.showInfo('Selected workspaces have been removed.');
          }
        }
      });
    });
  }

  /**
   * Show confirmation popup before workspaces to delete
   * @param {Number} numberToDelete
   * @returns {ng.IPromise<any>}
   */
  showDeleteWorkspacesConfirmation(numberToDelete: number): ng.IPromise<any> {
    let confirmTitle = 'Would you like to delete ';
    if (numberToDelete > 1) {
      confirmTitle += 'these ' + numberToDelete + ' workspaces?';
    } else {
      confirmTitle += 'this selected workspace?';
    }
    let confirm = this.$mdDialog.confirm()
      .title(confirmTitle)
      .ariaLabel('Remove workspaces')
      .ok('Delete!')
      .cancel('Cancel')
      .clickOutsideToClose(true);

    return this.$mdDialog.show(confirm);
  }

  /**
   * Returns the list of available namespaces.
   *
   * @returns {Array} array of namespaces
   */
  getNamespaces() {
    let namespaces = this.cheNamespaceRegistry.getNamespaces();
    if (namespaces.length > 0) {
      return [this.ALL_NAMESPACES].concat(namespaces);
    }
    return namespaces;
  }
}
