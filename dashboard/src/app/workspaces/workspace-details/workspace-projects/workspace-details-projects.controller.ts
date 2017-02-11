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
import {ConfirmDialogService} from '../../../../components/service/confirm-dialog/confirm-dialog.service';
import {CheAPI} from '../../../../components/api/che-api.factory';
import {CheNotification} from '../../../../components/notification/che-notification.factory';
import {CheWorkspace} from '../../../../components/api/che-workspace.factory';

/**
 * @ngdoc controller
 * @name workspace.details.controller:WorkspaceDetailsProjectsCtrl
 * @description This class is handling the controller for details of workspace : section projects
 * @author Ann Shumilova
 * @author Oleksii Orel
 */
export class WorkspaceDetailsProjectsCtrl {

  namespace: string;
  workspaceId: string;
  workspaceKey: string;
  workspaceName: string;
  projectFilter: any;
  profileCreationDate: any;
  projectsSelectedStatus: any;
  isNoSelected: boolean;
  isAllSelected: boolean;
  isBulkChecked: boolean;
  workspace: che.IWorkspace;
  projects: Array<che.IProject>;

  private $q: ng.IQService;
  private $log: ng.ILogService;
  private cheWorkspace: CheWorkspace;
  private cheNotification: CheNotification;
  private $mdDialog: ng.material.IDialogService;
  private confirmDialogService: ConfirmDialogService;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($route: ng.route.IRouteService, cheAPI: CheAPI, cheNotification: CheNotification, $mdDialog: ng.material.IDialogService, $log: ng.ILogService, $q: ng.IQService, confirmDialogService: ConfirmDialogService) {
    this.cheWorkspace = cheAPI.getWorkspace();
    this.cheNotification = cheNotification;
    this.$mdDialog = $mdDialog;
    this.$log = $log;
    this.$q = $q;
    this.confirmDialogService = confirmDialogService;

    this.namespace = $route.current.params.namespace;
    this.workspaceName = $route.current.params.workspaceName;
    this.workspaceKey = this.namespace + '/' + this.workspaceName;

    let preferences = cheAPI.getPreferences().getPreferences();

    this.profileCreationDate = preferences['che:created'];
    this.projectFilter = {name: ''};
    this.projectsSelectedStatus = {};
    this.isNoSelected = true;
    this.isAllSelected = false;
    this.isBulkChecked = false;

    if (!this.cheWorkspace.getWorkspaceByName(this.namespace, this.workspaceName)) {
      let promise = this.cheWorkspace.fetchWorkspaceDetails(this.workspaceKey);
      promise.then(() => {
        this.updateProjectsData();
      }, (error: any) => {
        if (error.status === 304) {
          this.updateProjectsData();
        }
      });
    } else {
      this.updateProjectsData();
    }
  }

  updateProjectsData(): void {
    this.workspace = this.cheWorkspace.getWorkspaceByName(this.namespace, this.workspaceName);
    this.projects = [];

    // filter only root projects (do not show sub-projects of multi-project item):
    this.workspace.config.projects.forEach((project: any) => {
      let path = project.path.replace('/', '');
      if (path === project.name) {
        this.projects.push(project);
      }
    });
    this.workspaceId = this.workspace.id;
  }

  /**
   * Check all projects in list
   */
  selectAllProjects(): void {
    this.projects.forEach((project: che.IProject) => {
      this.projectsSelectedStatus[project.name] = true;
    });
  }

  /**
   * Uncheck all projects in list
   */
  deselectAllProjects(): void {
    this.projects.forEach((project: che.IProject) => {
      this.projectsSelectedStatus[project.name] = false;
    });
  }

  /**
   * Change bulk selection value
   */
  changeBulkSelection(): void {
    if (this.isBulkChecked) {
      this.deselectAllProjects();
      this.isBulkChecked = false;
    } else {
      this.selectAllProjects();
      this.isBulkChecked = true;
    }
    this.updateSelectedStatus();
  }

  /**
   * Update project selected status
   */
  updateSelectedStatus(): void {
    this.isNoSelected = true;
    this.isAllSelected = true;

    this.projects.forEach((project) => {
      if (this.projectsSelectedStatus[project.name]) {
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
   * Delete all selected projects
   */
  deleteSelectedProjects(): void {
    let projectsSelectedStatusKeys = Object.keys(this.projectsSelectedStatus);
    let checkedProjectsKeys = [];

    if (!projectsSelectedStatusKeys.length) {
      this.cheNotification.showError('No such projects.');
      return;
    }

    projectsSelectedStatusKeys.forEach((key) => {
      if (this.projectsSelectedStatus[key] === true) {
        checkedProjectsKeys.push(key);
      }
    });

    let queueLength = checkedProjectsKeys.length;
    if (!queueLength) {
      this.cheNotification.showError('No such project.');
      return;
    }

    let confirmationPromise = this.showDeleteProjectsConfirmation(queueLength);
    confirmationPromise.then(() => {
      let numberToDelete = queueLength;
      let isError = false;
      let deleteProjectPromises = [];
      let currentProjectName;

      let workspaceAgent = this.cheWorkspace.getWorkspaceAgent(this.workspace.id);

      if (!workspaceAgent) {
        this.cheNotification.showError('Workspace isn\'t run. Cannot delete any project.');
        return;
      }
      let projectService = workspaceAgent.getProject();

      checkedProjectsKeys.forEach((projectName: string) => {
        currentProjectName = projectName;
        this.projectsSelectedStatus[projectName] = false;

        let promise = projectService.remove(projectName);
        promise.then(() => {
          queueLength--;
        }, (error: any) => {
          isError = true;
          this.$log.error('Cannot delete project: ', error);
        });
        deleteProjectPromises.push(promise);
      });

      this.$q.all(deleteProjectPromises).finally(() => {
        this.cheWorkspace.fetchWorkspaceDetails(this.workspaceKey).then(() => {
          this.updateProjectsData();
          this.updateSelectedStatus();
        }, (error: any) => {
          if (error.status === 304) {
            this.updateProjectsData();
          } else {
            this.$log.error(error);
          }
        });
        if (isError) {
          this.cheNotification.showError('Delete failed.');
        } else {
          if (numberToDelete === 1) {
            this.cheNotification.showInfo(currentProjectName + ' has been removed.');
          } else {
            this.cheNotification.showInfo('Selected projects have been removed.');
          }
        }
      });
    });
  }

  /**
   * Show confirmation popup before projects to delete
   * @param numberToDelete{number}
   * @returns {ng.IPromise<any>}
   */
  showDeleteProjectsConfirmation(numberToDelete: number): ng.IPromise<any> {
    let content = 'Would you like to delete ';
    if (numberToDelete > 1) {
      content += 'these ' + numberToDelete + ' projects?';
    } else {
      content += 'this selected project?';
    }

    return this.confirmDialogService.showConfirmDialog('Remove projects', content, 'Delete');
  }
}
