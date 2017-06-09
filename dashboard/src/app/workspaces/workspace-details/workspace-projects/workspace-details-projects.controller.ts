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
  workspace: che.IWorkspace;
  projects: Array<che.IProject>;

  private $q: ng.IQService;
  private $log: ng.ILogService;
  private cheWorkspace: CheWorkspace;
  private cheNotification: CheNotification;
  private $mdDialog: ng.material.IDialogService;
  private confirmDialogService: ConfirmDialogService;
  private cheListHelper: che.widget.ICheListHelper;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($route: ng.route.IRouteService, cheAPI: CheAPI, cheNotification: CheNotification, $mdDialog: ng.material.IDialogService, $log: ng.ILogService, $q: ng.IQService, confirmDialogService: ConfirmDialogService, $scope: ng.IScope, cheListHelperFactory: che.widget.ICheListHelperFactory) {
    this.cheWorkspace = cheAPI.getWorkspace();
    this.cheNotification = cheNotification;
    this.$mdDialog = $mdDialog;
    this.$log = $log;
    this.$q = $q;
    this.confirmDialogService = confirmDialogService;

    const helperId = 'workspace-details-projects';
    this.cheListHelper = cheListHelperFactory.getHelper(helperId);
    $scope.$on('$destroy', () => {
      cheListHelperFactory.removeHelper(helperId);
    });

    this.namespace = $route.current.params.namespace;
    this.workspaceName = $route.current.params.workspaceName;
    this.workspaceKey = this.namespace + '/' + this.workspaceName;

    let preferences = cheAPI.getPreferences().getPreferences();

    this.profileCreationDate = preferences['che:created'];
    this.projectFilter = {name: ''};

    let promise = this.cheWorkspace.fetchWorkspaceDetails(this.workspaceKey);
    promise.then(() => {
      this.updateProjectsData();
    }, (error: any) => {
      if (error.status === 304) {
        this.updateProjectsData();
      }
    });
  }

  /**
   * Callback when name is changed.
   *
   * @param str {string} a string to filter projects names
   */
  onSearchChanged(str: string): void {
    this.projectFilter.name = str;
    this.cheListHelper.applyFilter('name', this.projectFilter);
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

    this.cheListHelper.setList(this.projects, 'name');
  }

  /**
   * Delete all selected projects
   */
  deleteSelectedProjects(): void {
    const selectedProjects = this.cheListHelper.getSelectedItems(),
          selectedProjectsNames = selectedProjects.map((project: che.IProject) => {
            return project.name;
          });

    const queueLength = selectedProjectsNames.length;
    if (!queueLength) {
      this.cheNotification.showError('No such project.');
      return;
    }

    let confirmationPromise = this.showDeleteProjectsConfirmation(queueLength);
    confirmationPromise.then(() => {
      const numberToDelete = queueLength;
      const deleteProjectPromises = [];
      let isError = false;
      let currentProjectName;

      const workspaceAgent = this.cheWorkspace.getWorkspaceAgent(this.workspace.id);

      if (!workspaceAgent) {
        this.cheNotification.showError('Workspace isn\'t run. Cannot delete any project.');
        return;
      }
      const projectService = workspaceAgent.getProject();

      selectedProjectsNames.forEach((projectName: string) => {
        currentProjectName = projectName;
        this.cheListHelper.itemsSelectionStatus[projectName] = false;

        const promise = projectService.remove(projectName);
        promise.catch((error: any) => {
          isError = true;
          this.$log.error('Cannot delete project: ', error);
        });
        deleteProjectPromises.push(promise);
      });

      this.$q.all(deleteProjectPromises).finally(() => {
        this.cheWorkspace.fetchWorkspaceDetails(this.workspaceKey).then(() => {
          this.updateProjectsData();
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
