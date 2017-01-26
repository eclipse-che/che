/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';
import {CheWorkspace} from '../../../../components/api/che-workspace.factory';

/**
 * This class is handling the controller for the add secret key error notification
 * @author Oleksii Orel
 */
export class AddSecretKeyNotificationController {
  repoURL: string;
  workspaceId: string;
  workspace: che.IWorkspace;

  private $mdDialog: ng.material.IDialogService;
  private $location: ng.ILocationService;

  /**
   * Default constructor.
   * @ngInject for Dependency injection
   */
  constructor($mdDialog: ng.material.IDialogService, $location: ng.ILocationService, cheWorkspace: CheWorkspace) {
    this.$mdDialog = $mdDialog;
    this.$location = $location;

    this.workspace = cheWorkspace.getWorkspacesById().get(this.workspaceId);
  }

  /**
   * Redirect to IDE preferences.
   */
  redirectToConfig(): void {
    this.$location.path('ide/' + this.workspace.namespace + '/' + this.workspace.config.name).search({action: 'showPreferences'});
    this.$mdDialog.hide();
  }

  /**
   * Callback of the cancel button of the dialog.
   */
  hide(): void {
    this.$mdDialog.hide();
  }
}
