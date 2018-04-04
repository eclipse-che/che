/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {ConfirmDialogService} from '../../../../../components/service/confirm-dialog/confirm-dialog.service';
import {CheNotification} from '../../../../../components/notification/che-notification.factory';
import {CheAPI} from '../../../../../components/api/che-api.factory';
import {CheProject} from '../../../../../components/api/che-project';

/**
 * Controller for a project details
 * @author Florent Benoit
 * @author Oleksii Orel
 */
export class ProjectDetailsController {

  static $inject = ['$scope', '$log', '$route', '$location', '$timeout', 'cheAPI', 'confirmDialogService', 'cheNotification', 'lodash'];

  private $log: ng.ILogService;
  private cheNotification: CheNotification;
  private cheAPI: CheAPI;
  private $location: ng.ILocationService;
  private lodash: any;
  private $timeout: ng.ITimeoutService;
  private confirmDialogService: ConfirmDialogService;

  private namespace: string;
  private workspaceName: string;
  private projectName: string;
  private projectPath: string;
  private loading: boolean = true;
  private workspace: che.IWorkspace;
  private timeoutPromise: ng.IPromise<any>;
  private projectService: CheProject;
  private invalidProject: boolean;
  private projectDetails: che.IProjectTemplate;
  private projectDescription: string;

  /**
   * Default constructor that is using resource injection
   */
  constructor($scope: ng.IScope,
              $log: ng.ILogService,
              $route: ng.route.IRouteService,
              $location: ng.ILocationService,
              $timeout: ng.ITimeoutService,
              cheAPI: CheAPI,
              confirmDialogService: ConfirmDialogService,
              cheNotification: CheNotification,
              lodash: any) {
    this.$log = $log;
    this.cheNotification = cheNotification;
    this.cheAPI = cheAPI;
    this.$location = $location;
    this.lodash = lodash;
    this.$timeout = $timeout;
    this.confirmDialogService =  confirmDialogService;

    this.namespace = $route.current.params.namespace;
    this.workspaceName = $route.current.params.workspaceName;
    this.projectName = $route.current.params.projectName;
    this.projectPath = '/' + this.projectName;

    this.loading = true;

    cheAPI.getWorkspace().fetchWorkspaces();

    this.workspace = cheAPI.getWorkspace().getWorkspaceByName(this.namespace, this.workspaceName);

    if (!this.workspace || !this.workspace.runtime) {
      cheAPI.getWorkspace().fetchWorkspaceDetails(this.namespace + ':' + this.workspaceName).then(() => {
        this.workspace = cheAPI.getWorkspace().getWorkspaceByName(this.namespace, this.workspaceName);
        if (this.workspace && this.workspace.runtime) {
         this.fetchProjectDetails();
        } else {
          this.loading = false;
        }
      }, (error: any) => {
        this.cheNotification.showError(error.data.message ? error.data.message : 'Failed to get runtime of the project workspace.');
        this.$log.log('error', error);
      });
    } else {
      this.fetchProjectDetails();
    }

    $scope.$on('$destroy', () => {
      if (this.timeoutPromise) {
        $timeout.cancel(this.timeoutPromise);
      }
    });
  }

  fetchProjectDetails(): void {
    this.loading = true;

    if (this.workspace.status !== 'STARTING' && this.workspace.status !== 'RUNNING') {
      this.loading = false;
      return;
    }

    this.cheAPI.getWorkspace().fetchStatusChange(this.workspace.id, 'RUNNING').then(() => {
      return this.cheAPI.getWorkspace().fetchWorkspaceDetails(this.workspace.id);
    }).then(() => {

      this.projectService = this.cheAPI.getWorkspace().getWorkspaceAgent(this.workspace.id).getProject();

      if (this.projectService.getProjectDetailsByKey(this.projectPath)) {
        this.loading = false;
        this.updateProjectDetails();
      } else {
        this.projectService.fetchProjectDetails(this.workspace.id, this.projectPath).then(() => {
          this.loading = false;
          this.updateProjectDetails();
        }, (error: any) => {
          if (error.status === 304) {
            this.loading = false;
            this.updateProjectDetails();
          } else {
            this.$log.error(error);
            this.loading = false;
            this.invalidProject = error.statusText + error.status;
          }
        });
      }
    }, (error: any) => {
      this.$log.error(error);
      this.loading = false;
    });
  }

