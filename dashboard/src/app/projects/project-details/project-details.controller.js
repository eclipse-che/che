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
  constructor($route, $location, cheAPI, $mdDialog, cheNotification) {
    this.cheNotification = cheNotification;
    this.cheAPI = cheAPI;
    this.$mdDialog = $mdDialog;
    this.$location = $location;

    this.workspaceId = $route.current.params.workspaceId;
    this.projectPath = '/' + $route.current.params.projectName;

    this.loading = true;

    this.askedName = null;
    this.askedDescription = null;

    this.workspacesById = cheAPI.getWorkspace().getWorkspacesById();
    cheAPI.getWorkspace().fetchWorkspaces();

    if (!this.cheAPI.getProject().getProjectDetailsByKey(this.workspaceId, this.projectPath)) {
      let promise = this.cheAPI.getProject().fetchProjectDetails(this.workspaceId, this.projectPath);

      promise.then(() => {
        this.updateProjectDetails();
      }, (error) => {
        if (error.status === 304) {
          this.updateProjectDetails();
        } else if (error.status === 503) {
          this.missingWorkspace = true;
          this.loading = false;
        } else {
          this.loading = false;
          this.invalidProject = error.statusText + error.status;
        }
      });
    } else {
      this.updateProjectDetails();
    }

    //this.toolbarIcons = [{name: 'favorite', font: 'material-design icon-ic_star_24px'},
    //  {name: 'share', font: 'material-design icon-ic_share_24px'}
    //];
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
    this.projectDetails = this.cheAPI.getProject().getProjectDetailsByKey(this.workspaceId, this.projectPath);
    this.askedName = angular.copy(this.projectDetails.name);
    this.askedDescription = angular.copy(this.projectDetails.description);
    this.loading = false;
  }

  updateLocation() {
    if (this.$location.path().endsWith(this.projectDetails.name)) {
      return;
    }
    this.$location.path('/project/' + this.projectDetails.workspaceId + '/' + this.projectDetails.name);
  }

  setProjectDetails(projectDetails) {
    let promise = this.cheAPI.getProject().updateProjectDetails(projectDetails);

    promise.then(() => {
      this.cheNotification.showInfo('Project information successfully updated.');
      this.updateLocation();
      if (this.isNameChanged()) {
        this.cheAPI.getProject().fetchProjectDetails(this.workspaceId, this.projectPath).then(() => {
          this.updateProjectDetails();
        });
      } else {
        this.askedDescription = projectDetails.description;
      }
    }, (error) => {
      this.projectDetails.description = this.askedDescription;
      this.cheNotification.showError(error.data.message ? error.data.message : 'Update information failed.');
      console.log('error', error);
    });

  }

  isNameChanged() {
    if (this.projectDetails) {
      return this.askedName !== this.projectDetails.name;
    } else {
      return false;
    }
  }

  isDescriptionChanged() {
    if (this.projectDetails) {
      return this.askedDescription !== this.projectDetails.description;
    } else {
      return false;
    }
  }

  updateInfo(isInputFormValid) {
    if (!isInputFormValid || !(this.isNameChanged() || this.isDescriptionChanged())) {
      return;
    }

    if (this.isNameChanged()) {
      let promise = this.cheAPI.getProject().rename(this.projectDetails.workspaceId, this.askedName, this.projectDetails.name);

      promise.then(() => {
        this.cheAPI.getProject().removeProjectDetailsByKey(this.workspaceId, this.projectPath);
        this.cheAPI.getProject().fetchProjectsForWorkspaceId(this.workspaceId);
        if (!this.isDescriptionChanged()) {
          this.cheNotification.showInfo('Project information successfully updated.');
          this.updateLocation();
          this.cheAPI.getProject().fetchProjectDetails(this.workspaceId, this.projectPath).then(() => {
            this.updateProjectDetails();
          });
        } else {
          this.setProjectDetails(this.projectDetails);
        }
      }, (error) => {
        this.projectDetails.name = this.askedName;
        this.cheNotification.showError(error.data.message ? error.data.message : 'Update information failed.');
        console.log('error', error);
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
      let promise = this.cheAPI.getProject().remove(this.projectDetails.workspaceId, this.projectDetails.name);
      promise.then(() => {
        this.$location.path('/projects');
      }, (error) => {
        console.log('error', error);
      });
    });
  }

}
