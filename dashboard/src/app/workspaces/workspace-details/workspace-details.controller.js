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
  constructor($rootScope, $route, $location, cheWorkspace, $mdDialog, cheNotification, ideSvc, $log, workspaceDetailsService) {
    this.$rootScope = $rootScope;
    this.cheNotification = cheNotification;
    this.cheWorkspace = cheWorkspace;
    this.$mdDialog = $mdDialog;
    this.$location = $location;
    this.ideSvc = ideSvc;
    this.$log = $log;
    this.workspaceDetailsService = workspaceDetailsService;

    this.workspaceDetails = {};
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

    // show link 'Show more' if true
    this.showShowMore = false;

    this.cheWorkspace.fetchWorkspaces();
  }

  /**
   * Returns workspace details sections (tabs, example - projects)
   * @returns {*}
   */
  getSections() {
    return this.workspaceDetailsService.getSections();
  }

  //Update the workspace data to be displayed.
  updateWorkspaceData() {
    this.workspaceDetails = this.cheWorkspace.getWorkspacesById().get(this.workspaceId);
    if (this.loading) {
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
      this.$log.error(error);
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
      if (this.workspaceDetails.status === 'STOPPED' || this.workspaceDetails.status === 'ERROR') {
        this.removeWorkspace();
      } else if (this.workspaceDetails.status === 'RUNNING') {
        this.cheWorkspace.stopWorkspace(this.workspaceId);
        this.cheWorkspace.fetchStatusChange(this.workspaceId, 'STOPPED').then(() => {
          this.removeWorkspace();
        });
      }
    });
  }

  removeWorkspace() {
    let promise = this.cheWorkspace.deleteWorkspaceConfig(this.workspaceId);

    promise.then(() => {
      this.$location.path('/workspaces');
    }, (error) => {
      this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Delete workspace failed.');
      this.$log.error(error);
    });

    return promise;
  }

  runWorkspace() {
    this.showShowMore = true;
    delete this.errorMessage;

    this.ideSvc.init();
    let promise = this.ideSvc.startIde(this.workspaceDetails);
    promise.then(() => {
      this.showShowMore = false;
    }, (error) => {
        let errorMessage;

        if (!error || !(error.data || error.error)) {
          errorMessage = 'Unable to start this workspace.';
        } else if (error.error) {
            errorMessage = error.error;
        } else if (error.data.errorCode === 10000 && error.data.attributes) {
            let attributes = error.data.attributes;

            errorMessage = 'Unable to start this workspace.' +
            ' There are ' + attributes.workspaces_count + ' running workspaces consuming ' +
            attributes.used_ram + attributes.ram_unit + ' RAM.' +
            ' Your current RAM limit is ' + attributes.limit_ram + attributes.ram_unit +
            '. This workspace requires an additional ' +
            attributes.required_ram + attributes.ram_unit + '.' +
            '  You can stop other workspaces to free resources.';
        } else {
            errorMessage = error.data.message;
        }

      this.cheNotification.showError(errorMessage);
      this.$log.error(error);

      this.errorMessage = errorMessage;
    });
  }

  stopWorkspace() {
    let promise = this.cheWorkspace.stopWorkspace(this.workspaceId);

    promise.then(() => {}, (error) => {
      this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Stop workspace failed.');
      this.$log.error(error);
    });
  }

  /**
   * Returns current status of workspace
   * @returns {String}
   */
  getWorkspaceStatus() {
    let workspace = this.cheWorkspace.getWorkspaceById(this.workspaceId);
    return workspace ? workspace.status : 'unknown';
  }
}