  updateProjectDetails(): void {
    this.projectDetails = this.projectService.getProjectDetailsByKey(this.projectPath);
    this.projectName = angular.copy(this.projectDetails.name);
    this.projectDescription = angular.copy(this.projectDetails.description);
    this.loading = false;
  }

  updateLocation(): void {
    if (this.$location.path().endsWith(this.projectDetails.name)) {
      return;
    }
    this.$location.path('/workspace/' + this.namespace + '/' + this.workspaceName + '/' + this.projectDetails.name);
  }

  setProjectDetails(projectDetails: che.IProjectTemplate): void {
    projectDetails.description = this.projectDescription;
    let promise = this.projectService.updateProjectDetails(projectDetails);

    promise.then(() => {
      this.cheNotification.showInfo('Project information successfully updated.');
      this.updateLocation();
      if (this.isNameChanged()) {
        this.projectService.fetchProjectDetails(this.workspace.id, this.projectPath).then(() => {
          this.updateProjectDetails();
        });
      } else {
        this.projectDescription = projectDetails.description;
      }
    }, (error: any) => {
      this.projectDetails.description = this.projectDescription;
      this.cheNotification.showError(error.data.message ? error.data.message : 'Update information failed.');
      this.$log.log('error', error);
    });

  }

  isNameChanged(): boolean {
    if (this.projectDetails) {
      return this.projectName !== this.projectDetails.name;
    } else {
      return false;
    }
  }

  isDescriptionChanged(): boolean {
    if (this.projectDetails) {
      return this.projectDescription !== this.projectDetails.description;
    } else {
      return false;
    }
  }

  updateInfo(isInputFormValid: boolean): void {
    this.$timeout.cancel(this.timeoutPromise);

    if (!isInputFormValid || !(this.isNameChanged() || this.isDescriptionChanged())) {
      return;
    }

    this.timeoutPromise = this.$timeout(() => {
      this.doUpdateInfo();
    }, 500);
  }

  doUpdateInfo(): void {
    if (this.isNameChanged()) {
      let promise = this.projectService.rename(this.projectDetails.name, this.projectName);

      promise.then(() => {
        this.projectService.removeProjectDetailsByKey(this.projectPath);
        if (!this.isDescriptionChanged()) {
          this.cheNotification.showInfo('Project information successfully updated.');
          this.updateLocation();
          this.projectService.fetchProjectDetails(this.workspace.id, this.projectPath).then(() => {
            this.updateProjectDetails();
          });
        } else {
          this.setProjectDetails(this.projectDetails);
        }
      }, (error: any) => {
        this.projectDetails.name = this.projectName;
        this.cheNotification.showError(error.data.message ? error.data.message : 'Update information failed.');
        this.$log.log('error', error);
      });
    } else {
      this.setProjectDetails(this.projectDetails);
    }
  }

  deleteProject(): void {
    let content = 'Would you like to delete the project \'' + this.projectDetails.name + '\' ?';
    this.confirmDialogService.showConfirmDialog('Remove project', content, 'Delete').then(() => {
      // remove it !
      let promise = this.projectService.remove(this.projectDetails.name);
      promise.then(() => {
       this.cheAPI.getWorkspace().fetchWorkspaceDetails(this.workspace.namespace + ':' + this.workspace.config.name).finally(() => {
         this.$location.path('/workspace/' + this.workspace.namespace + '/' + this.workspace.config.name).search({tab: 'Projects'});
       });
      }, (error: any) => {
        this.$log.log('error', error);
      });
    });
  }

  /**
   * Returns list of projects of current workspace excluding current project
   * @returns {*|Array}
   */
  getWorkspaceProjects(): Array<che.IProject> {
    let projects = this.cheAPI.getWorkspace().getWorkspaceProjects()[this.workspace.id];
    let _projects = this.lodash.filter(projects, (project: che.IProject) => {
      return project.name !== this.projectName;
    });
    return _projects;
  }

  /**
   * Returns current status of workspace
   * @returns {String}
   */
  getWorkspaceStatus(): string {
    if (!this.workspace) {
      return 'unknown';
    }
    let workspace = this.cheAPI.getWorkspace().getWorkspaceById(this.workspace.id);
    return workspace ? workspace.status : 'unknown';
  }
}
