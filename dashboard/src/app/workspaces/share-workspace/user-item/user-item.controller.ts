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
 * Controller for a permission user item.
 *
 * @author Ann Shumilova
 */
export class UserItemController {
  static $inject = ['confirmDialogService', 'chePermissions', '$mdDialog'];

  user: { id: string; email: string; permissions: { actions: Array<string> } };

  private confirmDialogService: any;
  private chePermissions: che.api.IChePermissions;
  private $mdDialog: ng.material.IDialogService;
  private callback: ShareWorkspaceController;

  /**
   * Default constructor that is using resource injection
   */
  constructor(confirmDialogService: any, chePermissions: che.api.IChePermissions, $mdDialog: ng.material.IDialogService) {
    this.confirmDialogService = confirmDialogService;
    this.chePermissions = chePermissions;
    this.$mdDialog = $mdDialog;
  }

  /**
   * Call user permissions removal. Show the dialog.
   */
  removeUser(): void {
    let content = 'Please confirm removal for the member \'' + this.user.email + '\'.';
    let promise = this.confirmDialogService.showConfirmDialog('Remove the member', content, 'Delete');

    promise.then(() => {
      // callback is set in scope definition:
      this.callback.removePermissions(this.user);
    });
  }

  /**
   * Returns string with user actions.
   *
   * @returns {string} string format of actions array
   */
  getUserActions(): string {
    // user is set in scope definition:
    return this.user.permissions.actions.join(', ');
  }
}

