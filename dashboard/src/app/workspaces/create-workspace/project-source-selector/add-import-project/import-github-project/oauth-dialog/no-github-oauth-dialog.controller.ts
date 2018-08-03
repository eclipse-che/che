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
 * @name projects.create-project.github.oauth-dialog.controller:NoGithubOauthDialogController
 * @description This class is handling the controller for the no Github oAuth dialog
 * @author Florent Benoit
 */
export class NoGithubOauthDialogController {

  static $inject = ['$mdDialog', '$rootScope'];

  /**
   * Material's dialog service.
   */
  private $mdDialog: ng.material.IDialogService;
  private name: string;
  private message: string;

  /**
   * Default constructor that is using resource
   */
  constructor($mdDialog: ng.material.IDialogService, $rootScope: ng.IRootScopeService) {
    this.$mdDialog = $mdDialog;

    this.name = ($rootScope as any).branding.name;
    this.message = ($rootScope as any).branding.oauthDocs;
  }

  /**
   * It will hide the dialog box.
   */
  hide(): void {
    this.$mdDialog.hide();
  }
}
