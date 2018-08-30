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
import {CheAPI} from '../../../components/api/che-api.factory';
import {CheNotification} from '../../../components/notification/che-notification.factory';
import {CheWorkspace} from '../../../components/api/workspace/che-workspace.factory';
import {CheNamespaceRegistry} from '../../../components/api/namespace/che-namespace-registry.factory';
import {ConfirmDialogService} from '../../../components/service/confirm-dialog/confirm-dialog.service';
import {CheBranding} from '../../../components/branding/che-branding.factory';

/**
 * @ngdoc controller
 * @name workspaces.list.controller:ListWorkspacesCtrl
 * @description This class is handling the controller for listing the workspaces
 * @author Ann Shumilova
 */
export class ListWorkspacesCtrl {

  static $inject = ['$log', '$mdDialog', '$q', 'lodash', 'cheAPI', 'cheNotification', 'cheBranding', 'cheWorkspace', 'cheNamespaceRegistry',
   'confirmDialogService', '$scope', 'cheListHelperFactory'];

   $q: ng.IQService;
  $log: ng.ILogService;
  lodash: any;
  $mdDialog: ng.material.IDialogService;
  cheAPI: CheAPI;
  cheNotification: CheNotification;
  cheWorkspace: CheWorkspace;
  cheListHelper: che.widget.ICheListHelper;

  state: string;
  isInfoLoading: boolean;
  workspaceFilter: any;
  userWorkspaces: che.IWorkspace[];
  workspaceCreationLink: string;

  workspacesById: Map<string, che.IWorkspace>;
  workspaceUsedResources: Map<string, string>;

  isExactMatch: boolean = false;
  namespaceFilter: {namespace: string};
  namespaceLabels: string[];
  onFilterChanged: Function;
  onSearchChanged: Function;

  cheNamespaceRegistry: CheNamespaceRegistry;
  private confirmDialogService: ConfirmDialogService;
  private ALL_NAMESPACES: string = 'All Teams';

  /**
   * Default constructor that is using resource
   */
  constructor($log: ng.ILogService, $mdDialog: ng.material.IDialogService, $q: ng.IQService, lodash: any,
              cheAPI: CheAPI, cheNotification: CheNotification, cheBranding: CheBranding,
              cheWorkspace: CheWorkspace, cheNamespaceRegistry: CheNamespaceRegistry,
              confirmDialogService: ConfirmDialogService, $scope: ng.IScope, cheListHelperFactory: che.widget.ICheListHelperFactory) {
    this.cheAPI = cheAPI;
    this.$q = $q;
    this.$log = $log;
    this.lodash = lodash;
    this.$mdDialog = $mdDialog;
    this.cheNotification = cheNotification;
    this.cheWorkspace = cheWorkspace;
    this.cheNamespaceRegistry = cheNamespaceRegistry;
    this.confirmDialogService = confirmDialogService;

    this.workspaceCreationLink = cheBranding.getWorkspace().creationLink;

    const helperId = 'list-workspaces';
    this.cheListHelper = cheListHelperFactory.getHelper(helperId);
    $scope.$on('$destroy', () => {
      cheListHelperFactory.removeHelper(helperId);
    });

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

    this.cheNamespaceRegistry.fetchNamespaces().then(() => {
      this.namespaceLabels = this.getNamespaceLabelsList();
    });

    // callback when search value is changed
    this.onSearchChanged = (str: string) => {
      this.workspaceFilter.config.name = str;
      this.cheListHelper.applyFilter('name', this.workspaceFilter);
    };

    // callback when namespace is changed
    this.onFilterChanged = (label :  string) => {
      if (label === this.ALL_NAMESPACES) {
        this.namespaceFilter.namespace = '';
      } else {
        let namespace = this.cheNamespaceRegistry.getNamespaces().find((namespace: che.INamespace) => {
          return namespace.label === label;
        });
        this.namespaceFilter.namespace = namespace.id;
      }
      this.isExactMatch = (label === this.ALL_NAMESPACES) ? false : true;

      this.cheListHelper.applyFilter('namespace', this.namespaceFilter, this.isExactMatch);
    };

  }

