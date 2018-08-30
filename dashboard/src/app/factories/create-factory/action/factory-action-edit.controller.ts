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
import {FactoryActionBoxController} from './factory-action-box.controller';

/**
 * @ngdoc controller
 * @name factory.directive:FactoryActionDialogEditController
 * @description This class is handling the controller for editing action of a factory
 * @author Florent Benoit
 */
export class FactoryActionDialogEditController {

  static $inject = ['$mdDialog'];

  isName: boolean;
  isFile: boolean;
  selectedValue: { name: string; file: string };

  private $mdDialog: ng.material.IDialogService;
  private index: number;
  private callbackController: FactoryActionBoxController;

  /**
   * Default constructor that is using resource
   */
  constructor($mdDialog: ng.material.IDialogService) {
    this.$mdDialog = $mdDialog;

    this.isName = angular.isDefined(this.selectedValue.name);
    this.isFile = angular.isDefined(this.selectedValue.file);
  }

  /**
   * Callback of the edit button of the dialog.
   */
  edit() {
    this.$mdDialog.hide();
    this.callbackController.callbackEditAction(this.index, this.selectedValue);
  }


  /**
   * Callback of the cancel button of the dialog.
   */
  abort() {
    this.$mdDialog.hide();
  }
}
