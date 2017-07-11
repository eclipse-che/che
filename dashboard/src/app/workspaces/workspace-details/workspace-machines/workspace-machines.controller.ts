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
import {MemoryUnit} from '../../../../components/filter/change-memory-unit/change-memory-unit.filter';
import {EnvironmentManager} from '../../../../components/api/environment/environment-manager';
import {ConfirmDialogService} from '../../../../components/service/confirm-dialog/confirm-dialog.service';
import {CheEnvironmentRegistry} from '../../../../components/api/environment/che-environment-registry.factory';
import {IEnvironmentManagerMachine} from '../../../../components/api/environment/environment-manager-machine';


type machine = {
  name: string;
  image: string;
  isDev: boolean;
  memoryLimitGBytes: number;
};

/**
 * @ngdoc controller
 * @name workspace.machines.controller:WorkspaceMachinesController
 * @description This class is handling the controller for the workspace machines widget.
 * @author Oleksii Orel
 */
export class WorkspaceMachinesController {

  /**
   * Angular Promise service.
   */
  private $q: ng.IQService;
  /**
   * Log service.
   */
  private $log: ng.ILogService;
  /**
   * Filter service.
   */
  private $filter: ng.IFilterService;
  /**
   * Material Design Dialog Service
   */
  private $mdDialog: ng.material.IDialogService;
  /**
   * Confirm dialog service.
   */
  private confirmDialogService: ConfirmDialogService;
  /**
   * Environment registry.
   */
  private cheEnvironmentRegistry: CheEnvironmentRegistry;
  /**
   * Environment manager.
   */
  private environmentManager: EnvironmentManager;
  /**
   * List helper.
   */
  private cheListHelper: che.widget.ICheListHelper;
  /**
   * List of machines provided by parent controller.
   */
  private machines: Array<IEnvironmentManagerMachine>;
  /**
   * List of machines.
   */
  private machinesList: Array<machine>;
  /**
   * Workspace details.
   */
  private workspaceDetails: che.IWorkspace;
  /**
   * Workspace config.
   */
  private workspaceConfig: che.IWorkspaceConfig;
  /**
   * Environment.
   */
  private environment: che.IWorkspaceEnvironment;
  /**
   * URL of current page.
   */
  private absUrl: string;
  /**
   * Callback which is called when workspace is changed.
   */
  private workspaceOnChange: Function;
  /**
   * Callback which is called when change DEV machine.
   */
  private onDevChange: (machineName: string) => ng.IPromise<any>;

  /**
   * Default constructor that is using resource injection.
   * @ngInject for Dependency injection
   */
  constructor($q: ng.IQService, $log: ng.ILogService, $filter: ng.IFilterService, $scope: ng.IScope, $mdDialog: ng.material.IDialogService, confirmDialogService: ConfirmDialogService, $location: ng.ILocationService, cheEnvironmentRegistry: CheEnvironmentRegistry, cheListHelperFactory: che.widget.ICheListHelperFactory) {
    this.$q = $q;
    this.$log = $log;
    this.$filter = $filter;
    this.$mdDialog = $mdDialog;
    this.confirmDialogService = confirmDialogService;
    this.cheEnvironmentRegistry = cheEnvironmentRegistry;

    this.absUrl = $location.absUrl().split('?')[0];
    this.cheListHelper = cheListHelperFactory.getHelper('workspace-machine-list');

    this.onDevChange = (machineName: string) => {
      return this.changeDevMachine(machineName).catch((error: string) => {
        $log.error(error);
      });
    };

    const deRegistrationFn = $scope.$watch(() => {
      return this.workspaceDetails ? this.workspaceDetails.config : null;
    }, (workspaceConfig: che.IWorkspaceConfig) => {
      this.updateData(workspaceConfig);
    }, true);

    $scope.$on('$destroy', () => {
      deRegistrationFn();
    });
  }

  /**
   * Returns true if the recipe type is compose.
   *
   * @returns {boolean}
   */
  isCompose(): boolean {
    return this.environment && this.environment.recipe && this.environment.recipe.type === 'compose';
  }