  /**
   * Fetch current user's workspaces (where he is a member):
   */
  getUserWorkspaces(): void {
    // fetch workspaces when initializing
    const promise = this.cheAPI.getWorkspace().fetchWorkspaces();

    promise.then(() => {
      return this.updateSharedWorkspaces();
    }, (error: any) => {
      if (error && error.status === 304) {
        // ok
        return this.updateSharedWorkspaces();
      }
      this.state = 'error';
      this.isInfoLoading = false;
      return this.$q.reject(error);
    }).then(() => {
      this.cheListHelper.setList(this.userWorkspaces, 'id');
    });
  }

  /**
   * Update the info of all user workspaces:
   *
   * @return {IPromise<any>}
   */
  updateSharedWorkspaces(): ng.IPromise<any> {
    this.userWorkspaces = [];
    let workspaces = this.cheAPI.getWorkspace().getWorkspaces();
    if (workspaces.length === 0) {
      this.isInfoLoading = false;
    }
    const promises: Array<ng.IPromise<any>> = [];
    workspaces.forEach((workspace: che.IWorkspace) => {
      // first check the list of already received workspace info:
      if (!this.workspacesById.get(workspace.id)) {
        const promise = this.cheWorkspace.fetchWorkspaceDetails(workspace.id)
          .catch((error: any) => {
            if (error && error.status === 304) {
              return this.$q.when();
            }
            let message = error.data && error.data.message ? ' Reason: ' + error.data.message : '';
            this.cheNotification.showError('Failed to retrieve workspace ' + workspace.config.name + ' data.' + message) ;
            return this.$q.reject(error);
          })
          .then(() => {
            let userWorkspace = this.cheAPI.getWorkspace().getWorkspaceById(workspace.id);
            this.getWorkspaceInfo(userWorkspace);
            this.userWorkspaces.push(userWorkspace);
            return this.$q.when();
          });
        promises.push(promise);
      } else {
        let userWorkspace = this.workspacesById.get(workspace.id);
        this.userWorkspaces.push(userWorkspace);
        this.isInfoLoading = false;
      }
    });

    this.state = 'loaded';

    return this.$q.all(promises);
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
   * Delete all selected workspaces
   */
  deleteSelectedWorkspaces(): void {
    const selectedWorkspaces = this.cheListHelper.getSelectedItems(),
          selectedWorkspacesIds = selectedWorkspaces.map((workspace: che.IWorkspace) => {
      return workspace.id;
    });

    let queueLength = selectedWorkspacesIds.length;
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

      selectedWorkspacesIds.forEach((workspaceId: string) => {
        this.cheListHelper.itemsSelectionStatus[workspaceId] = false;

        let workspace = this.cheWorkspace.getWorkspaceById(workspaceId);
        if (!workspace) {
          return;
        }
        workspaceName = workspace.config.name;
        let stoppedStatusPromise = this.cheWorkspace.fetchStatusChange(workspaceId, 'STOPPED');

        // stop workspace if it's status is RUNNING
        if (workspace.status === 'RUNNING') {
          this.cheWorkspace.stopWorkspace(workspaceId);
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
   * @param numberToDelete{number}
   * @returns {ng.IPromise<any>}
   */
  showDeleteWorkspacesConfirmation(numberToDelete: number): ng.IPromise<any> {
    let content = 'Would you like to delete ';
    if (numberToDelete > 1) {
      content += 'these ' + numberToDelete + ' workspaces?';
    } else {
      content += 'this selected workspace?';
    }

    return this.confirmDialogService.showConfirmDialog('Remove workspaces', content, 'Delete');
  }

  /**
   * Returns the list of labels of available namespaces.
   *
   * @returns {Array} array of namespaces
   */
  getNamespaceLabelsList(): string[] {
    let namespaces = this.lodash.pluck(this.cheNamespaceRegistry.getNamespaces(), 'label');
    if (namespaces.length > 0) {
      return [this.ALL_NAMESPACES].concat(namespaces);
    }
    return namespaces;
  }
}
