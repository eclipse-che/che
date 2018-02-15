/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

export class AfterCreationDialogController {

  static $inject = ['$mdDialog'];

  /**
   * Service for displaying dialogs.
   */
  private $mdDialog: ng.material.IDialogService;

  /**
   * Default constructor that is using resource
   */
  constructor($mdDialog: ng.material.IDialogService) {
    this.$mdDialog = $mdDialog;
  }

  /**
   * It will hide the dialog box.
   */
  close(): void {
    this.$mdDialog.cancel();
  }

  /**
   * Opens a workspace in IDE.
   */
  openWorkspace(): any {
    return this.$mdDialog.hide();
  }

  /**
   * Opens the Workspace Details page.
   */
  editWorkspace(): void {
    this.$mdDialog.cancel();
  }

}
