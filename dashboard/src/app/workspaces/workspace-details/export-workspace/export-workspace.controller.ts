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
 * @name workspace.export.controller:ExportWorkspaceController
 * @description This class is handling the controller for the export of workspace
 * @author Florent Benoit
 */
export class ExportWorkspaceController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($mdDialog) {
    this.$mdDialog = $mdDialog;
  }

  showExport($event, destination) {
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
