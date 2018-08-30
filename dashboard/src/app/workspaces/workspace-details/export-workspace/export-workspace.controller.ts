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

/**
 * @ngdoc controller
 * @name workspace.export.controller:ExportWorkspaceController
 * @description This class is handling the controller for the export of workspace
 * @author Florent Benoit
 */
export class ExportWorkspaceController {

  static $inject = ['$mdDialog'];

  $mdDialog: ng.material.IDialogService;

  workspaceId: string;
  workspaceDetails: che.IWorkspace;

  /**
   * Default constructor that is using resource
   */
  constructor($mdDialog: ng.material.IDialogService) {
    this.$mdDialog = $mdDialog;
  }

  showExport($event: MouseEvent, destination: string) {
    this.$mdDialog.show({
      targetEvent: $event,
      controller: 'ExportWorkspaceDialogController',
      controllerAs: 'exportWorkspaceDialogController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        workspaceId: this.workspaceId,
        workspaceDetails: this.workspaceDetails,
        callbackController: this,
        destination: destination
      },
      templateUrl: 'app/workspaces/workspace-details/export-workspace/dialog/export-tab-dialog.html'
    });
  }


}
