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
export class WorkspaceDetailsController {
  $location;
  $log;
  $mdDialog;
  $q;
  $rootScope: angular.IRootScopeService;
  $route;
  $scope: angular.IScope;
  $timeout;
  cheNotification;
  cheWorkspace;
  ideSvc;
  lodash;
  workspaceDetailsService;

  workspaceDetails;
  copyWorkspaceDetails;
  machinesViewStatus;
  namespace: string;
  workspaceId: string;
  workspaceName: string;
  newName: string;
  workspaceKey: string;
  editMode: boolean;
  showApplyMessage: boolean;
  loading: boolean;
  timeoutPromise: Promise<any>;
  invalidWorkspace: string;
  selectedTabIndex;
  errorMessage: string;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($scope, $rootScope, $route, $location, cheWorkspace, $mdDialog, cheNotification, ideSvc, $log, workspaceDetailsService, lodash, $q, $timeout) {
    this.$rootScope = $rootScope;
    this.cheNotification = cheNotification;
    this.cheWorkspace = cheWorkspace;
    this.$mdDialog = $mdDialog;
    this.$location = $location;
    this.ideSvc = ideSvc;
    this.$log = $log;
    this.workspaceDetailsService = workspaceDetailsService;
    this.lodash = lodash;
    this.$q = $q;
    this.$timeout = $timeout;

    this.workspaceDetails = {};
    this.copyWorkspaceDetails = {};
    this.machinesViewStatus = {};
    this.namespace = $route.current.params.namespace;
    this.workspaceName = $route.current.params.workspaceName;
    this.workspaceKey = this.namespace + ":" + this.workspaceName;
    this.editMode = false;
    this.showApplyMessage = false;

    this.loading = true;
    this.timeoutPromise;
    $scope.$on('$destroy', () => {
      if (this.timeoutPromise) {
        $timeout.cancel(this.timeoutPromise);
      }
    });

    if (!this.cheWorkspace.getWorkspaceByName(this.namespace, this.workspaceName)) {
      let promise = this.cheWorkspace.fetchWorkspaceDetails(this.workspaceKey);
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

    this.cheWorkspace.fetchWorkspaces();

    //search the selected page
    let page = $route.current.params.page;
    if (!page) {
      $location.path('/workspace/' + this.namespace + '/' + this.workspaceName);
    } else {
      switch (page) {
        case 'info':
          this.selectedTabIndex = 0;
          break;
        case 'projects':
          this.selectedTabIndex = 2;
          break;
        case 'share':
          this.selectedTabIndex = 3;
          break;
        default:
          $location.path('/workspace/' + this.namespace + '/' + this.workspaceName);
          break;
      }
    }
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
    this.workspaceDetails = this.cheWorkspace.getWorkspaceByName(this.namespace, this.workspaceName);
    if (this.loading) {
      this.loading = false;
    }

    angular.copy(this.workspaceDetails, this.copyWorkspaceDetails);

    this.workspaceId = this.workspaceDetails.id;
    this.newName = this.workspaceDetails.config.name;
  }

  /**
   * Returns true if name of workspace is changed.
   * @returns {boolean}
   */
  isNameChanged() {
    if (this.workspaceDetails) {
      return this.workspaceDetails.config.name !== this.newName;
    }
    return false;
  }

  /**
   * Updates name of workspace
   * @param isFormValid {boolean} true if workspaceNameForm is valid
   */
  updateName(isFormValid) {
    if (isFormValid === false || !this.isNameChanged()) {
      return;
    }

    this.copyWorkspaceDetails.config.name = this.newName;
    this.doUpdateWorkspace();
  }

  /**
   * Callback which is called after workspace config was changed
   * @returns {Promise}
   */
  updateWorkspaceConfig() {
    this.editMode = !angular.equals(this.copyWorkspaceDetails.config, this.workspaceDetails.config);

    let status = this.getWorkspaceStatus();
    if (status === 'STOPPED' || status === 'STOPPING') {
      this.showApplyMessage = false;
    } else {
      this.showApplyMessage = true;
    }

    let defer = this.$q.defer();
    defer.resolve();
    return defer.promise;
  }

  /**
   * Updates workspace info.
   */
  doUpdateWorkspace() {
    delete this.copyWorkspaceDetails.links;

    let promise = this.cheWorkspace.updateWorkspace(this.workspaceId, this.copyWorkspaceDetails);
    promise.then((data) => {
      this.workspaceName = data.config.name;
      this.updateWorkspaceData();
      this.cheNotification.showInfo('Workspace updated.');
      return this.$location.path('/workspace/' + this.namespace + '/' + this.workspaceName);
    }, (error) => {
      this.loading = false;
      this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Update workspace failed.');
      this.$log.error(error);
    });

    return promise;
  }

  /**
   * Updates workspace config and restarts workspace if it's necessary
   */
  applyConfigChanges() {
    this.editMode = false;
    this.showApplyMessage = false;

    let status = this.getWorkspaceStatus();

    if (status !== 'RUNNING' && status !== 'STARTING') {
      this.doUpdateWorkspace();
      return;
    }

    this.selectedTabIndex = 0;
    this.loading = true;

    let stoppedStatusPromise = this.cheWorkspace.fetchStatusChange(this.workspaceId, 'STOPPED');
    if (status === 'RUNNING') {
      this.stopWorkspace();
      stoppedStatusPromise.then(() => {
        return this.doUpdateWorkspace();
      }).then(() => {
        this.runWorkspace();
      });
      return;
    }

    let runningStatusPromise = this.cheWorkspace.fetchStatusChange(this.workspaceId, 'RUNNING');
    if (status === 'STARTING') {
      runningStatusPromise.then(() => {
        this.stopWorkspace();
        return stoppedStatusPromise;
      }).then(() => {
        return this.doUpdateWorkspace();
      }).then(() => {
        this.runWorkspace();
      });
    }
  }

  /**
   * Cancels workspace config changes that weren't stored
   */
  cancelConfigChanges() {
    this.editMode = false;
    this.updateWorkspaceData();
  }

  //Perform workspace deletion.
  deleteWorkspace(event) {
    let confirm = this.$mdDialog.confirm()
      .title('Would you like to delete workspace \'' + this.workspaceDetails.config.name + '\'?')
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
    delete this.errorMessage;

    let promise = this.ideSvc.startIde(this.workspaceDetails);
    promise.then(() => {}, (error) => {
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
   * Creates snapshot of workspace
   */
  createSnapshotWorkspace() {
    this.cheWorkspace.createSnapshot(this.workspaceId).then(() => {}, (error) => {
      this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Creating snapshot failed.');
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
