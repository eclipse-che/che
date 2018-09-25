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
import {ConfirmDialogService} from '../../../../../components/service/confirm-dialog/confirm-dialog.service';

interface IEnvironmentVariable {
  name: string;
  value: string;
}

/**
 * @ngdoc controller
 * @name workspace.details.controller:ListEnvVariablesController
 * @description This class is handling the controller for list of environment variables.
 * @author Oleksii Kurinnyi
 */
export class ListEnvVariablesController {
  static $inject = ['$mdDialog', 'lodash', 'confirmDialogService'];

  $mdDialog: ng.material.IDialogService;
  lodash: any;

  isNoSelected: boolean = true;
  isBulkChecked: boolean = false;
  envVariables: {
    [envVarName: string]: string
  };
  envVariablesList: IEnvironmentVariable[] = [];
  envVariablesSelectedStatus: {
    [envVarName: string]: boolean
  } = {};
  envVariablesSelectedNumber: number = 0;
  envVariableOrderBy: string = 'name';

  envVariablesOnChange: Function;

  private confirmDialogService: ConfirmDialogService;

  /**
   * Default constructor that is using resource
   */
  constructor($mdDialog: ng.material.IDialogService,
              lodash: any,
              confirmDialogService: ConfirmDialogService) {
    this.$mdDialog = $mdDialog;
    this.lodash = lodash;
    this.confirmDialogService = confirmDialogService;

    this.buildVariablesList();
  }

  buildVariablesList(): void {
    this.envVariablesList = this.lodash.map(this.envVariables, (value: string, name: string) => {
      return {name: name, value: value};
    });
  }

  /**
   * Update environment variable selected status
   */
  updateSelectedStatus(): void {
    this.envVariablesSelectedNumber = 0;
    this.isBulkChecked = true;
    this.envVariablesList.forEach((envVariable: IEnvironmentVariable) => {
      if (this.envVariablesSelectedStatus[envVariable.name]) {
        this.envVariablesSelectedNumber++;
      } else {
        this.isBulkChecked = false;
      }
    });
  }

  changeEnvVariableSelection(name: string): void {
    this.envVariablesSelectedStatus[name] = !this.envVariablesSelectedStatus[name];
    this.updateSelectedStatus();
  }

  /**
   * Change bulk selection value
   */
  changeBulkSelection(): void {
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
  selectAllVariables(): void {
    this.envVariablesSelectedNumber = this.envVariablesList.length;
    this.envVariablesList.forEach((envVariable: IEnvironmentVariable) => {
      this.envVariablesSelectedStatus[envVariable.name] = true;
    });
  }

  /**
   * Uncheck all environment variables in list
   */
  deselectAllVariables(): void {
    this.envVariablesSelectedStatus = {};
    this.envVariablesSelectedNumber = 0;
  }

  /**
   * Add new environment variable
   *
   * @param {string} name environment's variable name
   * @param {string} value environment's variable value
   */
  addEnvVariable(name: string, value: string): void {
    this.envVariables[name] = value;
    this.envVariablesOnChange();
    this.buildVariablesList();
  }

  /**
   * Updates an existing environment variable
   *
   * @param {string} oldName old name of environment variable
   * @param {string} newName new name of environment variable
   * @param {string} newValue new value of environment variable
   */
  updateEnvVariable(oldName: string, newName: string, newValue: string): void {
    delete this.envVariables[oldName];

    this.addEnvVariable(newName, newValue);
  }

  /**
   * Show dialog to edit existing environment variable
   * @param {MouseEvent} $event
   * @param {string=} name environment variable's name
   */
  showEditDialog($event: MouseEvent, name?: string): void {
    this.$mdDialog.show({
      targetEvent: $event,
      controller: 'EditVariableDialogController',
      controllerAs: 'editVariableDialogController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        toEdit: name,
        envVariables: this.envVariables,
        callbackController: this
      },
      templateUrl: 'app/workspaces/workspace-details/environments/list-env-variables/edit-variable-dialog/edit-variable-dialog.html'
    });
  }

  /**
   * Removes selected environment variables
   */
  deleteSelectedEnvVariables(): void {
    this.showDeleteConfirmation(this.envVariablesSelectedNumber).then(() => {
      this.lodash.forEach(this.envVariablesSelectedStatus, (value: string, name: string) => {
        delete this.envVariables[name];
      });
      this.deselectAllVariables();
      this.isBulkChecked = false;
      this.envVariablesOnChange();
      this.buildVariablesList();
    });
  }

  /**
   * Show confirmation popup before environment variable to delete
   * @param {number} numberToDelete number of environment variables to delete
   * @returns {ng.IPromise<any>}
   */
  showDeleteConfirmation(numberToDelete: number): ng.IPromise<any> {
    let content = 'Would you like to delete ';
    if (numberToDelete > 1) {
      content += 'these ' + numberToDelete + ' variables?';
    } else {
      content += 'this selected variable?';
    }

    return this.confirmDialogService.showConfirmDialog('Remove variables', content, 'Delete');
  }

}
