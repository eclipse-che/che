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
import {DockerimageParser, IDockerimage} from './docker-image-parser';
import {CheRecipeTypes} from '../recipe/che-recipe-types';

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

  parser: DockerimageParser;

  constructor($log: ng.ILogService) {
    super($log);

    this.parser = new DockerimageParser();
  }

  get type(): string {
    return CheRecipeTypes.DOCKERIMAGE;
  }

  /**
   * Parses a dockerimages and returns an object which contains repo and tag.
   *
   * @param image {string}
   * @returns {IDockerimage}
   * @private
   */
  parseRecipe(image: string): IDockerimage {
    let imageObj = null;
    try {
      imageObj = this.parser.parse(image);
    } catch (e) {
      this.$log.error(e);
    }
    return imageObj;
  }

  /**
   * Dumps an object with repo and tag into dockerimage.
   *
   * @param imageObj {IDockerimage} array of objects
   * @returns {string} dockerfile
   * @private
   */
  stringifyRecipe(imageObj: IDockerimage): string {
    let image = '';

    try {
      image = this.parser.dump(imageObj);
    } catch (e) {
      this.$log.log(e);
    }

    return image;
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
        machine = {name: machineName, recipe: this.parseRecipe(environment.recipe.content)};
        machines.push(machine);
      } else {
        machine.recipe = this.parseRecipe(environment.recipe.content);
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
    newEnvironment.recipe.content = this.stringifyRecipe(machines[0].recipe);

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
