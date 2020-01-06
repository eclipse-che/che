/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {CheNotification} from '../../../../../components/notification/che-notification.factory';
import {CheRemote} from '../../../../../components/api/remote/che-remote.factory';
import { WorkspaceDataManager } from '../../../../../components/api/workspace/workspace-data-manager';

/**
 * @ngdoc controller
 * @name workspace.export.controller:ExportWorkspaceDialogController
 * @description This class is handling the controller for the dialog box about the export of workspace
 * @author Florent Benoit
 */
export class ExportWorkspaceDialogController {

  static $inject = [
    '$filter',
    '$log',
    '$mdDialog',
    '$q',
    '$scope',
    '$window',
    'cheNotification',
    'cheRemote',
    'lodash',
  ];

  private $filter: ng.IFilterService;
  private $log: ng.ILogService;
  private $mdDialog: ng.material.IDialogService;
  private $q: ng.IQService;
  private $window: ng.IWindowService;
  private cheNotification: CheNotification;
  private cheRemote: CheRemote;
  private lodash: any;

  private workspaceDataManager: WorkspaceDataManager;

  private editorOptions: any;
  private destination: string;
  private privateCloudUrl: string;
  private privateCloudLogin: string;
  private privateCloudPassword: string;
  private importInProgress: boolean;

  private copyOfConfig: any;
  private exportConfigContent: any;
  private workspaceDetails: any;
  private exportInCloudSteps: string;

  /**
   * Default constructor that is using resource
   */
  constructor(
    $filter: ng.IFilterService,
    $log: ng.ILogService,
    $mdDialog: ng.material.IDialogService,
    $q: ng.IQService,
    $scope: ng.IScope,
    $window: ng.IWindowService,
    cheNotification: CheNotification,
    cheRemote: CheRemote,
    lodash: any,
  ) {
    this.$filter = $filter;
    this.$log = $log;
    this.$mdDialog = $mdDialog;
    this.$q = $q;
    this.$window = $window;
    this.cheNotification = cheNotification;
    this.cheRemote = cheRemote;
    this.lodash = lodash;

    this.workspaceDataManager = new WorkspaceDataManager();

    this.editorOptions = {
      wordWrap : 'on',
      lineNumbers: 'off',
      matchBrackets: true,
      readOnly: true,
      mode: 'application/json'
    };

    this.privateCloudUrl = '';
    this.privateCloudLogin = '';
    this.privateCloudPassword = '';
    this.importInProgress = false;

    this.copyOfConfig = this.getCopyOfConfig();
    this.exportConfigContent = this.$filter('json')(angular.fromJson(this.copyOfConfig), 2);

    ($scope as any).selectedIndex = this.destination === 'file' ? 0 : 1;
  }

  $onInit(): void {
    // this method won't be called here
    // place all initialization code in constructor
  }

  /**
   * It will hide the dialog box.
   */
  hide() {
    this.$mdDialog.hide();
  }

  /**
   * Returns copy of workspace's config without unnecessary properties
   * @returns {*}
   */
  getCopyOfConfig() {
    if (this.workspaceDetails.config) {
      let copyOfConfig = angular.copy(this.workspaceDetails.config);
      return this.removeLinks(copyOfConfig);
    } else if (this.workspaceDetails.devfile) {
      return this.workspaceDetails.devfile;
    }
  }

  /**
   * Recursively remove 'links' property from object
   *
   * @param object {Object} object to iterate
   * @returns {*}
   */
  removeLinks(object: any) {
    delete object.links;

    return this.lodash.forEach(object, (value: any) => {
      if (angular.isObject(value)) {
        return this.removeLinks(value);
      } else {
        return value;
      }
    });
  }

  /**
   * Provide ability to download workspace's config
   */
  downloadConfig() {
    this.$window.open('data:text/csv,' + encodeURIComponent(this.exportConfigContent));
  }

  /**
   * Start the process to export to the private Cloud
   */
  exportToPrivateCloud() {
    this.exportInCloudSteps = '';
    this.importInProgress = true;
    let login = this.cheRemote.newAuth(this.privateCloudUrl, this.privateCloudLogin, this.privateCloudPassword);

    login.then((authData: any) => {
      let copyOfConfig = angular.copy(this.copyOfConfig);
      copyOfConfig.name = 'import-' + copyOfConfig.name;
      this.exportToPrivateCloudWorkspace(copyOfConfig, authData);
      // get content of the recipe
    }, (error: any) => {
      this.handleError(error);
    });
  }

  /**
   * Export the given workspace using authentication data provided and using specified recipe content
   * @param recipeScriptContent the content of the machine configuration
   * @param workspaceConfig the workspace configuration to use
   * @param authData the data including token to deal with remote server
   */
  exportToPrivateCloudWorkspace(workspaceConfig: any, authData: any) {
    let remoteWorkspaceAPI = this.cheRemote.newWorkspace(authData);
    this.exportInCloudSteps += 'Creating remote workspace...';
    let createWorkspacePromise = remoteWorkspaceAPI.createWorkspaceFromConfig(workspaceConfig);
    createWorkspacePromise.then((remoteWorkspace : any) => {
      this.exportInCloudSteps += 'ok !<br>';
      this.finishWorkspaceExporting(remoteWorkspace);
    }, (error: any) => {
      this.handleError(error);
    });
  }

  /**
   * Finilize the Workspace exporting - show proper messages, close popup.
   *
   * @param remoteWorkspace the remote exported workspace
   */
  finishWorkspaceExporting(remoteWorkspace: che.IWorkspace) {
    this.exportInCloudSteps += 'Export of workspace ' + this.workspaceDataManager.getName(remoteWorkspace) + 'finished <br>';
    this.cheNotification.showInfo('Successfully exported the workspace to ' + this.workspaceDataManager.getName(remoteWorkspace) + ' on ' + this.privateCloudUrl);
    this.hide();
  }

  /**
   * Notify user about the error.
   * @param error the error message to display
   */
  handleError(error: any) {
    this.importInProgress = false;
    var message;
    if (error && error.data) {
      if (error.data.message) {
        message = error.data.message;
      } else {
        message = error.data;
      }
    } else if (error && error.config && error.config.url) {
      message = 'unable to connect to ' + error.config.url;
    }
    this.cheNotification.showError('Exporting workspace failed: ' + message);
    this.$log.error('error', message, error);
  }
}
