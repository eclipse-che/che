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
import {CheAPI} from '../../../../components/api/che-api.factory';
import {CheNotification} from '../../../../components/notification/che-notification.factory';
import {WorkspacesService} from '../../../workspaces/workspaces.service';
import {WorkspaceDataManager} from '../../../../components/api/workspace/workspace-data-manager';

/**
 * Controller for creating factory from a workspace.
 * @author Oleksii Orel
 * @author Michail Kuznyetsov
 */
export class FactoryFromWorkspaceCtrl {

  static $inject = [
    '$filter',
    'cheAPI',
    'cheNotification',
    'workspacesService',
  ];

  private $filter: ng.IFilterService;
  private cheAPI: CheAPI;
  private cheNotification: CheNotification;
  private workspacesService: WorkspacesService;

  private workspaceDataManager: WorkspaceDataManager;

  private workspaces: Array<che.IWorkspace>;
  private workspacesById: Map<string, che.IWorkspace>;
  private filtersWorkspaceSelected: any;
  private workspaceFilter: any;
  private isLoading: boolean;
  private isImporting: boolean;
  private factoryContent: any;

  /**
   * Default constructor that is using resource injection
   */
  constructor(
    $filter: ng.IFilterService,
    cheAPI: CheAPI,
    cheNotification: CheNotification,
    workspacesService: WorkspacesService,
  ) {
    this.$filter = $filter;
    this.cheAPI = cheAPI;
    this.cheNotification = cheNotification;
    this.workspacesService = workspacesService;

    this.workspaceDataManager = new WorkspaceDataManager();

    this.filtersWorkspaceSelected = {};

    this.workspaceFilter = {config: {name: ''}};

    this.isLoading = true;
  }

  $onInit(): void {
    this.workspaces = this.cheAPI.getWorkspace().getWorkspaces().filter((workspace: che.IWorkspace) => {
      return this.workspacesService.isSupported(workspace);
    });
    this.workspacesById = this.cheAPI.getWorkspace().getWorkspacesById();

    // fetch workspaces when initializing
    let promise = this.cheAPI.getWorkspace().fetchWorkspaces();
    promise.then(() => {
        this.isLoading = false;
        this.updateData();
      }, (error: any) => {
        this.isLoading = false;
        if (error.status === 304) {
          this.updateData();
        }
      });
  }

  updateData(): void {
    this.setAllFiltersWorkspaces(true);
  }

  /**
   * Get factory content from workspace
   * @param {che.IWorkspace} workspace is selected workspace
   */
  getFactoryContentFromWorkspace(workspace: che.IWorkspace): void {
    let factoryContent = this.cheAPI.getFactory().getFactoryContentFromWorkspace(workspace);
    if (factoryContent) {
      this.factoryContent = this.$filter('json')(factoryContent, 2);
      return;
    }

    this.isImporting = true;

    let promise = this.cheAPI.getFactory().fetchFactoryContentFromWorkspace(workspace);

    promise.then((factoryContent: any) => {
      this.isImporting = false;
      this.factoryContent = this.$filter('json')(factoryContent, 2);
    }, (error: any) => {
      let message = (error.data && error.data.message) ? error.data.message : 'Get factory configuration failed.';
      if (error.status === 400) {
        message = 'Factory can\'t be created. The selected workspace has no projects defined. Project sources must be available from an external storage.';
      }

      this.isImporting = false;
      this.factoryContent = null;
      this.cheNotification.showError(message);
    });
  }

  /**
   * Set all workspaces in the filters of workspaces
   * @param {boolean} isChecked is setting value
   */
  setAllFiltersWorkspaces(isChecked: boolean): void {
    this.workspaces.forEach((workspace: che.IWorkspace) => {
      this.filtersWorkspaceSelected[workspace.id] = isChecked;
    });
  }

  /**
   * Get the workspace name by ID
   * @param {string} workspaceId
   * @returns {string} workspace name
   */
  getWorkspaceName(workspaceId: string): string {
    let workspace = this.workspacesById.get(workspaceId);
    if (!workspace) {
      return '';
    }
    return this.workspaceDataManager.getName(workspace) || '';
  }
}
