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

import {EnvironmentManager} from './environment-manager';
import {DockerfileParser} from './docker-file-parser';
import {IEnvironmentManagerMachine} from './environment-manager-machine';

/**
 * This is the implementation of environment manager that handles the docker file format of environment.
 *
 * Format sample and specific description:
 * <code>
 * FROM ubuntu
 * RUN mkdir /var/www
 * ADD app.js /var/www/app.js
 * CMD ["/usr/bin/node", "/var/www/app.js"]
 * </code>
 *
 * The recipe type is <code>dockerfile</code>. This environment can contain only one machine.
 * Machine is described both in recipe (content or location to recipe) and in machines attribute of the environment (machine configs).
 * The machine configs contain memoryLimitBytes in attributes, servers and agent.
 * Environment variables can be set only in recipe content.
 *
 * @author Ann Shumilova
 */

const DOCKERFILE = 'dockerfile';
const ENV_INSTRUCTION: string = 'ENV';
const FROM_INSTRUCTION: string = 'FROM';

export class DockerFileEnvironmentManager extends EnvironmentManager {
  parser: DockerfileParser;

  constructor($log: ng.ILogService) {
    super($log);

    this.parser = new DockerfileParser();
  }

  get type(): string {
    return DOCKERFILE;
  }

  get editorMode(): string {
    return 'text/x-dockerfile';
  }

  /**
   * Create a new default machine.
   *
   * @param {che.IWorkspaceEnvironment} environment
   *
   * @return {IEnvironmentManagerMachine}
   */
  createNewDefaultMachine(environment: che.IWorkspaceEnvironment): IEnvironmentManagerMachine {
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
   * Parses a dockerfile and returns an array of objects
   *
   * @param content {string} content of dockerfile
   * @returns {Array} a list of instructions and arguments
   * @private
   */
  _parseRecipe(content: string): any[] {
    let recipe: any[] = null;
    try {
      recipe = this.parser.parse(content);
    } catch (e) {
      this.$log.error(e);
    }
    return recipe;
  }

  /**
   * Dumps a list of instructions and arguments into dockerfile
   *
   * @param instructions {Array} array of objects
   * @returns {string} dockerfile
   * @private
   */
  _stringifyRecipe(instructions: any[]): string {
    let content = '';

    try {
      content = this.parser.dump(instructions);
    } catch (e) {
      this.$log.log(e);
    }

    return content;
  }

  /**
   * Provides the environment configuration based on machines format.
   *
   * @param {che.IWorkspaceEnvironment} environment origin of the environment to be edited
   * @param {IEnvironmentManagerMachine[]} machines the list of machines
   * @returns {che.IWorkspaceEnvironment} environment's configuration
   */
  getEnvironment(environment: che.IWorkspaceEnvironment, machines: IEnvironmentManagerMachine[]): che.IWorkspaceEnvironment {
    let newEnvironment = super.getEnvironment(environment, machines);

    // machines should contain one machine only
    if (machines && machines[0] && machines[0].recipe) {
      try {
        newEnvironment.recipe.content = this._stringifyRecipe(machines[0].recipe);
      } catch (e) {
        this.$log.error('Cannot retrieve environment\'s recipe, error: ', e);
      }
    }

    return newEnvironment;
  }

  /**
   * Retrieves the list of machines.
   *
   * @param {che.IWorkspaceEnvironment} environment environment's configuration
   * @param {any=} runtime runtime of active environment
   * @returns {IEnvironmentManagerMachine[]} list of machines defined in environment
   */
  getMachines(environment: che.IWorkspaceEnvironment, runtime?: any): IEnvironmentManagerMachine[] {
    let recipe = null,
        machines: IEnvironmentManagerMachine[] = super.getMachines(environment, runtime);

    if (environment.recipe.content) {
      recipe = this._parseRecipe(environment.recipe.content);
    }

    Object.keys(environment.machines).forEach((machineName: string) => {
      let machine: IEnvironmentManagerMachine = machines.find((_machine: IEnvironmentManagerMachine) => {
        return _machine.name === machineName;
      });

      if (!machine) {
        machine = {name: machineName};
        machines.push(machine);
      }

      angular.merge(machine, environment.machines[machineName]);
      machine.recipe = recipe;
    });

    return machines;
  }

  /**
   * Returns a docker image from the recipe.
   *
   * @param {IEnvironmentManagerMachine} machine
   * @returns {*}
   */
  getSource(machine: IEnvironmentManagerMachine): any {
    if (!machine.recipe) {
      return null;
    }

    let from = machine.recipe.find((line: any) => {
      return line.instruction === 'FROM';
    });

    return {image: from.argument};
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

    // new recipe without 'FROM' instruction
    let newRecipe = machine.recipe.filter((line: any) => {
      return line.instruction !== FROM_INSTRUCTION;
    });

    // update recipe with new source
    let from = {
      instruction: FROM_INSTRUCTION,
      argument: image
    };
    newRecipe.splice(0, 0, from);

    machine.recipe = newRecipe;
  }

  /**
   * Returns true if environment recipe content is present.
   *
   * @param {IEnvironmentManagerMachine} machine
   * @returns {boolean}
   */
  canEditEnvVariables(machine: IEnvironmentManagerMachine): boolean {
    return !!machine.recipe;
  }

  /**
   * Returns environment variables from recipe
   *
   * @param {IEnvironmentManagerMachine} machine
   * @returns {*}
   */
  getEnvVariables(machine: IEnvironmentManagerMachine): any {
    if (!machine.recipe) {
      return null;
    }

    let envVariables = {};

    let envList = machine.recipe.filter((line: any) => {
      return line.instruction === ENV_INSTRUCTION;
    }) || [];

    envList.forEach((line: any) => {
      envVariables[line.argument[0]] = line.argument[1];
    });

    return envVariables;
  }

  /**
   * Updates machine with new environment variables.
   *
   * @param {IEnvironmentManagerMachine} machine
   * @param {any} envVariables
   */
  setEnvVariables(machine: IEnvironmentManagerMachine, envVariables: any): void {
    if (!machine.recipe) {
      return;
    }

    let newRecipe = [];

    // new recipe without any 'ENV' instruction
    newRecipe = machine.recipe.filter((line: any) => {
      return line.instruction !== ENV_INSTRUCTION;
    });

    // add environments if any
    if (Object.keys(envVariables).length) {
      Object.keys(envVariables).forEach((name: string) => {
        let line = {
          instruction: ENV_INSTRUCTION,
          argument: [name, envVariables[name]]
        };
        newRecipe.splice(1, 0, line);
      });
    }

    machine.recipe = newRecipe;
  }
}
