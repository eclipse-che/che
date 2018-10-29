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
import {ComposeParser, IComposeRecipe} from './compose-parser';
import {CheRecipeTypes} from '../recipe/che-recipe-types';

/**
 * This is the implementation of environment manager that handles the docker compose format.
 *
 * Format sample and specific description:
 * <code>
 * services:
 *   devmachine:
 *     image: codenvy/ubuntu_jdk8
 *     depends_on:
 *       - anotherMachine
 *     mem_limit: 2147483648
 *   anotherMachine:
 *     image: codenvy/ubuntu_jdk8
 *     depends_on:
 *       - thirdMachine
 *     mem_limit: 1073741824
 *   thirdMachine:
 *     image: codenvy/ubuntu_jdk8
 *     mem_limit: 512741824
 *     labels:
 *       com.example.description: "Accounting webapp"
 *       com.example.department: "Finance"
 *       com.example.label-with-empty-value: ""
 *     environment:
 *       SOME_ENV: development
 *       SHOW: 'true'
 *       SESSION_SECRET:
 * </code>
 *
 *
 * The recipe type is <code>compose</code>.
 * Machines are described both in recipe and in machines attribute of the environment (machine configs).
 * The machine configs contain memoryLimitBytes in attributes, servers and agent.
 * Environment variables can be set only in recipe content.
 *
 *  @author Ann Shumilova
 */

export class ComposeEnvironmentManager extends EnvironmentManager {
  parser: ComposeParser;

  constructor($log: ng.ILogService) {
    super($log);

    this.parser = new ComposeParser();
  }

  get type(): string {
    return CheRecipeTypes.COMPOSE;
  }

  get editorMode(): string {
    return 'text/x-yaml';
  }

  /**
   * Parses recipe content
   *
   * @param content {string} recipe content
   * @returns {IComposeRecipe} recipe object
   */
  parseRecipe(content: string): IComposeRecipe {
    let recipe = null;
    try {
      recipe = this.parser.parse(content);
    } catch (e) {
      this.$log.error(e);
    }
    return recipe;
  }

  /**
   * Dumps recipe object
   *
   * @param recipe {IComposeRecipe} recipe object
   * @returns {string} recipe content
   */

  stringifyRecipe(recipe: IComposeRecipe): string {
    let content = '';
    try {
      content = this.parser.dump(recipe);
    } catch (e) {
      this.$log.error(e);
    }

    return content;
  }

  /**
   * Retrieves the list of machines.
   *
   * @param {che.IWorkspaceEnvironment} environment environment's configuration
   * @param {any=} runtime runtime of active environment
   * @returns {IEnvironmentManagerMachine[]} list of machines defined in environment
   */
  getMachines(environment: che.IWorkspaceEnvironment, runtime?: any): IEnvironmentManagerMachine[] {
    let recipe: any = null,
      machines: IEnvironmentManagerMachine[] = super.getMachines(environment, runtime),
      machineNames: string[] = [];

    if (environment.recipe.content) {
      recipe = this.parseRecipe(environment.recipe.content);
      if (recipe) {
        machineNames = Object.keys(recipe.services);
      } else if (environment.machines) {
        machineNames = Object.keys(environment.machines);
      }
    } else if (environment.recipe.location) {
      machineNames = Object.keys(environment.machines);
    }

    machineNames.forEach((machineName: string) => {
      let machine: IEnvironmentManagerMachine = machines.find((_machine: IEnvironmentManagerMachine) => {
        return _machine.name === machineName;
      });

      if (!machine) {
        machine = {name: machineName};
        machines.push(machine);
      }

      machine.recipe = recipe ? {[machineName]: recipe.services[machineName]} : recipe;

      if (environment.machines && environment.machines[machineName]) {
        angular.merge(machine, environment.machines[machineName]);
      }

      // memory
      let memoryLimitBytes = this.getMemoryLimit(machine);
      if (memoryLimitBytes === -1 && recipe) {
        this.setMemoryLimit(machine, recipe.services[machineName].mem_limit);
      }
    });

    return machines;
  }

