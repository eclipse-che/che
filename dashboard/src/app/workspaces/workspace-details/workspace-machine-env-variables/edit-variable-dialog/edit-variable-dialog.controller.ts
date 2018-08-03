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
 * @name environment.variables.controller:EditEnvVariableDialogController
 * @description This class is handling the controller for the dialog box about adding a new environment variable or editing an existing one.
 * @author Oleksii Orel
 */
export class EditEnvVariableDialogController {

  static $inject = ['$mdDialog'];

  private $mdDialog: ng.material.IDialogService;
  private popupTitle: string;
  private toEdit: string;
  private envVariables: {
    [envVarName: string]: string
  };
  private name: string;
  private value: string;
  private usedNames: Array<string>;
  private updateEnvVariable: (name: string, value: string, oldName?: string) => void;

  /**
   * Default constructor that is using resource
   */
  constructor($mdDialog: ng.material.IDialogService) {
    this.$mdDialog = $mdDialog;

    // build list of used names
    this.usedNames = angular.isObject(this.envVariables) ? Object.keys(this.envVariables) : [];

    if (this.toEdit) {
      this.name = this.toEdit;
      this.value = this.envVariables[this.name];
      this.popupTitle = 'Edit the environment variable';
    } else {
      this.popupTitle = 'Add a new environment variable';
    }
  }

  /**
   * Check if name is unique.
   * @param {string} name environment variable name to test
   * @return {boolean} true if name is unique
   */
  isUniqueName(name: string) {
    return this.usedNames.indexOf(name) < 0 || name === this.toEdit;
  }

  /**
   * It will hide the dialog box.
   */
  hide(): void {
    this.$mdDialog.hide();
  }

  /**
   * Add new or update an existing environment variable.
   */
  saveVariable(): void {
    this.updateEnvVariable(this.name, this.value, this.toEdit);
    this.hide();
  }
}
