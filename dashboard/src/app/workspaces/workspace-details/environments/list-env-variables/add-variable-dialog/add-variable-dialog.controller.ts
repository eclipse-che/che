/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
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
 * @name list.environment.variables.controller:AddVariableDialogController
 * @description This class is handling the controller for the dialog box about adding the environment variable.
 * @author Oleksii Kurinnyi
 */
export class AddVariableDialogController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($mdDialog) {
    this.$mdDialog = $mdDialog;

    this.name = '';
    this.value = '';
  }

  isUnique(name) {
    return !this.variables[name];
  }

  /**
   * It will hide the dialog box.
   */
  hide() {
    this.$mdDialog.hide();
  }

  /**
   * Adds new environment variable
   */
  addVariable() {
    this.callbackController.updateEnvVariable(this.name, this.value);
    this.hide();
  }

}
