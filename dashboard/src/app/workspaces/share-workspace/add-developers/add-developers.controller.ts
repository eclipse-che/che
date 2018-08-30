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
import {ShareWorkspaceController} from '../share-workspace.controller';

/**
 * This class is handling the controller for sharing a private workspace with developers.
 * @author Oleksii Kurinnyi
 */
export class AddDeveloperController {

  static $inject = ['$q', '$mdDialog'];

  /**
   * Promises service.
   */
  private $q: ng.IQService;
  /**
   * Service for displaying dialogs.
   */
  private $mdDialog: ng.material.IDialogService;
  /**
   * true if user owns the workspace.
   */
  /* tslint:disable */
  private canShare: boolean;
  /* tslint:enable */
  /**
   * List of users to share the workspace.
   */
  private existingUsers: string[];
  /**
   * Parent controller.
   */
  private callbackController: ShareWorkspaceController;

  /**
   * Default constructor.
   */
  constructor($q: ng.IQService, $mdDialog: ng.material.IDialogService) {
    this.$q = $q;
    this.$mdDialog = $mdDialog;
  }

  /**
   * Callback of the cancel button of the dialog.
   */
  abort() {
    this.$mdDialog.hide();
  }

  /**
   * Callback of the share button of the dialog.
   */
  shareWorkspace() {
    let users = [];
    this.existingUsers.forEach((userId: string) => {
      users.push({userId: userId, isTeamAdmin: false});
    });

    let permissionPromises = this.callbackController.shareWorkspace(users);

    this.$q.all(permissionPromises).then(() => {
      this.$mdDialog.hide();
    });
  }
}