  /**
   * Update workspace data.
   *
   * @param workspaceConfig {che.IWorkspaceConfig}
   */
  updateData(workspaceConfig: che.IWorkspaceConfig): void {
    if (workspaceConfig === null) {
      return;
    }
    if (this.workspaceConfig && angular.equals(this.workspaceConfig, workspaceConfig)) {
      return;
    }
    this.environment = workspaceConfig ? workspaceConfig.environments[workspaceConfig.defaultEnv] : null;
    if (!this.environment || !this.environment.recipe || !this.environment.recipe.type) {
      return;
    }
    this.environmentManager = this.cheEnvironmentRegistry.getEnvironmentManager(this.environment.recipe.type);
    this.machines = this.environmentManager.getMachines(this.environment);
    this.environment = this.environmentManager.getEnvironment(this.environment, this.machines);

    if (!angular.isArray(this.machines)) {
      this.machinesList = [];
    } else {
      this.machinesList = this.machines.map((machine: IEnvironmentManagerMachine) => {
        const source: any = this.environmentManager.getSource(machine),
          memoryLimitBytes = this.environmentManager.getMemoryLimit(machine),
          memoryLimitGBytesWithUnit = this.$filter('changeMemoryUnit')(memoryLimitBytes, [MemoryUnit[MemoryUnit.B], MemoryUnit[MemoryUnit.GB]]);
        return <machine>{
          image: source && source.image ? source.image : '',
          name: machine.name,
          isDev: this.environmentManager.isDev(machine),
          memoryLimitGBytes: this.getNumber(memoryLimitGBytesWithUnit)
        };
      });
    }
    this.workspaceConfig = angular.copy(workspaceConfig);
    this.workspaceOnChange();
    this.cheListHelper.setList(this.machinesList, 'name', (machine: machine): boolean => {
      return !machine.isDev;
    });
  }

  /**
   * Update environment.
   */
  updateEnvironment(): void {
    const environment = this.environmentManager.getEnvironment(this.environment, this.machines);
    this.workspaceDetails.config.environments[this.workspaceDetails.config.defaultEnv] = environment;
  }

  /**
   * Changes DEV machine by name.
   *
   * @param machineName {string}
   * @returns {IPromise<any>}
   */
  changeDevMachine(machineName: string): ng.IPromise<any> {
    const deferred = this.$q.defer();

    if (!machineName) {
      deferred.reject('Machine name is not defined.');
    } else {
      let machine = this.machines.find((machine: any) => {
        return machine.name === machineName;
      });
      if (!machine) {
        deferred.reject('Machine is not found.');
      } else {
        if (!this.environmentManager.isDev(machine)) {
          // remove ws-agent from machine which is the dev machine now
          this.machines.forEach((machine: IEnvironmentManagerMachine) => {
            if (this.environmentManager.isDev(machine)) {
              this.environmentManager.setDev(machine, false);
            }
          });
          // add ws-agent to current machine agents list
          this.environmentManager.setDev(machine, true);
          this.updateEnvironment();
          deferred.resolve();
        } else {
          // return ws-agent to current machine
          this.cheListHelper.getVisibleItems().find((machine: machine) => {
            return machine.name === machineName;
          }).isDev = true;
          this.changeDevMachineDialog(machine).then(() => {
            deferred.resolve();
          }, () => {
            deferred.reject('Machine is not select.');
          });
        }
      }
    }

    return deferred.promise;
  };

  /**
   * Show confirmation popup before delete
   * @returns {ng.IPromise<any>}
   */
  showDeleteMachinesConfirmation(): ng.IPromise<any> {
    const selectedItems = this.cheListHelper && angular.isArray(this.cheListHelper.getSelectedItems()) ? this.cheListHelper.getSelectedItems() : [];
    if (selectedItems.length === 0) {
      return this.$q.reject('No selected machines');
    }
    let content = 'Would you like to delete ';
    if (selectedItems.length === 1) {
      content += 'this selected machine?';
    } else {
      content += 'these ' + selectedItems.length + ' machines?';
    }

    return this.confirmDialogService.showConfirmDialog('Remove machines', content, 'Delete').then(() => {
      return selectedItems;
    });
  }

