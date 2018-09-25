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
import {TeamDetailsService} from '../team-details.service';
/**
 * @ngdoc controller
 * @name teams.workspaces:ListTeamWorkspacesController
 * @description This class is handling the controller for the list of team's workspaces.
 * @author Ann Shumilova
 */
export class ListTeamWorkspacesController {

  static $inject = ['cheTeam', 'chePermissions', 'cheUser', 'cheWorkspace', 'cheNotification', 'lodash', '$mdDialog', '$q', 'teamDetailsService',
'$scope', 'cheListHelperFactory'];

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
   * List of team's workspaces
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
   * Current team data (comes from directive's scope).
   */
  private team: any;
  /**
   * Selection and filtration helper.
   */
  private cheListHelper: che.widget.ICheListHelper;

  /**
   * Default constructor that is using resource
   */
  constructor(cheTeam: che.api.ICheTeam, chePermissions: che.api.IChePermissions, cheUser: any, cheWorkspace: any,
              cheNotification: any, lodash: any, $mdDialog: angular.material.IDialogService, $q: ng.IQService,
              teamDetailsService: TeamDetailsService, $scope: ng.IScope, cheListHelperFactory: che.widget.ICheListHelperFactory) {
    this.cheTeam = cheTeam;
    this.cheWorkspace = cheWorkspace;
    this.cheNotification = cheNotification;
    this.chePermissions = chePermissions;
    this.cheUser = cheUser;
    this.$mdDialog = $mdDialog;
    this.$q = $q;
    this.lodash = lodash;

    this.workspaces = [];
    this.isLoading = true;

    this.workspaceFilter = {config: {name: ''}};
    const helperId = 'list-team-workspaces';
    this.cheListHelper = cheListHelperFactory.getHelper(helperId);
    $scope.$on('$destroy', () => {
      cheListHelperFactory.removeHelper(helperId);
    });

    this.team = teamDetailsService.getTeam();

    this.fetchPermissions();
  }

  /**
   * Callback when name is changed.
   *
   * @param str {string} a string to filter team workspaces.
   */
  onSearchChanged(str: string): void {
    this.workspaceFilter.config.name = str;
    this.cheListHelper.applyFilter('name', this.workspaceFilter);
  }

  fetchPermissions(): void {
    if (!this.team) {
      return;
    }
    this.chePermissions.fetchOrganizationPermissions(this.team.id).then(() => {
      this.processPermissions();
    }, (error: any) => {
      if (error && error.status === 304) {
        this.processPermissions();
      } else {
        this.cheNotification.showError('Failed to access workspaces of the ' + this.team.name + ' team.');
      }
    });
  }

  processPermissions(): void {
    let permissions = this.chePermissions.getOrganizationPermissions(this.team.id);
    let currentUserPermissions = this.lodash.find(permissions, (permission: any) => {
      return permission.userId === this.cheUser.getUser().id;
    });

    if (currentUserPermissions && currentUserPermissions.actions.indexOf('manageWorkspaces') >= 0) {
      this.fetchWorkspacesByNamespace();
    } else {
      this.fetchWorkspaces();
    }
  }

  fetchWorkspacesByNamespace(): void {
    this.cheWorkspace.fetchWorkspacesByNamespace(this.team.qualifiedName).then(() => {
      this.isLoading = false;
      this.workspaces = this.cheWorkspace.getWorkspacesByNamespace(this.team.qualifiedName);
    }, (error: any) => {
      this.isLoading = false;
      if (error.status === 304) {
        this.workspaces = this.cheWorkspace.getWorkspacesByNamespace(this.team.qualifiedName);
      }
      // todo
    }).finally(() => {
      this.cheListHelper.setList(this.workspaces, 'id');
    });
  }

  /**
   * Fetches the list of teams workspaces.
   */
  fetchWorkspaces(): void {
    let promise = this.cheWorkspace.fetchWorkspaces();

    promise.then(() => {
      this.isLoading = false;
      this.workspaces = this.filterWorkspacesByNamespace();
    }, (error: any) => {
      if (error.status === 304) {
        this.workspaces = this.filterWorkspacesByNamespace();
      }
      this.isLoading = false;
    }).finally(() => {
      this.cheListHelper.setList(this.workspaces, 'id');
    });
  }

  /**
   * Filter workspaces by namespace.
   *
   * @returns {any}
   */
  filterWorkspacesByNamespace(): Array<any> {
    let workspaces = this.cheWorkspace.getWorkspaces();
    return this.lodash.filter(workspaces, (workspace: che.IWorkspace) => {
      return workspace.namespace === this.team.qualifiedName;
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
}
