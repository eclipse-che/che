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
import {ConfirmDialogService} from '../../../../components/service/confirm-dialog/confirm-dialog.service';
import {IEnvironmentManagerMachine} from '../../../../components/api/environment/environment-manager-machine';
import {EnvironmentManager} from '../../../../components/api/environment/environment-manager';

interface IEnvironmentVariable {
  name: string;
  value: string;
}

/**
 * @ngdoc controller
 * @name environment.variables.controller:EnvVariablesController
 * @description This class is handling the controller for list of environment variables.
 * @author Oleksii Orel
 */
export class EnvVariablesController {

  static $inject = ['$scope', '$mdDialog', 'confirmDialogService'];

  envVariableOrderBy = 'name';

  private $mdDialog: ng.material.IDialogService;
  private confirmDialogService: ConfirmDialogService;
  private selectedMachine: IEnvironmentManagerMachine;
  private environmentManager: EnvironmentManager;
  private isEnvVarEditable: boolean;
  private isBulkChecked: boolean = false;
  private envVariables: { [envVarName: string]: string } = {};
  private envVariablesSelectedStatus: { [envVarName: string]: boolean } = {};
  private envVariablesList: Array<IEnvironmentVariable> = [];
  private envVariablesSelectedNumber: number = 0;
  private onChange: Function;

  /**
   * Default constructor that is using resource
   */
  constructor($scope: ng.IScope, $mdDialog: ng.material.IDialogService, confirmDialogService: ConfirmDialogService) {
    this.$mdDialog = $mdDialog;
    this.confirmDialogService = confirmDialogService;

    this.buildVariablesList(this.selectedMachine);
    const deRegistrationFn = $scope.$watch(() => {
      return this.selectedMachine;
    }, (selectedMachine: IEnvironmentManagerMachine) => {
      this.buildVariablesList(selectedMachine);
    }, true);

    $scope.$on('$destroy', () => {
      deRegistrationFn();
    });
  }

  /**
   * Build variables list.
   * @param selectedMachine {IEnvironmentManagerMachine}
   */
  buildVariablesList(selectedMachine: IEnvironmentManagerMachine): void {
    if (!selectedMachine || !this.environmentManager) {
      return;
    }
    this.isEnvVarEditable = this.environmentManager.canEditEnvVariables(selectedMachine);
    this.envVariables = this.environmentManager.getEnvVariables(selectedMachine);
    this.envVariablesList = [];
    if (!angular.isObject(this.envVariables)) {
      return;
    }
    Object.keys(this.envVariables).forEach((key: string) => {
      this.envVariablesList.push({name: key, value: this.envVariables[key]});
    });
  }

  /**
   * Update environment variable selected status.
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

  /**
   * Change bulk selection value.
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
   * Check all environment variables in list.
   */
  selectAllVariables(): void {
    this.envVariablesSelectedNumber = this.envVariablesList.length;
    this.envVariablesList.forEach((envVariable: IEnvironmentVariable) => {
      this.envVariablesSelectedStatus[envVariable.name] = true;
    });
  }

  /**
   * Uncheck all environment variables in list.
   */
  deselectAllVariables(): void {
    this.envVariablesSelectedStatus = {};
    this.envVariablesSelectedNumber = 0;
  }

  /**
   * Updates an existing environment variable.
   * @param {string} name  name of environment variable
   * @param {string} value  value of environment variable
   * @param {string} oldName  old name of environment variable
   */
  updateEnvVariable(name: string, value: string, oldName?: string): void {
    if (oldName) {
      delete this.envVariables[oldName];
    }
    this.envVariables[name] = value;
    this.environmentManager.setEnvVariables(this.selectedMachine, this.envVariables);
    this.buildVariablesList(this.selectedMachine);
    this.onChange();
  }

  /**
   * Show dialog to edit the existing environment variable or add a new one.
   * @param {string} name environment variable's name
   */
  showEditDialog(name?: string): void {
    this.$mdDialog.show({
      controller: 'EditEnvVariableDialogController',
      controllerAs: 'editEnvVariableDialogController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        toEdit: name,
        envVariables: this.envVariables,
        updateEnvVariable: this.updateEnvVariable.bind(this)
      },
      templateUrl: 'app/workspaces/workspace-details/workspace-machine-env-variables/edit-variable-dialog/edit-variable-dialog.html'
    });
  }

  /**
   * Removes selected environment variables.
   */
  deleteSelectedEnvVariables(): void {
    this.showDeleteConfirmation(this.envVariablesSelectedNumber).then(() => {
      Object.keys(this.envVariablesSelectedStatus).forEach((variableName: string) => {
        delete this.envVariables[variableName];
      });
      this.deselectAllVariables();
      this.isBulkChecked = false;
      this.environmentManager.setEnvVariables(this.selectedMachine, this.envVariables);
      this.onChange();
      this.buildVariablesList(this.selectedMachine);
    });
  }

  /**
   * Shows confirmation popup before delete.
   * @param variableName {string}
   */
  deleteEnvVariable(variableName: string): void {
    const promise = this.confirmDialogService.showConfirmDialog('Remove variable', 'Would you like to delete this variable?', 'Delete');
    promise.then(() => {
      delete this.envVariables[variableName];
      this.environmentManager.setEnvVariables(this.selectedMachine, this.envVariables);
      this.onChange();
      this.buildVariablesList(this.selectedMachine);
    });
  }

  /**
   * Show confirmation popup before environment variable to delete.
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
