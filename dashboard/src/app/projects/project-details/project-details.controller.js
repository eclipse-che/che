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
export class ProjectDetailsCtrl {

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($log, $route, $location, cheAPI, $mdDialog, cheNotification) {
    this.$log = $log;
    this.cheNotification = cheNotification;
    this.cheAPI = cheAPI;
    this.$mdDialog = $mdDialog;
    this.$location = $location;

    this.workspaceId = $route.current.params.workspaceId;
    this.projectName = $route.current.params.projectName;
    this.projectPath = '/' + this.projectName;

    this.loading = true;

    this.workspacesById = cheAPI.getWorkspace().getWorkspacesById();
    cheAPI.getWorkspace().fetchWorkspaces();

    this.workspace = cheAPI.getWorkspace().getWorkspaceById(this.workspaceId);

    if (!this.workspace || !this.workspace.runtime) {
      cheAPI.getWorkspace().fetchWorkspaceDetails(this.workspaceId).then(() => {
        this.workspace = cheAPI.getWorkspace().getWorkspaceById(this.workspaceId);
        if (this.workspace && this.workspace.runtime) {
         this.fetchProjectDetails();
        } else {
          this.loading = false;
          this.noWorkspaceRuntime = true;
        }
      }, (error) => {
        this.cheNotification.showError(error.data.message ? error.data.message : 'Failed to get runtime of the project workspace.');
        this.$log.log('error', error);
      });
    } else {
      this.fetchProjectDetails();
    }
  }

  fetchProjectDetails() {
    this.projectService = this.cheAPI.getWorkspace().getWorkspaceAgent(this.workspaceId).getProject();

    if (!this.projectService.getProjectDetailsByKey(this.projectPath)) {
      let promise = this.projectService.fetchProjectDetails(this.workspaceId, this.projectPath);
      promise.then(() => {
        this.updateProjectDetails();
      }, (error) => {
        if (error.status === 304) {
          this.updateProjectDetails();
        } else {
          this.loading = false;
          this.invalidProject = error.statusText + error.status;
        }
    });
    } else {
      this.updateProjectDetails();
    }
  }

  /**
   * Gets the name of the workspace based on its ID
   * @param workspaceId
   * @returns {CheWorkspace.name|*}
   */
  getWorkspaceName(workspaceId) {
    let workspace = this.workspacesById.get(workspaceId);
    if (workspace && workspace.config) {
      return workspace.config.name;
    }
    return '';
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
    this.$location.path('/project/' + this.projectDetails.workspaceId + '/' + this.projectDetails.name);
  }

  setProjectDetails(projectDetails) {
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
    if (!isInputFormValid || !(this.isNameChanged() || this.isDescriptionChanged())) {
      return;
    }

    if (this.isNameChanged()) {
      let promise = this.projectService.rename(this.projectName, this.projectDetails.name);

      promise.then(() => {
        this.projectService.removeProjectDetailsByKey(this.projectPath);
        if (!this.isDescriptionChanged()) {
          this.cheNotification.showInfo('Project information successfully updated.');
          this.updateLocation();
          this.projectService.fetchProjectDetails(this.workspaceId, this.projectPath).then(() => {
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
        this.$location.path('/projects');
      }, (error) => {
        this.$log.log('error', error);
      });
    });
  }

}
