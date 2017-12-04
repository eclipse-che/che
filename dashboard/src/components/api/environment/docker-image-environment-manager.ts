/*
 * Copyright (c) 2015-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

import {EnvironmentManager} from './environment-manager';
import {IEnvironmentManagerMachine} from './environment-manager-machine';

/**
 * This is the implementation of environment manager that handles the docker image format of environment.
 *
 * Format sample and specific description:
 * <code>
 *     condevy/ubuntu_jdk8
 * </code>
 *
 * The recipe type is <code>dockerimage</code>. This environment can contain only an image.
 * Machine is described by image and in machines attribute of the environment (machine configs).
 * The machine configs contain memoryLimitBytes in attributes, servers and agent.
 * Environment variables can not be set.
 *
 * @author Ann Shumilova
 */
export class DockerImageEnvironmentManager extends EnvironmentManager {

  constructor($log: ng.ILogService) {
    super($log);
  }

  /**
   * Parses a dockerimages and returns an array of objects
   *
   * @param content {string} content of dockerfile
   * @returns {Array<string>} a list of arguments
   * @private
   */
  parseRecipe(content: string): Array<string> {
    if (/\s/.test(content)) {
      throw new TypeError('Cannot parse recipe image location');
    }

    return [content];
  }

  /**
   * Dumps a list of arguments into dockerimages
   *
   * @param instructions {Array<string>} array of objects
   * @returns {string} dockerfile
   * @private
   */
  stringifyRecipe(instructions: Array<string>): string {

    return instructions[0];
  }

  /**
   * Create a new default machine.
   * @param {che.IWorkspaceEnvironment} environment
   * @param {string} image
   * @return {IEnvironmentManagerMachine}
   */
  createNewDefaultMachine(environment: che.IWorkspaceEnvironment, image?: string): IEnvironmentManagerMachine {
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
   * Retrieves the list of machines.
   *
   * @param {che.IWorkspaceEnvironment} environment environment's configuration
   * @param {any=} runtime runtime of an active environment
   * @returns {IEnvironmentManagerMachine[]} list of machines defined in environment
   */
  getMachines(environment: che.IWorkspaceEnvironment, runtime?: any): IEnvironmentManagerMachine[] {
    let machines: IEnvironmentManagerMachine[] = super.getMachines(environment, runtime);

    Object.keys(environment.machines).forEach((machineName: string) => {
      let machine: IEnvironmentManagerMachine = machines.find((_machine: IEnvironmentManagerMachine) => {
        return _machine.name === machineName;
      });

      if (!machine) {
        machine = {name: machineName, recipe: this.parseRecipe(environment.recipe.location)};
        machines.push(machine);
      } else {
        machine.recipe = this.parseRecipe(environment.recipe.location);
      }
      angular.merge(machine, environment.machines[machineName]);
    });

    return machines;
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
    newEnvironment.recipe.location = this.stringifyRecipe(machines[0].recipe);

    return newEnvironment;
  }

  /**
   * Returns a dockerimage.
   *
   * @param {IEnvironmentManagerMachine} machine
   * @returns {{image: string}}
   */
  getSource(machine: IEnvironmentManagerMachine): any {
    return {image: this.stringifyRecipe(machine.recipe)};
  }

  /**
   * Updates machine's image
   *
   * @param {IEnvironmentManagerMachine} machine
   * @param {String} image
   */
  setSource(machine: IEnvironmentManagerMachine, image: string): void {
    machine.recipe = this.parseRecipe(image);
  }

}