  /**
   * Provides the environment configuration based on machines format.
   *
   * @param {che.IWorkspaceEnvironment} environment origin of the environment to be edited
   * @param {IEnvironmentManagerMachine} machines the list of machines
   * @returns {che.IWorkspaceEnvironment} environment's configuration
   */
  getEnvironment(environment: che.IWorkspaceEnvironment, machines: IEnvironmentManagerMachine[]): che.IWorkspaceEnvironment {
    let newEnvironment = super.getEnvironment(environment, machines);

    if (newEnvironment.recipe.content) {
      let recipe: IComposeRecipe = this.parseRecipe(newEnvironment.recipe.content);

      if (recipe) {
        machines.forEach((machine: IEnvironmentManagerMachine) => {
          let machineName = machine.name;
          if (!recipe.services[machineName]) {
            return;
          }
          if (machine.recipe[machine.name].environment && Object.keys(machine.recipe[machine.name].environment).length) {
            recipe.services[machineName].environment = angular.copy(machine.recipe[machine.name].environment);
          } else {
            delete recipe.services[machineName].environment;
          }
          if (machine.recipe[machine.name].image) {
            recipe.services[machineName].image = machine.recipe[machine.name].image;
          }
        });

        try {
          newEnvironment.recipe.content = this.stringifyRecipe(recipe);
        } catch (e) {
          this.$log.error('Cannot retrieve environment\'s recipe, error: ', e);
        }
      }
    }

    return newEnvironment;
  }

  /**
   * Returns object which contains docker image or link to docker file and build context.
   *
   * @param {IEnvironmentManagerMachine} machine
   * @returns {*}
   */
  getSource(machine: IEnvironmentManagerMachine): any {
    if (!machine.recipe) {
      return null;
    }

    if (machine.recipe[machine.name].image) {
      return {image: machine.recipe[machine.name].image};
    } else if (machine.recipe[machine.name].build) {
      return machine.recipe[machine.name].build;
    }
  }

  /**
   * Updates machine's image
   *
   * @param {IEnvironmentManagerMachine} machine
   * @param {String} image
   */
  setSource(machine: IEnvironmentManagerMachine, image: string) {
    if (!machine.recipe) {
      return;
    }
    machine.recipe[machine.name].image = image;
  }

  /**
   * Renames machine.
   *
   * @param {che.IWorkspaceEnvironment} environment
   * @param {string} oldName
   * @param {string} newName
   * @returns {che.IWorkspaceEnvironment} new environment
   */
  renameMachine(environment: che.IWorkspaceEnvironment, oldName: string, newName: string): che.IWorkspaceEnvironment {
    environment = angular.copy(environment);
    try {
      const recipe: IComposeRecipe = this.parseRecipe(environment.recipe.content);

      // fix relations to other machines in recipe
      Object.keys(recipe.services).forEach((serviceName: string) => {
        if (serviceName === oldName) {
          return;
        }

        // fix 'depends_on'
        const dependsOn = recipe.services[serviceName].depends_on || [],
          index = dependsOn.indexOf(oldName);
        if (index > -1) {
          dependsOn.splice(index, 1);
          dependsOn.push(newName);
        }

        // fix 'links'
        const links = recipe.services[serviceName].links || [],
          re = new RegExp('^' + oldName + '(?:$|:(.+))');
        for (let i = 0; i < links.length; i++) {
          if (re.test(links[i])) {
            const match = links[i].match(re),
              alias = match[1] || '',
              newLink = alias ? newName + ':' + alias : newName;
            links.splice(i, 1);
            links.push(newLink);

            break;
          }
        }
      });

      // rename machine in recipe
      recipe.services[newName] = recipe.services[oldName];
      delete recipe.services[oldName];

      // try to update recipe
      environment.recipe.content = this.stringifyRecipe(recipe);

      // and then update config
      environment.machines[newName] = environment.machines[oldName];
      delete environment.machines[oldName];
    } catch (e) {
      this.$log.error('Cannot rename machine, error: ', e);
    }

    return environment;
  }

  /**
   * Retrieves the machines name.
   *
   * @param {IEnvironmentManagerMachine} machine
   * @returns {string}
   */
  getMachineName(machine: IEnvironmentManagerMachine): string {
    if (!machine && !machine.name) {
      return '';
    }
    const machineRecipe = machine.recipe;
    if (machineRecipe[machine.name]) {
      return machine.name;
    }

    const machineNames = Object.keys(machineRecipe);
    if (machine.recipe && machineNames.length === 1) {
      return machineNames[0];
    }

    return machine.name;
  }

