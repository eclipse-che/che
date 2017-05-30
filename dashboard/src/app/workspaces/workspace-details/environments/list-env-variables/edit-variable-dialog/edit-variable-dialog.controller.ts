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
import {ListEnvVariablesController} from '../list-env-variables.controller';

/**
 * @ngdoc controller
 * @name list.environment.variables.controller:EditVariableDialogController
 * @description This class is handling the controller for the dialog box about adding a new environment variable or editing an existing one.
 * @author Oleksii Kurinnyi
 */
export class EditVariableDialogController {
  $mdDialog: ng.material.IDialogService;

  popupTitle: string;

  toEdit: string;
  envVariables: {
    [envVarName: string]: string
  };

  name: string;
  value: string;
  usedNames: string[];
  callbackController: ListEnvVariablesController;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($mdDialog: ng.material.IDialogService) {
    this.$mdDialog = $mdDialog;

    // build list of used names
    let envVariablesCopy = angular.copy(this.envVariables);
    if (this.toEdit && envVariablesCopy[this.toEdit]) {
      delete envVariablesCopy[this.toEdit];
    }
    this.usedNames = Object.keys(envVariablesCopy);

    if (this.toEdit && this.envVariables[this.toEdit]) {
      this.name = this.toEdit;
      this.value = this.envVariables[this.name];
      this.popupTitle = 'Edit the environment variable';
    } else {
      this.popupTitle = 'Add a new environment variable';
    }
  }

  /**
   * Check if name is unique.
   *
   * @param {string} name environment variable name to test
   * @return {boolean} true if name is unique
   */
  isUniqueName(name: string) {
    return this.usedNames.indexOf(name) < 0;
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
    if (this.toEdit) {
      this.callbackController.updateEnvVariable(this.toEdit, this.name, this.value);
    } else {
      this.callbackController.addEnvVariable(this.name, this.value);
    }
    this.hide();
  }
}
