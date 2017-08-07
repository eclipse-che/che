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
import {EnvironmentManager} from '../../../../components/api/environment/environment-manager';
import {IEnvironmentManagerMachine} from '../../../../components/api/environment/environment-manager-machine';
import {CheEnvironmentRegistry} from '../../../../components/api/environment/che-environment-registry.factory';

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
   * Workspace config.
   */
  private workspaceConfig: che.IWorkspaceConfig;
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
   * Default constructor that is using resource injection.
   * @ngInject for Dependency injection
   */
  constructor($scope: ng.IScope, cheEnvironmentRegistry: CheEnvironmentRegistry) {
    this.$scope = $scope;
    this.cheEnvironmentRegistry = cheEnvironmentRegistry;

    const deRegistrationFn = $scope.$watch(() => {
      return this.workspaceDetails;
    }, (workspaceDetails: che.IWorkspace) => {
      this.init(workspaceDetails);
    }, true);

    $scope.$on('$destroy', () => {
      deRegistrationFn();
    });
  }

  /**
   * Update workspace data.  .runtime
   * @param workspaceDetails {che.IWorkspace}
   */
  init(workspaceDetails: che.IWorkspace): void {
    if (!workspaceDetails) {
      return;
    }
    const workspaceConfig: che.IWorkspaceConfig = workspaceDetails.config;
    if (!workspaceConfig || angular.equals(workspaceConfig, this.workspaceConfig)) {
      return;
    }
    this.workspaceConfig = angular.copy(workspaceConfig);
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

    const machines = this.environmentManager.getMachines(this.environment, workspaceDetails.runtime);
    if (!angular.isArray(machines)) {
      return;
    }
    machines.forEach((machine: IEnvironmentManagerMachine) => {
      const isDev = this.environmentManager.isDev(machine);
      if (isDev && !this.selectedMachine) {
        this.selectedMachine = machine;
      }
      this.machines.push(machine);
      this.machinesList.push({
        name: machine.name,
        isDev: isDev
      });
    });
    this.updateData(this.selectedMachine.name);
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
