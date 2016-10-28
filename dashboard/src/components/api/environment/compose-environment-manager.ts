/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
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

interface IComposeRecipe {
  services: {
    [machineName: string]: any
  };
}

export class ComposeEnvironmentManager extends EnvironmentManager {
  $log: ng.ILogService;

  constructor($log: ng.ILogService) {
    super();

    this.$log = $log;
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
  _parseRecipe(content: string): IComposeRecipe {
    let recipe = null;
    try {
      recipe = this._validate(jsyaml.load(content));
    } catch (e) {
      this.$log.error(e);
    }
    return recipe;
  }

  /**
   * Validate given recipe
   *
   * @param {IComposeRecipe} recipe
   * @returns {IComposeRecipe | *}
   * @private
   */
  _validate(recipe: IComposeRecipe): IComposeRecipe | void {
    if (!recipe.services) {
      throw new Error('Recipe should contain services section.');
    }

    let services: any = Object.keys(recipe.services);
    services.forEach((serviceName: string) => {
      let serviceFields: any = Object.keys(recipe.services[serviceName] || {});
      if (!serviceFields || (serviceFields.includes('build') === false && serviceFields.includes('image') === false)) {
        throw new Error('Service \'' + serviceName + '\' should contain \'build\' or \'image\' section.');
      }
    });

    return recipe;
  }

  /**
   * Dumps recipe object
   *
   * @param recipe {object} recipe object
   * @returns {string} recipe content
   */

  _stringifyRecipe(recipe: IComposeRecipe): string {
    let content = '';
    try {
      content = jsyaml.dump(recipe);
    } catch (e) {
      this.$log.error(e);
    }

    return content;
  }

  /**
   * Retrieves the list of machines.
   *
   * @param environment environment's configuration
   * @returns {Array} list of machines defined in environment
   */
  getMachines(environment: any): any[] {
    let recipe: any = null,
        machines: any[] = [],
        machineNames: string[] = [];

    if (environment.recipe.content) {
      recipe = this._parseRecipe(environment.recipe.content);
      if (recipe) {
        machineNames = Object.keys(recipe.services);
      } else if (environment.machines) {
        machineNames = Object.keys(environment.machines);
      }
    } else if (environment.recipe.location) {
      machineNames = Object.keys(environment.machines);
    }

    machineNames.forEach((machineName: string) => {
      let machine: any = {};
      if (environment.machines && environment.machines[machineName]) {
        machine = angular.copy(environment.machines[machineName]);
      }
      machine.name = machineName;
      machine.recipe = recipe ? recipe.services[machineName] : recipe;

      // memory
      let memoryLimitBytes = this.getMemoryLimit(machine);
      if (memoryLimitBytes === -1 && recipe) {
        this.setMemoryLimit(machine, recipe.services[machineName].mem_limit);
      }
      machines.push(machine);
    });

    return machines;
  }

  /**
   * Provides the environment configuration based on machines format.
   *
   * @param environment origin of the environment to be edited
   * @param machines the list of machines
   * @returns environment's configuration
   */
  getEnvironment(environment: any, machines: any[]): any {
    let newEnvironment = super.getEnvironment(environment, machines);

    if (newEnvironment.recipe.content) {
      let recipe: IComposeRecipe = this._parseRecipe(newEnvironment.recipe.content);

      if (recipe) {
        machines.forEach((machine: any) => {
          let machineName = machine.name;
          if (!recipe.services[machineName]) {
            return;
          }
          if (machine.recipe.environment && Object.keys(machine.recipe.environment).length) {
            recipe.services[machineName].environment = angular.copy(machine.recipe.environment);
          } else {
            delete recipe.services[machineName].environment;
          }
        });

        try {
          newEnvironment.recipe.content = this._stringifyRecipe(recipe);
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
   * @param machine {object}
   * @returns {*}
   */
  getSource(machine: any): any {
    if (!machine.recipe) {
      return null;
    }

    if (machine.recipe.image) {
      return {image: machine.recipe.image};
    } else if (machine.recipe.build) {
      return machine.recipe.build;
    }
  }

  /**
   * Returns true if environment recipe content is present.
   *
   * @param machine {object}
   * @returns {boolean}
   */
  canEditEnvVariables(machine: any): boolean {
    return !!machine.recipe;
  }

  /**
   * Returns object with environment variables.
   *
   * @param machine {object}
   * @returns {*}
   */
  getEnvVariables(machine: any): any {
    if (!machine.recipe) {
      return null;
    }

    return machine.recipe.environment || {};
  }

  /**
   * Updates machine with new environment variables.
   *
   * @param machine {object}
   * @param envVariables {object}
   */
  setEnvVariables(machine: any, envVariables: any): void {
    if (!machine.recipe) {
      return;
    }

    if (Object.keys(envVariables).length) {
      machine.recipe.environment = angular.copy(envVariables);
    } else {
      delete machine.recipe.environment;
    }
  }

  /**
   * Returns true if machine can be renamed.
   *
   * @param machine {object}
   * @returns {boolean}
   */
  canRenameMachine(machine: any): boolean {
    return !!machine.recipe;
  }

  /**
   * Renames machine.
   *
   * @param environment {object}
   * @param oldName {string}
   * @param newName {string}
   * @returns {*} new environment
   */
  renameMachine(environment: any, oldName: string, newName: string): any {
    try {
      let recipe: IComposeRecipe = this._parseRecipe(environment.recipe.content);

      // fix relations to other machines in recipe
      Object.keys(recipe.services).forEach((serviceName: any) => {
        if (serviceName === oldName) {
          return;
        }

        // fix 'depends_on'
        let dependsOn = recipe.services[serviceName].depends_on || [],
            index = dependsOn.indexOf(oldName);
        if (index > -1) {
          dependsOn.splice(index, 1);
          dependsOn.push(newName);
        }

        // fix 'links'
        let links = recipe.services[serviceName].links || [],
            re = new RegExp('^' + oldName + '(?:$|:(.+))');
        for (let i = 0; i < links.length; i++) {
          if (re.test(links[i])) {
            let match = links[i].match(re),
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
      environment.recipe.content = this._stringifyRecipe(recipe);

      // and then update config
      environment.machines[newName] = environment.machines[oldName];
      delete environment.machines[oldName];
    } catch (e) {
      this.$log.error('Cannot rename machine, error: ', e);
    }

    return environment;
  }

  /**
   * Returns true if machine can be deleted.
   *
   * @param machine {object}
   * @returns {boolean}
   */
  canDeleteMachine(machine: any): boolean {
    return !!machine.recipe;
  }

  /**
   * Removes machine.
   *
   * @param environment {object}
   * @param name {string} name of machine
   * @returns {*} new environment
   */
  deleteMachine(environment: any, name: string): any {
    try {
      let recipe: IComposeRecipe = this._parseRecipe(environment.recipe.content);

      // fix relations to other machines in recipe
      Object.keys(recipe.services).forEach((serviceName: any) => {
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
      environment.recipe.content = this._stringifyRecipe(recipe);

      // and then update config
      delete environment.machines[name];
    } catch (e) {
      this.$log.error('Cannot delete machine, error: ', e);
    }

    return environment;
  }
}
