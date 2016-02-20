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
 * @ngdoc controller
 * @name projects.list.controller:ListProjectsCtrl
 * @description This class is handling the controller for listing the projects
 * @author Florent Benoit
 */
export class ListProjectsCtrl {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($mdDialog, cheAPI, cheNotification) {
    this.$mdDialog = $mdDialog;
    this.cheAPI = cheAPI;
    this.cheNotification = cheNotification;

    this.filtersWorkspaceSelected = {};
    this.projectFilter = {name: ''};
    this.projectsSelectedStatus = {};
    this.workspacesById = cheAPI.getWorkspace().getWorkspacesById();



    this.isLoading = true;
    // fetch workspaces when initializing
    let promise = cheAPI.getWorkspace().fetchWorkspaces();

    promise.then(() => {
        this.updateData();
        this.isLoading = false;
      },
      (error) => {
        this.isLoading = false;
        if (error.status === 304) {
          this.updateData();
        }
      });

    let profilePreferences = cheAPI.getProfile().getPreferences();

    this.profileCreationDate = profilePreferences['che:created'];

    this.menuOptions = [
      {
        title: 'Filter Workspace',
        onclick:  () => {
          this.displayWorkspaceFilter = true;
        }
      },
      {
        title: 'Delete selected projects',
        onclick: () => {
          this.deleteSelectedProjects();
        }
      }
    ];

    // by default, the workspace filter is hidden
    this.displayWorkspaceFilter = false;

    // projects on all workspaces
    this.projects = [];
  }

  updateData() {
    this.workspaces = this.cheAPI.getWorkspace().getWorkspaces();
    this.projectsPerWorkspace = this.cheAPI.getProject().getProjectsByWorkspace();
    this.projects = this.cheAPI.getProject().getAllProjects();
    // init the filters of workspaces
    this.workspaces.forEach((workspace) => {
      this.filtersWorkspaceSelected[workspace.id] = true;
    });
  }

  /**
   * Gets the name of the workspace based on its ID
   * @param workspaceId
   * @returns {CheWorkspace.name|*}
   */
  getWorkspaceName(workspaceId) {
    return this.workspacesById.get(workspaceId).config.name;
  }

  /**
   * Hide the workspace filter menu
   */
  hideWorkspaceFilter() {
    this.displayWorkspaceFilter = false;
  }

  /**
   * All workspaces should be selected
   */
  resetWorkspaceFilter() {
    var keys = Object.keys(this.filtersWorkspaceSelected);
    keys.forEach((key) => {
      this.filtersWorkspaceSelected[key] = true;
    });
  }

  /**
   * Delete all selected projects
   * @param event
   */
  deleteSelectedProjects() {
    let projectsSelectedStatusKeys = Object.keys(this.projectsSelectedStatus);
    let checkedProjectsKeys = [];

    if (projectsSelectedStatusKeys.length) {

      projectsSelectedStatusKeys.forEach((key) => {
        if (this.projectsSelectedStatus[key] === true) {
          checkedProjectsKeys.push(key);
        }
      });
      var queueLenth = checkedProjectsKeys.length;
      if (queueLenth) {
        let confirmTitle = 'Would you like to delete ';
        if (queueLenth > 1) {
          confirmTitle += 'these ' + queueLenth + ' projects?';
        } else {
          confirmTitle += 'this selected project?';
        }
        let confirm = this.$mdDialog.confirm()
          .title(confirmTitle)
          .content('Please confirm for the removal.')
          .ariaLabel('Remove projects')
          .ok('Delete!')
          .cancel('Cancel')
          .clickOutsideToClose(true);
        this.$mdDialog.show(confirm).then(() => {
          var isError = false;
          checkedProjectsKeys.forEach((key) => {
            // remove it !
            var partsArray = key.split('/');
            if (partsArray.length === 2) {
              this.projectsSelectedStatus[key] = false;
              let promise = this.cheAPI.getProject().remove(partsArray[0], partsArray[1]);
              promise.then(() => {
                queueLenth--;
                if (!queueLenth) {
                  if (isError) {
                    this.cheNotification.showError('Delete failed.');
                  } else {
                    this.cheNotification.showInfo('Has been successfully removed.');
                  }
                }
              }, (error) => {
                queueLenth--;
                if (!queueLenth) {
                  this.cheNotification.showError('Delete failed.');
                }
                console.log('error', error);
              });
            } else {
              this.cheNotification.showError('No such project.');
            }
          });
        });
      } else {
        this.cheNotification.showError('No selected projects.');
      }
    } else {
      this.cheNotification.showError('No selected projects.');
    }
  }
}
