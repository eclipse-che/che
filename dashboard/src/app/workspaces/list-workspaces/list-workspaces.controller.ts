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
import { CheAPI } from '../../../components/api/che-api.factory';
import { CheNotification } from '../../../components/notification/che-notification.factory';
import { CheWorkspace } from '../../../components/api/workspace/che-workspace.factory';
import { CheNamespaceRegistry } from '../../../components/api/namespace/che-namespace-registry.factory';
import { ConfirmDialogService } from '../../../components/service/confirm-dialog/confirm-dialog.service';
import { CheBranding } from '../../../components/branding/che-branding';

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
  userWorkspaces: che.IWorkspace[];
  workspaceCreationLink: string;

  workspacesById: Map<string, che.IWorkspace>;
  workspaceUsedResources: Map<string, string>;

  isExactMatch: boolean = false;
  namespaceFilter: { namespace: string };
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
    this.namespaceFilter = { namespace: '' };

    // map of all workspaces with additional info by id:
    this.workspacesById = new Map();
    // map of workspaces' used resources (consumed GBH):
    this.workspaceUsedResources = new Map();

    this.fetchUserWorkspaces();

    this.cheNamespaceRegistry.fetchNamespaces().then(() => {
      this.namespaceLabels = this.getNamespaceLabelsList();
    });

    // callback when search value is changed
    this.onSearchChanged = (str: string) => {
      this.cheListHelper.applyFilter('$', str ? str : {});
    };

    // callback when namespace is changed
    this.onFilterChanged = (label: string) => {
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

  $onInit(): void {
    // this method won't be called here
    // place all initialization code in constructor
  }

  /**
   * Fetch current user's workspaces.
   */
  fetchUserWorkspaces(): void {
    const promise = this.cheAPI.getWorkspace().fetchWorkspaces();

    promise.then(() => {
      this.userWorkspaces = this.cheAPI.getWorkspace().getWorkspaces();
      return this.$q.resolve();
    }, (error: any) => {
      if (error && error.status === 304) {
        this.userWorkspaces = this.cheAPI.getWorkspace().getWorkspaces();
        return this.$q.resolve();
      }
      this.state = 'error';
      return this.$q.reject(error);
    }).then(() => {
      this.cheListHelper.setList(this.userWorkspaces, 'id');
    }).finally(() => {
      this.isInfoLoading = false;
    });
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
   * Delete all selected workspaces
   */
  deleteSelectedWorkspaces(): void {
    const selectedWorkspaces = this.cheListHelper.getSelectedItems();

    let queueLength = selectedWorkspaces.length;
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

      selectedWorkspaces.forEach((workspace: che.IWorkspace) => {
        this.cheListHelper.itemsSelectionStatus[workspace.id] = false;

        workspaceName = this.cheWorkspace.getWorkspaceDataManager().getName(workspace);
        let stoppedStatusPromise = this.cheWorkspace.fetchStatusChange(workspace.id, 'STOPPED');

        // stop workspace if it's status is RUNNING
        if (workspace.status === 'RUNNING') {
          this.cheWorkspace.stopWorkspace(workspace.id);
        }

        // delete stopped workspace
        let promise = stoppedStatusPromise.then(() => {
          return this.cheWorkspace.deleteWorkspace(workspace.id);
        }).then(() => {
          this.workspacesById.delete(workspace.id);
          queueLength--;
        },
          (error: any) => {
            isError = true;
            this.$log.error('Cannot delete workspace: ', error);
          });
        deleteWorkspacePromises.push(promise);
      });

      this.$q.all(deleteWorkspacePromises).finally(() => {
        this.fetchUserWorkspaces();

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

    return this.confirmDialogService.showConfirmDialog('Remove workspaces', content, { resolve: 'Delete' });
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
