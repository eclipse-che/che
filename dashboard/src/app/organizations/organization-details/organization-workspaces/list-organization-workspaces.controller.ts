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
import {CheNotification} from '../../../../components/notification/che-notification.factory';
import {CheUser} from '../../../../components/api/che-user.factory';
import {CheWorkspace} from '../../../../components/api/workspace/che-workspace.factory';
import {WorkspacesService} from '../../../workspaces/workspaces.service';

/**
 * @ngdoc controller
 * @name organizations.workspaces:ListOrganizationWorkspacesController
 * @description This class is handling the controller for the list of organization's workspaces.
 * @author Oleksii Orel
 */
export class ListOrganizationWorkspacesController {

  static $inject = ['cheTeam', 'chePermissions', 'cheUser', 'cheWorkspace', 'cheNotification', 'lodash', '$mdDialog', '$q',
    '$scope', 'cheListHelperFactory', '$location', 'workspacesService'];

  /**
   * Team API interaction.
   */
  private cheTeam: che.api.ICheTeam;
  /**
   * User API interaction.
   */
  private cheUser: any;
  /**
   * Permissions API interaction.
   */
  private chePermissions: che.api.IChePermissions;
  /**
   * Workspace API interaction.
   */
  private cheWorkspace: any;
  /**
   * Service for displaying notifications.
   */
  private cheNotification: any;
  /**
   * Service for displaying dialogs.
   */
  private $mdDialog: angular.material.IDialogService;
  /**
   * Promises service.
   */
  private $q: ng.IQService;

  private lodash: any;
  /**
   * List of organization's workspaces
   */
  private workspaces: Array<any>;
  /**
   * Loading state of the page.
   */
  private isLoading: boolean;
  /**
   * Filter for workspace list.
   */
  private workspaceFilter: any;
  /**
   * Current organization data (comes from directive's scope).
   */
  private organization: any;
  /**
   * Selection and filtration helper.
   */
  private cheListHelper: che.widget.ICheListHelper;
  /**
   * Location service.
   */
  private $location: ng.ILocationService;
  /**
   * Workspaces service.
   */
  private workspacesService: WorkspacesService;

  /**
   * Default constructor that is using resource
   */
  constructor(cheTeam: che.api.ICheTeam, chePermissions: che.api.IChePermissions, cheUser: CheUser, cheWorkspace: CheWorkspace,
              cheNotification: CheNotification, lodash: any, $mdDialog: angular.material.IDialogService, $q: ng.IQService,
              $scope: ng.IScope, cheListHelperFactory: che.widget.ICheListHelperFactory, $location: ng.ILocationService, workspacesService: WorkspacesService) {
    this.cheTeam = cheTeam;
    this.cheWorkspace = cheWorkspace;
    this.cheNotification = cheNotification;
    this.chePermissions = chePermissions;
    this.cheUser = cheUser;
    this.$mdDialog = $mdDialog;
    this.$q = $q;
    this.lodash = lodash;
    this.workspacesService = workspacesService;
    this.$location = $location;

    this.workspaces = [];
    this.isLoading = false;

    this.workspaceFilter = {config: {name: ''}};
    const helperId = 'list-organization-workspaces';
    this.cheListHelper = cheListHelperFactory.getHelper(helperId);
    $scope.$on('$destroy', () => {
      cheListHelperFactory.removeHelper(helperId);
    });

    this.fetchPermissions();
  }

  /**
   * Callback when name is changed.
   *
   * @param str {string} a string to filter organization workspaces.
   */
  onSearchChanged(str: string): void {
    this.workspaceFilter.config.name = str;
    this.cheListHelper.applyFilter('name', this.workspaceFilter);
  }

  /**
   * Fetches the permissions.
   */
  fetchPermissions(): void {
    if (!this.organization) {
      return;
    }
    this.isLoading = true;
    this.chePermissions.fetchOrganizationPermissions(this.organization.id).then(() => {
      this.processPermissions();
    }, () => {
      this.cheNotification.showError('Failed to access workspaces of the ' + this.organization.name + ' organization.');
    }).finally(() => {
      this.isLoading = false;
    });
  }

  /**
   * Fetches the process permissions.
   */
  processPermissions(): void {
    let permissions = this.chePermissions.getOrganizationPermissions(this.organization.id);
    let currentUserPermissions = this.lodash.find(permissions, (permission: any) => {
      return permission.userId === this.cheUser.getUser().id;
    });

    if (currentUserPermissions && currentUserPermissions.actions.indexOf('manageWorkspaces') >= 0) {
      this.fetchWorkspacesByNamespace();
    } else {
      this.fetchWorkspaces();
    }
  }

