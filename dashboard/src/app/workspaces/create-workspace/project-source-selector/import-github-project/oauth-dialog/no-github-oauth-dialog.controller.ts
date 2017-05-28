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

/**
 * @ngdoc controller
 * @name projects.create-project.github.oauth-dialog.controller:NoGithubOauthDialogController
 * @description This class is handling the controller for the no Github oAuth dialog
 * @author Florent Benoit
 */
export class NoGithubOauthDialogController {
  /**
   * Material's dialog service.
   */
  private $mdDialog: ng.material.IDialogService;
  /**
   * todo
   */
  private name: string;
  /**
   * todo
   */
  private message: string;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
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