  /**
   * Create a new default machine.
   * @param {che.IWorkspaceEnvironment} environment
   * @param {string} image
   * @return {IEnvironmentManagerMachine}
   */
  createMachine(environment: che.IWorkspaceEnvironment, image?: string): IEnvironmentManagerMachine {
    const machineName = this.getUniqueMachineName(environment);
    const machineImage = !image ? 'eclipse/ubuntu_jdk8' : image;

    return {
      name: machineName,
      attributes: {
        memoryLimitBytes: this.DEFAULT_MEMORY_LIMIT
      },
      recipe: jsyaml.load(`${machineName}:\n image: ${machineImage}\n mem_limit: ${this.DEFAULT_MEMORY_LIMIT}\n`)
    };
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
    if (!machine || !machine.name || !machine.recipe) {
      this.$log.error('EnvironmentManager: cannot add machine.');
      return environment;
    }

    machine = angular.copy(machine);

    const machineRecipe = machine.recipe;
    delete machine.recipe;
    const machineName = machine.name;
    delete machine.name;

    environment.machines[machineName] = machine;

    const recipe: IComposeRecipe = this.parseRecipe(environment.recipe.content);
    angular.extend(recipe.services, machineRecipe);
    environment.recipe.content = this.stringifyRecipe(recipe);

    return environment;
  }


  /**
   * Returns memory limit from machine's attributes
   *
   * @param {IEnvironmentManagerMachine} machine
   * @returns {number} memory limit in bytes
   */
  getMemoryLimit(machine: IEnvironmentManagerMachine): number {
    const mem_limit = super.getMemoryLimit(machine);
    if (mem_limit > 0) {
      return mem_limit;
    }
    const machineName = this.getMachineName(machine);
    if (machine.recipe && machine.recipe[machineName] && machine.recipe[machineName].mem_limit) {
      return machine.recipe[machineName].mem_limit;
    }

    return -1;
  }

  /**
   * Sets the memory limit of the pointed machine.
   * Value in attributes has the highest priority,
   *
   * @param {IEnvironmentManagerMachine} machine machine to change memory limit
   * @param {number} limit memory limit
   */
  setMemoryLimit(machine: IEnvironmentManagerMachine, limit: number): void {
    super.setMemoryLimit(machine, limit);
    if (limit) {
      machine.attributes.memoryLimitBytes = limit.toString();
      if (machine.recipe && machine.recipe[machine.name]) {
        machine.recipe[machine.name].mem_limit = limit;
      }
    }
  }

  /**
   * Removes machine.
   *
   * @param {che.IWorkspaceEnvironment} environment
   * @param {string} name name of machine
   * @returns {che.IWorkspaceEnvironment} new environment
   */
  deleteMachine(environment: che.IWorkspaceEnvironment, name: string): che.IWorkspaceEnvironment {
    try {
      let recipe: IComposeRecipe = this.parseRecipe(environment.recipe.content);

      // fix relations to other machines in recipe
      Object.keys(recipe.services).forEach((serviceName: string) => {
        if (serviceName === name) {
          return;
        }

        // fix 'depends_on'
        let dependsOn = recipe.services[serviceName].depends_on || [],
          index = dependsOn.indexOf(name);
        if (index > -1) {
          dependsOn.splice(index, 1);
          if (dependsOn.length === 0) {
            delete recipe.services[serviceName].depends_on;
          }
        }

        // fix 'links'
        let links = recipe.services[serviceName].links || [],
          re = new RegExp('^' + name + '(?:$|:(.+))');
        for (let i = 0; i < links.length; i++) {
          if (re.test(links[i])) {
            links.splice(i, 1);
            break;
          }
        }
        if (links.length === 0) {
          delete recipe.services[serviceName].links;
        }
      });

      // delete machine from recipe
      delete recipe.services[name];

      // try to update recipe
      environment.recipe.content = this.stringifyRecipe(recipe);

      // and then update config
      delete environment.machines[name];
    } catch (e) {
      this.$log.error('Cannot delete machine, error: ', e);
    }

    return environment;
  }
}