  /**
   * Fetches the workspaces by namespace.
   */
  fetchWorkspacesByNamespace(): void {
    this.isLoading = true;
    this.cheWorkspace.fetchWorkspacesByNamespace(this.organization.qualifiedName).then(() => {
      this.workspaces = this.cheWorkspace.getWorkspacesByNamespace(this.organization.qualifiedName);
      this.cheWorkspace.fetchWorkspaces();
    }, (error: any) => {
      this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Update information failed.');
    }).finally(() => {
      this.isLoading = false;
      this.cheListHelper.setList(this.workspaces, 'id');
    });
  }

  /**
   * Fetches the list of organizations workspaces.
   */
  fetchWorkspaces(): void {
    this.isLoading = true;
    this.cheWorkspace.fetchWorkspaces().then(() => {
      this.workspaces = this.filterWorkspacesByNamespace();
    }, (error: any) => {
      this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Update information failed.');
    }).finally(() => {
      this.isLoading = false;
      this.cheListHelper.setList(this.workspaces, 'id');
    });
  }

  /**
   * Filter workspaces by namespace.
   *
   * @returns {Array<che.IWorkspace>}
   */
  filterWorkspacesByNamespace(): Array<che.IWorkspace> {
    return this.lodash.filter(this.cheWorkspace.getWorkspaces(), (workspace: che.IWorkspace) => {
      return workspace.namespace === this.organization.qualifiedName;
    });
  }

  /**
   * Delete all selected workspaces.
   */
  deleteSelectedWorkspaces(): void {
    const selectedWorkspaces = this.cheListHelper.getSelectedItems(),
      selectedWorkspaceIds = selectedWorkspaces.map((workspace: che.IWorkspace) => {
        return workspace.id;
      });

    const queueLength = selectedWorkspaceIds.length;
    if (!queueLength) {
      this.cheNotification.showError('No such workspace.');
      return;
    }

    const confirmationPromise = this.showDeleteWorkspacesConfirmation(queueLength);
    confirmationPromise.then(() => {
      const numberToDelete = queueLength;
      const deleteWorkspacePromises = [];
      let isError = false;
      let workspaceName;

      selectedWorkspaceIds.forEach((workspaceId: string) => {
        this.cheListHelper.itemsSelectionStatus[workspaceId] = false;

        let workspace = this.cheWorkspace.getWorkspaceById(workspaceId);
        workspaceName = workspace.config.name;
        let stoppedStatusPromise = this.cheWorkspace.fetchStatusChange(workspaceId, 'STOPPED');

        // stop workspace if it's status is RUNNING
        if (workspace.status === 'RUNNING') {
          this.cheWorkspace.stopWorkspace(workspaceId);
        }

        // delete stopped workspace
        let promise = stoppedStatusPromise.then(() => {
          return this.cheWorkspace.deleteWorkspaceConfig(workspaceId);
        }).catch((error: any) => {
          isError = true;
        });
        deleteWorkspacePromises.push(promise);
      });

      this.$q.all(deleteWorkspacePromises).finally(() => {
        this.processPermissions();
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
   * @param numberToDelete
   * @returns {*}
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
   * Returns `true` if default environment of workspace contains supported recipe type.
   * @param workspace {che.IWorkspace}
   * @returns {boolean}
   */
  isSupported(workspace: che.IWorkspace): boolean {
    return this.workspacesService.isSupported(workspace);
  }

  /**
   * Returns `true` if the workspace is own for current user.
   * @param workspace {che.IWorkspace}
   * @returns {boolean}
   */
  isOwnWorkspace(workspaceId: string): boolean {
    return this.cheWorkspace.getWorkspaces().findIndex((workspace: che.IWorkspace) => {
        return workspaceId === workspace.id;
      }) !== -1;
  }

  /**
   * Redirects to workspace details.
   * @param workspace {che.IWorkspace}
   * @param tab {string}
   */
  redirectToWorkspaceDetails(workspace: che.IWorkspace, tab?: string): void {
    this.$location.path('/workspace/' + workspace.namespace + '/' + workspace.config.name).search({tab: tab ? tab : 'Overview'});
  }

  /**
   * Gets workspace memory limit.
   * @param workspace {che.IWorkspace}
   * @returns {string}
   */
  getMemoryLimit(workspace: che.IWorkspace): string {
    const environment = workspace.config.environments[workspace.config.defaultEnv];
    if (!environment) {
      return '-';
    }

    let limits = this.lodash.pluck(environment.machines, 'attributes.memoryLimitBytes');
    let total = 0;
    limits.forEach((limit: number) => {
      if (limit) {
        total += limit / (1024 * 1024);
      }
    });

    return (total > 0) ? Math.round(total) + ' MB' : '-';
  }

  /**
   * Returns current status of a workspace.
   * @param {string} workspaceId a workspace ID
   * @returns {string}
   */
  getWorkspaceStatus(workspaceId: string): string {
    const workspace = this.cheWorkspace.getWorkspaceById(workspaceId);
    return workspace && workspace.status ? workspace.status : 'unknown';
  }

}
