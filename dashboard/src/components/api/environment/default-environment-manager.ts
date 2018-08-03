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

import {EnvironmentManager} from './environment-manager';
import {IEnvironmentManagerMachine} from './environment-manager-machine';

/**
 * This is the implementation of environment manager that handles the unknown workspace recipes type format.
 *
 * Machines are described only in machines attribute of the environment (machine configs).
 * The machine configs contain memoryLimitBytes in attributes, servers and agent.
 *
 *  @author Oleksii Orel
 */

export class DefaultEnvironmentManager extends EnvironmentManager {
  private environmentType: string;

  constructor($log: ng.ILogService, environmentType: string) {
    super($log);
    this.environmentType = environmentType;
  }

  get type(): string {
    return this.environmentType;
  }

  get editorMode(): string {
    return 'text/x-yaml';
  }

  /**
   * Parses recipe content.
   * @param content {string} recipe content
   * @returns {IPodList} recipe object
   */
  parseRecipe(content: string): any {
    let recipe: any;
    try {
      recipe = jsyaml.safeLoad(content);
    } catch (e) {
      recipe = {};
      this.$log.error(e);
    }
    return recipe;
  }

  /**
   * Dumps recipe object.
   * @param recipe {IPodList} recipe object
   * @returns {string} recipe content
   */
  stringifyRecipe(recipe: any): string {
    return jsyaml.safeDump(recipe, {'indent': 1});
  }

  /**
   * Retrieves the list of machines.
   * @param {che.IWorkspaceEnvironment} environment environment's configuration
   * @param {any=} runtime runtime of active environment
   * @returns {IEnvironmentManagerMachine[]} list of machines defined in environment
   */
  getMachines(environment: che.IWorkspaceEnvironment, runtime?: any): Array<IEnvironmentManagerMachine> {
    const machines: Array<IEnvironmentManagerMachine> = super.getMachines(environment, runtime);
    if (!environment || !environment.recipe || !environment.recipe.content) {
      return machines;
    }

    Object.keys(environment.machines).forEach((machineName: string) => {
      let machine: IEnvironmentManagerMachine = machines.find((_machine: IEnvironmentManagerMachine) => {
        return _machine.name === machineName;
      });
      if (!machine) {
        machine = {name: machineName};
        machines.push(machine);
      }

      machine.recipe = environment.recipe.content ? environment.recipe.content : '';

      if (environment.machines && environment.machines[machineName]) {
        angular.merge(machine, environment.machines[machineName]);
      }
      // memory
      let memoryLimitBytes = this.getMemoryLimit(machine);
      if (memoryLimitBytes !== -1) {
        return;
      }
      // set default value of memory limit
      this.setMemoryLimit(machine, this.DEFAULT_MEMORY_LIMIT);
    });

    return machines;
  }

  /**
   * Retrieves the machines name.
   * @param {IEnvironmentManagerMachine} machine
   * @returns {string}
   */
  getMachineName(machine: IEnvironmentManagerMachine): string {
    return super.getMachineName(machine).split(/\//).reverse().shift();
  }

  /**
   * Returns object which contains docker image or link to docker file and build context.
   * @param {IEnvironmentManagerMachine} machine
   * @returns {*}
   */
  getSource(machine: IEnvironmentManagerMachine): any {
    return null;
  }

  /**
   * Updates machine's image
   * @param {IEnvironmentManagerMachine} machine
   * @param {String} image
   */
  setSource(machine: IEnvironmentManagerMachine, image: string) {
    this.$log.error('EnvironmentManager: cannot change machine source.');
  }

  /**
   * Create a new default machine.
   * @param {che.IWorkspaceEnvironment} environment
   * @param {string} image
   * @return {IEnvironmentManagerMachine}
   */
  createMachine(environment: che.IWorkspaceEnvironment, image?: string): IEnvironmentManagerMachine {
    this.$log.error('EnvironmentManager: cannot create a new machine.');
    return null;
  }

  /**
   * Add machine.
   *
   * @param {che.IWorkspaceEnvironment} environment
   * @param {IEnvironmentManagerMachine} machine
   *
   * @return {che.IWorkspaceEnvironment}
   */
  addMachine(environment: che.IWorkspaceEnvironment, machine: IEnvironmentManagerMachine): che.IWorkspaceEnvironment {
    this.$log.error('EnvironmentManager: cannot add machine.');
    return environment;
  }

  /**
   * Removes machine.
   * @param {che.IWorkspaceEnvironment} environment
   * @param {string} name name of machine
   * @returns {che.IWorkspaceEnvironment} new environment
   */
  deleteMachine(environment: che.IWorkspaceEnvironment, name: string): che.IWorkspaceEnvironment {
    if (!environment || !environment.machines || !name) {
      return environment;
    }

    environment = angular.copy(environment);
    delete environment.machines[name];

    return environment;
  }
}
