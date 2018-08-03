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
import {CheListHelperFactory} from '../../../../components/widget/list/che-list-helper.factory';

/**
 * @ngdoc controller
 * @name environment.variables.controller:MachineVolumesController
 * @description This class is handling the controller for list of machine volumes.
 * @author Oleksii Orel
 */
export class MachineVolumesController {

  static $inject = ['$scope', '$mdDialog', 'confirmDialogService', 'cheListHelperFactory'];

  machineVolumeOrderBy = 'name';

  private $mdDialog: ng.material.IDialogService;
  private confirmDialogService: ConfirmDialogService;
  private cheListHelper: che.widget.ICheListHelper;
  private selectedMachine: IEnvironmentManagerMachine;
  private environmentManager: EnvironmentManager;
  private machineVolumes: { [volumeName: string]: { path: string } } = {};
  private machineVolumesSelectedNumber: number = 0;
  private onChange: Function;

  /**
   * Default constructor that is using resource
   */
  constructor($scope: ng.IScope, $mdDialog: ng.material.IDialogService, confirmDialogService: ConfirmDialogService, cheListHelperFactory: CheListHelperFactory) {
    this.$mdDialog = $mdDialog;
    this.confirmDialogService = confirmDialogService;

    const helperId = 'list-machine-volumes';
    this.cheListHelper = cheListHelperFactory.getHelper(helperId);
    this.buildVariablesList(this.selectedMachine);

    const deRegistrationFn = $scope.$watch(() => {
      return this.selectedMachine;
    }, (selectedMachine: IEnvironmentManagerMachine) => {
      this.buildVariablesList(selectedMachine);
    }, true);

    $scope.$on('$destroy', () => {
      cheListHelperFactory.removeHelper(helperId);
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
    this.machineVolumes = this.environmentManager.getMachineVolumes(selectedMachine);
    if (angular.isObject(this.machineVolumes)) {
      this.cheListHelper.setList(Object.keys(this.machineVolumes).map((key: string) => {
        const path = this.machineVolumes[key] && this.machineVolumes[key].path ? this.machineVolumes[key].path : '';
        return {
          name: key,
          path: path
        };
      }), 'name');
    }
  }

  /**
   * Updates an existing volume variable.
   * @param {string} name  name of volume variable
   * @param {string} path  path of volume variable
   * @param {string} oldName  old name of volume variable
   */
  updateMachineVolume(name: string, path: string, oldName?: string): void {
    if (oldName) {
      delete this.machineVolumes[oldName];
    }
    this.machineVolumes[name] = {'path': path};
    this.environmentManager.setMachineVolumes(this.selectedMachine, this.machineVolumes);
    this.buildVariablesList(this.selectedMachine);
    this.onChange();
  }

  /**
   * Show dialog to edit the existing volume variable or add a new one.
   * @param {string} name volume variable's name
   */
  showEditDialog(name?: string): void {
    this.$mdDialog.show({
      controller: 'EditMachineVolumeDialogController',
      controllerAs: 'editMachineVolumeDialogController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        toEdit: name,
        machineVolumes: this.machineVolumes,
        updateMachineVolume: this.updateMachineVolume.bind(this)
      },
      templateUrl: 'app/workspaces/workspace-details/workspace-machine-volumes/edit-volume-dialog/edit-volume-dialog.html'
    });
  }

  /**
   * Removes selected volume variables.
   */
  deleteSelectedMachineVolumes(): void {
    this.showDeleteConfirmation(this.machineVolumesSelectedNumber).then(() => {
      this.cheListHelper.getSelectedItems().forEach((volume: { name: string; path: string }) => {
        delete this.machineVolumes[volume.name];
      });
      this.cheListHelper.deselectAllItems();
      this.environmentManager.setMachineVolumes(this.selectedMachine, this.machineVolumes);
      this.onChange();
      this.buildVariablesList(this.selectedMachine);
    });
  }

  /**
   * Shows confirmation popup before delete.
   * @param variableName {string}
   */
  deleteMachineVolume(variableName: string): void {
    const promise = this.confirmDialogService.showConfirmDialog('Remove variable', 'Would you like to delete this variable?', 'Delete');
    promise.then(() => {
      delete this.machineVolumes[variableName];
      this.environmentManager.setMachineVolumes(this.selectedMachine, this.machineVolumes);
      this.onChange();
      this.buildVariablesList(this.selectedMachine);
    });
  }

  /**
   * Show confirmation popup before volume variable to delete.
   * @param {number} numberToDelete number of volume variables to delete
   * @returns {ng.IPromise<any>}
   */
  showDeleteConfirmation(numberToDelete: number): ng.IPromise<any> {
    let content = 'Would you like to delete ';
    if (numberToDelete > 1) {
      content += `these ${numberToDelete} variables?`;
    } else {
      content += 'this selected variable?';
    }

    return this.confirmDialogService.showConfirmDialog('Remove variables', content, 'Delete');
  }
}
