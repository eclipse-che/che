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
import {EnvironmentManager} from '../../../../components/api/environment/environment-manager';
import {IEnvironmentManagerMachine} from '../../../../components/api/environment/environment-manager-machine';
import {CheEnvironmentRegistry} from '../../../../components/api/environment/che-environment-registry.factory';
import {WorkspaceDetailsService} from '../workspace-details.service';

interface IMachineSelectorScope extends ng.IScope {
  setMachine?: (machine: IEnvironmentManagerMachine) => void;
  setEnvironmentManager?: (environmentManager: EnvironmentManager) => void;
}

type machine = {
  name: string;
  isDev: boolean;
};

/**
 * @ngdoc controller
 * @name workspace.details.controller:MachineSelectorController
 * @description This class is handling the controller for machine selector
 * @author Oleksii Orel
 */
export class MachineSelectorController {

  static $inject = ['$timeout', '$scope', 'cheEnvironmentRegistry', 'workspaceDetailsService'];

  $timeout: ng.ITimeoutService;
  /**
   * The selected machine.
   */
  selectedMachine: IEnvironmentManagerMachine;
  /**
   * The environment manager.
   */
  environmentManager: EnvironmentManager;
  /**
   * Directive's scope.
   */
  private $scope: IMachineSelectorScope;
  /**
   * Environment registry.
   */
  private cheEnvironmentRegistry: CheEnvironmentRegistry;
  /**
   * Workspace details.
   */
  private workspaceDetails: che.IWorkspace;
  /**
   * Environment.
   */
  private environment: che.IWorkspaceEnvironment;
  /**
   * State of a button.
   */
  private buttonState: { [buttonId: string]: boolean } = {};
  /**
   * List of machines provided by parent controller.
   */
  private machines: Array<IEnvironmentManagerMachine> = [];
  /**
   * List of machines.
   */
  private machinesList: Array<machine> = [];
  /**
   * Callback which is called for check workspaceDetails changes.
   */
  private onChange: Function;
  /**
   * Filter function.
   */
  private filter: Function;

  /**
   * Default constructor that is using resource injection.
   */
  constructor($timeout: ng.ITimeoutService, $scope: ng.IScope, cheEnvironmentRegistry: CheEnvironmentRegistry, workspaceDetailsService: WorkspaceDetailsService) {
    this.$scope = $scope;
    this.$timeout = $timeout;
    this.cheEnvironmentRegistry = cheEnvironmentRegistry;
    this.init(this.workspaceDetails);
    const action = this.init.bind(this);
    workspaceDetailsService.subscribeOnWorkspaceChange(action);

    $scope.$on('$destroy', () => {
      workspaceDetailsService.subscribeOnWorkspaceChange(action);
    });
  }

  /**
   * Update workspace data.
   * @param workspaceDetails {che.IWorkspace}
   */
  init(workspaceDetails: che.IWorkspace): void {
    if (!workspaceDetails || !workspaceDetails.config) {
      return;
    }
    const workspaceConfig: che.IWorkspaceConfig = angular.copy(workspaceDetails.config);
    const environment = workspaceConfig.environments[workspaceConfig.defaultEnv];
    if (!environment || !environment.recipe) {
      return;
    }
    this.environment = environment;
    const recipeType = environment.recipe.type;
    if (!recipeType) {
      return;
    }
    const environmentManager = this.cheEnvironmentRegistry.getEnvironmentManager(recipeType);
    if (!environmentManager) {
      return;
    }
    this.environmentManager = environmentManager;
    if (angular.isFunction(this.$scope.setEnvironmentManager)) {
      this.$scope.setEnvironmentManager(environmentManager);
    }
    // update machines
    this.machines.length = 0;
    this.machinesList.length = 0;

    let machines = this.environmentManager.getMachines(this.environment, workspaceDetails.runtime);
    if (!angular.isArray(machines) || machines.length === 0) {
      return;
    }

    let names = [];
    machines.forEach((machine: IEnvironmentManagerMachine) => {
      const isDev = this.environmentManager.isDev(machine);
      if (isDev && !this.selectedMachine) {
        this.selectedMachine = machine;
      }

      if ((this.filter && this.filter(machine)) || !this.filter) {
        names.push(machine.name);
        this.machines.push(machine);
        this.machinesList.push({
          name: machine.name,
          isDev: isDev
        });
      }
    });

    if (this.machinesList.length === 0) {
      return;
    }

    let name = this.selectedMachine && names.indexOf(this.selectedMachine.name) >= 0 ? this.selectedMachine.name : this.machinesList[0].name;
      this.updateData(name);
  }

  /**
   * Updates widget data.
   * @param machineName {string} selected machine
   */
  updateData(machineName: string): void {
    if (!machineName) {
      return;
    }
    // leave only one selected machine
    this.buttonState = {
      [machineName]: true
    };
    this.selectedMachine = this.machines.find((machine: IEnvironmentManagerMachine) => {
      return machine.name === machineName;
    });

    if (angular.isFunction(this.$scope.setMachine)) {
      this.$scope.setMachine(this.selectedMachine);
    }
  }

  /**
   * Updates workspace environment data.
   */
  updateEnvironment(): void {
    const environment = this.environmentManager.getEnvironment(this.environment, this.machines);
    this.workspaceDetails.config.environments[this.workspaceDetails.config.defaultEnv] = environment;
    if (angular.isFunction(this.onChange)) {
      this.onChange();
    }
  }
}
