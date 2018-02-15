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

/**
 * This class is handling the data for custom confirm dialog
 *
 * @author Oleksii Orel
 */
export class ConfirmDialogService {

  static $inject = ['$mdDialog'];

  private $mdDialog: ng.material.IDialogService;

    /**
     * Default constructor that is using resource
     */
    constructor ($mdDialog: ng.material.IDialogService) {
      this.$mdDialog = $mdDialog;
    }

  /**
   * Add new section to the workspace details.
   *
   * @param title{string} popup title
   * @param content{string} dialog content
   * @param resolveButtonTitle{string} title for resolve button
   * @param rejectButtonTitle{string} title for reject button
   *
   * @returns {ng.IPromise<any>}
   */
  showConfirmDialog(title: string, content: string, resolveButtonTitle: string, rejectButtonTitle?: string): ng.IPromise<any> {
    return this.$mdDialog.show({
      bindToController: true,
      clickOutsideToClose: true,
      controller: 'CheConfirmDialogController',
      controllerAs: 'cheConfirmDialogController',
      locals: {
        content: content,
        $mdDialog: this.$mdDialog,
        title: title,
        resolveButtonTitle: resolveButtonTitle,
        rejectButtonTitle: rejectButtonTitle ? rejectButtonTitle : 'Close'
      },
      templateUrl: 'components/service/confirm-dialog/che-confirm-dialog.html'
    });
  }

}
