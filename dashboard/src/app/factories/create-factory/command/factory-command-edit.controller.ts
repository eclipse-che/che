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
 * @name factory.directive:FactoryCommandDialogEditController
 * @description This class is handling the controller for editing command of a factory
 * @author Florent Benoit
 */
export class FactoryCommandDialogEditController {

  static $inject = ['$mdDialog'];

  private $mdDialog: ng.material.IDialogService;
  private callbackController: any;
  private index: number;
  private selectedValue: any;

  /**
   * Default constructor that is using resource
   */
  constructor($mdDialog: ng.material.IDialogService) {
    this.$mdDialog = $mdDialog;
  }

  /**
   * Callback of the edit button of the dialog.
   */
  edit(): void {
    this.$mdDialog.hide();
    this.callbackController.callbackEditAction(this.index, this.selectedValue);
  }


  /**
   * Callback of the cancel button of the dialog.
   */
  abort(): void {
    this.$mdDialog.hide();
  }
}
