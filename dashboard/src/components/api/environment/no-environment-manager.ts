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
 * This is the implementation of empty environment manager.
 *
 * The recipe type is <code>dockerimage</code>. This environment can contain only an image.
 * Machine is described by image and in machines attribute of the environment (machine configs).
 * The machine configs contain memoryLimitBytes in attributes, servers and agent.
 * Environment variables can not be set.
 *
 * @author Ann Shumilova
 */
export class NoEnvironmentManager extends EnvironmentManager {
  
  parseRecipe(content: string) {
    this.$log.error('EnvironmentManager: cannot parse recipe.');
  }
  
  stringifyRecipe(recipeObj: any): string {
    this.$log.error('EnvironmentManager: cannot stringify a recipe.');
    return null;
  }

  constructor($log: ng.ILogService) {
    super($log);
  }

  get type(): string {
    return null;
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
    return null;
  }

  /**
   * Retrieves the list of machines.
   *
   * @param {che.IWorkspaceEnvironment} environment environment's configuration
   * @param {any=} runtime runtime of an active environment
   * @returns {IEnvironmentManagerMachine[]} list of machines defined in environment
   */
  getMachines(environment: che.IWorkspaceEnvironment, runtime?: any): IEnvironmentManagerMachine[] {
    this.$log.error('EnvironmentManager: cannot get machines.');

    return null;
  }

  /**
   * Provides the environment configuration based on machines format.
   *
   * @param {che.IWorkspaceEnvironment} environment origin of the environment to be edited
   * @param {IEnvironmentManagerMachine[]} machines the list of machines
   * @returns {che.IWorkspaceEnvironment} environment's configuration
   */
  getEnvironment(environment: che.IWorkspaceEnvironment, machines: IEnvironmentManagerMachine[]): che.IWorkspaceEnvironment {
    this.$log.error('EnvironmentManager: cannot get environment.');

    return null;
  }

  /**
   * Returns a dockerimage.
   *
   * @param {IEnvironmentManagerMachine} machine
   * @returns {{image: string}}
   */
  getSource(machine: IEnvironmentManagerMachine): any {
    this.$log.error('EnvironmentManager: cannot get source.');
    return null;
  }

  /**
   * Updates machine's image
   *
   * @param {IEnvironmentManagerMachine} machine
   * @param {String} image
   */
  setSource(machine: IEnvironmentManagerMachine, image: string): void {
    this.$log.error('EnvironmentManager: cannot set source.');
  }

}
