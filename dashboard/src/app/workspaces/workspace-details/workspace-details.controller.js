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
 * Controller for a workspace details.
 * @author Ann Shumilova
 */
export class WorkspaceDetailsCtrl {

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($route, $location, cheWorkspace, cheAPI, $mdDialog, cheNotification) {
    this.cheNotification = cheNotification;
    this.cheAPI = cheAPI;
    this.cheWorkspace = cheWorkspace;
    this.$mdDialog = $mdDialog;
    this.$location = $location;

    this.workspaceId = $route.current.params.workspaceId;

    this.loading = true;

    if (!this.cheWorkspace.getWorkspacesById().get(this.workspaceId)) {
      let promise = this.cheWorkspace.fetchWorkspaceDetails(this.workspaceId);
      promise.then(() => {
        this.updateWorkspaceData();
      }, (error) => {
        if (error.status === 304) {
          this.updateWorkspaceData();
        } else {
          this.loading = false;
          this.invalidWorkspace = error.statusText;
        }
      });
    } else {
      this.updateWorkspaceData();
    }

    this.isRemoving = false;
  }


  //Update the workspace data to be displayed.
  updateWorkspaceData() {
    this.workspaceDetails = this.cheWorkspace.getWorkspacesById().get(this.workspaceId);
    if (this.loading) {
      this.startUpdateWorkspaceStatus();
      this.loading = false;
    }
    this.newName = angular.copy(this.workspaceDetails.config.name);
  }

  //Rename the workspace.
  renameWorkspace() {
    this.isLoading = true;

    let promise = this.cheWorkspace.fetchWorkspaceDetails(this.workspaceId);

    promise.then(() => {
      this.doRenameWorkspace();
    }, () => {
      this.doRenameWorkspace();
    });
  }

  doRenameWorkspace() {
    this.workspaceDetails = this.cheWorkspace.getWorkspacesById().get(this.workspaceId);
    let workspaceNewDetails = angular.copy(this.workspaceDetails);
    workspaceNewDetails.config.name = this.newName;
    delete workspaceNewDetails.links;

    let promise = this.cheWorkspace.updateWorkspace(this.workspaceId, workspaceNewDetails);
    promise.then((data) => {
      this.cheWorkspace.getWorkspacesById().set(this.workspaceId, data);
      this.updateWorkspaceData();
      this.cheNotification.showInfo('Workspace name is successfully updated.');
    }, (error) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Rename workspace failed.');
      console.log('error', error);
    });
  }

  //Perform workspace deletion.
  deleteWorkspace(event) {
    var confirm = this.$mdDialog.confirm()
      .title('Would you like to delete the workspace ' + this.workspaceDetails.config.name)
      .content('Please confirm for the workspace removal.')
      .ariaLabel('Delete workspace')
      .ok('Delete it!')
      .cancel('Cancel')
      .clickOutsideToClose(true)
      .targetEvent(event);
    this.$mdDialog.show(confirm).then(() => {
      if (this.workspaceDetails.status === 'STOPPED') {
        this.removeWorkspace();
      } else {
        this.isRemoving = true;
        this.stopWorkspace();
      }
    });
  }

  removeWorkspace() {
    this.isRemoving = true;

    let promise = this.cheWorkspace.deleteWorkspaceConfig(this.workspaceId);

    promise.then(() => {
      this.isRemoving = false;
      this.$location.path('/workspaces');
    }, (error) => {
      this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Delete workspace failed.');
      console.log('error', error);
    });

    return promise;
  }

  runWorkspace() {
    let promise = this.cheAPI.getWorkspace().startWorkspace(this.workspaceId, this.workspaceDetails.config.defaultEnv);

    promise.then(() => {}, (error) => {
      this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Start workspace failed.');
      console.log('error', error);
    });
  }

  stopWorkspace() {
    let promise = this.cheWorkspace.stopWorkspace(this.workspaceId);

    promise.then(() => {}, (error) => {
      this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Stop workspace failed.');
      console.log('error', error);
    });
  }

  startUpdateWorkspaceStatus() {
    let bus = this.cheAPI.getWebsocket().getBus(this.workspaceId);

    bus.subscribe('workspace:' + this.workspaceId, (message) => {
      this.workspaceDetails.status = message.eventType;
      if (message.eventType === 'STOPPED' && this.isRemoving) {
        this.removeWorkspace();
      }
    });
  }
}
