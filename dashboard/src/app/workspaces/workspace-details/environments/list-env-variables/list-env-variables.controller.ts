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
 * @name workspace.details.controller:ListEnvVariablesController
 * @description This class is handling the controller for list of environment variables.
 * @author Oleksii Kurinnyi
 */
export class ListEnvVariablesController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($mdDialog, lodash) {
    this.$mdDialog = $mdDialog;
    this.lodash = lodash;

    this.isNoSelected = true;
    this.isBulkChecked = false;
    this.envVariablesSelectedStatus = {};
    this.envVariablesSelectedNumber = 0;
    this.envVariableOrderBy = 'name';

    this.buildVariablesList();
  }

  buildVariablesList() {
    this.envVariablesList = this.lodash.map(this.envVariables, (value, name) => {
      return {name: name, value: value};
    });
  }

  /**
   * Update environment variable selected status
   */
  updateSelectedStatus() {
    this.envVariablesSelectedNumber = 0;
    this.isBulkChecked = true;
    this.envVariablesList.forEach((envVariable) => {
      if (this.envVariablesSelectedStatus[envVariable.name]) {
        this.envVariablesSelectedNumber++;
      } else {
        this.isBulkChecked = false;
      }
    });
  }

  changeEnvVariableSelection(name) {
    this.envVariablesSelectedStatus[name] = !this.envVariablesSelectedStatus[name];
    this.updateSelectedStatus();
  }

  /**
   * Change bulk selection value
   */
  changeBulkSelection() {
    if (this.isBulkChecked) {
      this.deselectAllVariables();
      this.isBulkChecked = false;
      return;
    }
    this.selectAllVariables();
    this.isBulkChecked = true;
  }

  /**
   * Check all environment variables in list
   */
  selectAllVariables() {
    this.envVariablesSelectedNumber = this.envVariablesList.length;
    this.envVariablesList.forEach((envVariable) => {
      this.envVariablesSelectedStatus[envVariable.name] = true;
    })
  }

  /**
   * Uncheck all environment variables in list
   */
  deselectAllVariables() {
    this.envVariablesSelectedStatus = {};
    this.envVariablesSelectedNumber = 0;
  }

  updateEnvVariable(name, value) {
    this.envVariables[name] = value;
    this.envVariablesOnChange();
    this.buildVariablesList();
  }

  /**
   * Show dialog to add new environment variable
   * @param $event
   */
  showAddDialog($event) {
    this.$mdDialog.show({
      targetEvent: $event,
      controller: 'AddVariableDialogController',
      controllerAs: 'addVariableDialogController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        callbackController: this,
        variables: this.envVariables
      },
      templateUrl: 'app/workspaces/workspace-details/environments/list-env-variables/add-variable-dialog/add-variable-dialog.html'
    });
  }

  /**
   * Show dialog to edit existing environment variable
   * @param $event
   * @param name environment variable's name
   * @param value environment variable's value
   */
  showEditDialog($event, name, value) {
    this.$mdDialog.show({
      targetEvent: $event,
      controller: 'EditVariableDialogController',
      controllerAs: 'editVariableDialogController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        name: name,
        value: value,
        callbackController: this
      },
      templateUrl: 'app/workspaces/workspace-details/environments/list-env-variables/edit-variable-dialog/edit-variable-dialog.html'
    });
  }

  /**
   * Removes selected environment variables
   */
  deleteSelectedEnvVariables() {
    this.showDeleteConfirmation(this.envVariablesSelectedNumber).then(() => {
      this.lodash.forEach(this.envVariablesSelectedStatus, (value, name) => {
        delete this.envVariables[name];
      });
      this.deselectAllVariables();
      this.isBulkChecked = false;
      this.envVariablesOnChange();
      this.buildVariablesList();
    })
  }

  /**
   * Show confirmation popup before environment variable to delete
   * @param numberToDelete
   * @returns {*}
   */
  showDeleteConfirmation(numberToDelete) {
    let confirmTitle = 'Would you like to delete ';
    if (numberToDelete > 1) {
      confirmTitle += 'these ' + numberToDelete + ' variables?';
    }
    else {
      confirmTitle += 'this selected variable?';
    }
    let confirm = this.$mdDialog.confirm()
      .title(confirmTitle)
      .ariaLabel('Remove environment variables')
      .ok('Delete!')
      .cancel('Cancel')
      .clickOutsideToClose(true);

    return this.$mdDialog.show(confirm);
  }

}
