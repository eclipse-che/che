/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

/**
 * Controller for a project details
 * @author Florent Benoit
 * @author Oleksii Orel
 */
export class ProjectDetailsController {

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($scope, $log, $route, $location, cheAPI, $mdDialog, cheNotification, lodash, $timeout) {
    this.$log = $log;
    this.cheNotification = cheNotification;
    this.cheAPI = cheAPI;
    this.$mdDialog = $mdDialog;
    this.$location = $location;
    this.lodash = lodash;
    this.$timeout = $timeout;

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
      }, (error) => {
        this.cheNotification.showError(error.data.message ? error.data.message : 'Failed to get runtime of the project workspace.');
        this.$log.log('error', error);
      });
    } else {
      this.fetchProjectDetails();
    }

    this.timeoutPromise;
    $scope.$on('$destroy', () => {
      if (this.timeoutPromise) {
        $timeout.cancel(this.timeoutPromise);
      }
    });
  }

  fetchProjectDetails() {
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
        }, (error) => {
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
    }, (error) => {
      this.$log.error(error);
      this.loading = false;
    });
  }

  updateProjectDetails() {
    this.projectDetails = this.projectService.getProjectDetailsByKey(this.projectPath);
    this.projectName = angular.copy(this.projectDetails.name);
    this.projectDescription = angular.copy(this.projectDetails.description);
    this.loading = false;
  }

  updateLocation() {
    if (this.$location.path().endsWith(this.projectDetails.name)) {
      return;
    }
    this.$location.path('/project/' + this.namespace + '/' + this.workspaceName + '/' + this.projectDetails.name);
  }

  setProjectDetails(projectDetails) {
    projectDetails.description = this.projectDescription;
    let promise = this.projectService.updateProjectDetails(projectDetails);

    promise.then(() => {
      this.cheNotification.showInfo('Project information successfully updated.');
      this.updateLocation();
      if (this.isNameChanged()) {
        this.projectService.fetchProjectDetails(this.projectPath).then(() => {
          this.updateProjectDetails();
        });
      } else {
        this.projectDescription = projectDetails.description;
      }
    }, (error) => {
      this.projectDetails.description = this.projectDescription;
      this.cheNotification.showError(error.data.message ? error.data.message : 'Update information failed.');
      this.$log.log('error', error);
    });

  }

  isNameChanged() {
    if (this.projectDetails) {
      return this.projectName !== this.projectDetails.name;
    } else {
      return false;
    }
  }

  isDescriptionChanged() {
    if (this.projectDetails) {
      return this.projectDescription !== this.projectDetails.description;
    } else {
      return false;
    }
  }

  updateInfo(isInputFormValid) {
    this.$timeout.cancel(this.timeoutPromise);

    if (!isInputFormValid || !(this.isNameChanged() || this.isDescriptionChanged())) {
      return;
    }

    this.timeoutPromise = this.$timeout(() => {
      this.doUpdateInfo();
    }, 500);
  }

  doUpdateInfo() {
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
      }, (error) => {
        this.projectDetails.name = this.projectName;
        this.cheNotification.showError(error.data.message ? error.data.message : 'Update information failed.');
        this.$log.log('error', error);
      });
    } else {
      this.setProjectDetails(this.projectDetails);
    }
  }

  deleteProject(event) {
    var confirm = this.$mdDialog.confirm()
      .title('Would you like to delete the project ' + this.projectDetails.name)
      .content('Please confirm for the project removal.')
      .ariaLabel('Remove project')
      .ok('Delete it!')
      .cancel('Cancel')
      .clickOutsideToClose(true)
      .targetEvent(event);
    this.$mdDialog.show(confirm).then(() => {
      // remove it !
      let promise = this.projectService.remove(this.projectDetails.name);
      promise.then(() => {
        this.$location.path('/workspace/' + this.workspace.namespace + '/' + this.workspace.config.name + '/projects');
      }, (error) => {
        this.$log.log('error', error);
      });
    });
  }

  /**
   * Returns list of projects of current workspace excluding current project
   * @returns {*|Array}
   */
  getWorkspaceProjects() {
    let projects = this.cheAPI.getWorkspace().getWorkspaceProjects()[this.workspace.id];
    let _projects = this.lodash.filter(projects, (project) => { return project.name !== this.projectName});
    return _projects;
  }

  /**
   * Returns current status of workspace
   * @returns {String}
   */
  getWorkspaceStatus() {
    if (!this.workspace) {
      return 'unknown';
    }
    let workspace = this.cheAPI.getWorkspace().getWorkspaceById(this.workspace.id);
    return workspace ? workspace.status : 'unknown';
  }
}
