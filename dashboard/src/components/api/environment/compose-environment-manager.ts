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
 *services:
 *  devmachine:
 *    image: codenvy/ubuntu_jdk8
 *    depends_on:
 *      - anotherMachine
 *    mem_limit: 2147483648
 *  anotherMachine:
 *    image: codenvy/ubuntu_jdk8
 *    depends_on:
 *      - thirdMachine
 *    mem_limit: 1073741824
 *  thirdMachine:
 *    image: codenvy/ubuntu_jdk8
 *    mem_limit: 512741824
 *    labels:
 *      com.example.description: "Accounting webapp"
 *      com.example.department: "Finance"
 *      com.example.label-with-empty-value: ""
 *    environment:
 *      SOME_ENV: development
 *      SHOW: 'true'
 *      SESSION_SECRET:
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

  constructor($log) {
    super();

    this.$log = $log;
  }

  get editorMode() {
    return 'text/x-yaml';
  }

  /**
   * Parses recipe content
   *
   * @param content {string} recipe content
   * @returns {object} recipe object
   */
  _parseRecipe(content) {
    let recipe = {};
    try {
      recipe = jsyaml.load(content);
    } catch (e) {
      this.$log.error(e);
    }
    return recipe;
  }

  /**
   * Dumps recipe object
   *
   * @param recipe {object} recipe object
   * @returns {string} recipe content
   */

  _stringifyRecipe(recipe) {
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
  getMachines(environment) {
    let recipe = null,
        machines = [],
        machineNames;

    if (environment.recipe.content) {
      recipe = this._parseRecipe(environment.recipe.content);
      machineNames = Object.keys(recipe.services);
    } else {
      machineNames = Object.keys(environment.machines);
    }

    machineNames.forEach((machineName) => {
      let machine = angular.copy(environment.machines[machineName]) || {};
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
  getEnvironment(environment, machines) {
    let newEnvironment = super.getEnvironment(environment, machines);

    if (newEnvironment.recipe.content) {
      let recipe = this._parseRecipe(newEnvironment.recipe.content);

      machines.forEach((machine) => {
        let machineName = machine.name;
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

    return newEnvironment;
  }

  /**
   * Returns object which contains docker image or link to docker file and build context.
   *
   * @param machine {object}
   * @returns {*}
   */
  getSource(machine) {
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
  canEditEnvVariables(machine) {
    return !!machine.recipe;
  }

  /**
   * Returns object with environment variables.
   *
   * @param machine {object}
   * @returns {*}
   */
  getEnvVariables(machine) {
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
  setEnvVariables(machine, envVariables) {
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
  canRenameMachine(machine) {
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
  renameMachine(environment, oldName, newName) {
    try {
      let recipe = this._parseRecipe(environment.recipe.content);

      // fix relations to other machines in recipe
      Object.keys(recipe.services).forEach((serviceName) => {
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
  canDeleteMachine(machine) {
    return !!machine.recipe;
  }

  /**
   * Removes machine.
   *
   * @param environment {object}
   * @param name {string} name of machine
   * @returns {*} new environment
   */
  deleteMachine(environment, name) {
    try {
      let recipe = this._parseRecipe(environment.recipe.content);

      // fix relations to other machines in recipe
      Object.keys(recipe.services).forEach((serviceName) => {
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