  /**
   * Removes selected machines.
   */
  deleteSelectedMachines(): void {
    this.showDeleteMachinesConfirmation().then((selectedMachines: Array<machine>) => {
      selectedMachines.forEach((selectedMachine: machine) => {
        this.machineOnDelete(selectedMachine.name);
      });
    });
  }

  /**
   * Show dialog to edit the mashine or add a new one machine to the environment.
   *
   * @param machineName {string}
   */
  editMachine(machineName?: string): void {
    this.$mdDialog.show({
      controller: 'EditMachineDialogController',
      controllerAs: 'editMachineDialogController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        environment: this.workspaceDetails.config.environments[this.workspaceDetails.config.defaultEnv],
        machineName: machineName
      },
      templateUrl: 'app/workspaces/workspace-details/workspace-machines/edit-machine-dialog/edit-machine-dialog.html'
    });
  }

  /**
   * Callback which is called in order to delete specified machine.
   *
   * @param name
   */
  machineOnDelete(name: string): void {
    const newEnvironment = this.environmentManager.deleteMachine(this.environment, name);
    this.workspaceDetails.config.environments[this.workspaceDetails.config.defaultEnv] = newEnvironment;
  }

  /**
   * Shows confirmation popup before machine to delete.
   * @param machine {IEnvironmentManagerMachine}
   * @param popupTitle {string}
   * @param okButtonTitle {string}
   * @returns {angular.IPromise<any>}
   */
  changeDevMachineDialog(machine: IEnvironmentManagerMachine, popupTitle?: string, okButtonTitle?: string): ng.IPromise<any> {
    return this.$mdDialog.show({
      controller: 'ChangeDevMachineDialogController',
      controllerAs: 'changeDevMachineDialogController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        popupTitle: popupTitle,
        okButtonTitle: okButtonTitle,
        currentDevMachineName: machine.name,
        machinesList: this.machines,
        changeDevMachine: this.onDevChange
      },
      templateUrl: 'app/workspaces/workspace-details/workspace-machines/change-dev-machine-dialog/change-dev-machine-dialog.html'
    });
  }

  /**
   * Shows confirmation popup before machine to delete.
   * @param name {string}
   *
   * @returns {ng.IPromise<any>}
   */
  deleteMachine(name: string): void {
    const machine: IEnvironmentManagerMachine = this.machines.find((machine: IEnvironmentManagerMachine) => {
      return machine && machine.name === name;
    });
    let promise: ng.IPromise<any>;
    if (!this.environmentManager.isDev(machine)) {
      promise = this.confirmDialogService.showConfirmDialog('Remove machine', 'Would you like to delete this machine?', 'Delete');
    } else {
      promise = this.changeDevMachineDialog(machine, 'Remove machine', 'Delete');
    }

    promise.then(() => {
      this.machineOnDelete(name);
    });
  }

  /**
   * Callback which is called when RAM is changes.
   *
   * @param name {string} a machine name
   * @param memoryLimitGBytes {number} amount of ram in GB
   */
  onRamChange(name: string, memoryLimitGBytes: number): void {
    if (!this.machines || !memoryLimitGBytes) {
      return;
    }
    const memoryLimitBytesWithUnit = this.$filter('changeMemoryUnit')(memoryLimitGBytes, [MemoryUnit[MemoryUnit.GB], MemoryUnit[MemoryUnit.B]]);
    const memoryLimitBytes = this.getNumber(memoryLimitBytesWithUnit);
    const machine: IEnvironmentManagerMachine = this.machines.find((machine: IEnvironmentManagerMachine) => {
      return machine && machine.name === name;
    });

    if (machine && this.environmentManager.getMemoryLimit(machine).toString() !== memoryLimitBytes.toString()) {
      this.environmentManager.setMemoryLimit(machine, memoryLimitBytes);
      this.updateEnvironment();
    }
  }

  /**
   * Returns number.
   *
   * @param memoryLimit {string} a string which contains machine's memory limit.
   * @return {number}
   */
  getNumber(memoryLimit: string): number {
    const [, memoryLimitNumber] = /^([^\s]+)\s+[^\s]+$/.exec(memoryLimit);
    return parseFloat(memoryLimitNumber);
  }

}
