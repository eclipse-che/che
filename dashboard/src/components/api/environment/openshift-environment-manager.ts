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
import {CheRecipeTypes} from '../recipe/che-recipe-types';
import {IPodList, OpenshiftEnvironmentRecipeParser} from './openshift-environment-recipe-parser';
import {IPodItem, IPodItemContainer, OpenshiftMachineRecipeParser} from './openshift-machine-recipe-parser';

enum MemoryUnit { 'B', 'Ki', 'Mi', 'Gi' }


/**
 * This is the implementation of environment manager that handles the openshift format.
 *
 * Format sample and specific description:
 * <code>
 * kind: List
 * items:
 * -
 *   apiVersion: v1
 *   kind: Pod
 *   metadata:
 *     name: podName
 *   spec:
 *     containers:
 *     -
 *       image: rhche/centos_jdk8:latest
 *       name: containerName
 * </code>
 *
 *
 * The recipe type is <code>openshift</code>.
 * Machines are described both in recipe and in machines attribute of the environment (machine configs).
 * The machine configs contain memoryLimitBytes in attributes, servers and agent.
 * Environment variables can be set only in recipe content.
 *
 *  @author Oleksii Orel
 */

export class OpenshiftEnvironmentManager extends EnvironmentManager {
  private openshiftEnvironmentRecipeParser: OpenshiftEnvironmentRecipeParser;
  private openshiftMachineRecipeParser: OpenshiftMachineRecipeParser;

  constructor($log: ng.ILogService) {
    super($log);

    this.openshiftEnvironmentRecipeParser = new OpenshiftEnvironmentRecipeParser();
    this.openshiftMachineRecipeParser = new OpenshiftMachineRecipeParser();
  }

  get type(): string {
    return CheRecipeTypes.OPENSHIFT;
  }

  get editorMode(): string {
    return 'text/x-yaml';
  }

  /**
   * Parses machine recipe content.
   * @param content {string} recipe content
   * @returns {IPodItem} recipe object
   */
  parseMachineRecipe(content: string): IPodItem {
    return this.openshiftMachineRecipeParser.parse(content);
  }

  /**
   * Parses recipe content.
   * @param content {string} recipe content
   * @returns {IPodList} recipe object
   */
  parseRecipe(content: string): IPodList {
    return this.openshiftEnvironmentRecipeParser.parse(content);
  }

  /**
   * Dumps recipe object.
   * @param recipe {IPodList} recipe object
   * @returns {string} recipe content
   */

  stringifyRecipe(recipe: IPodList): string {
    return this.openshiftEnvironmentRecipeParser.dump(recipe);
  }

  /**
   * Retrieves the list of machines.
   * @param {che.IWorkspaceEnvironment} environment environment's configuration
   * @param {any=} runtime runtime of active environment
   * @returns {IEnvironmentManagerMachine[]} list of machines defined in environment
   */
  getMachines(environment: che.IWorkspaceEnvironment, runtime?: any): Array<IEnvironmentManagerMachine> {
    const machines: Array<IEnvironmentManagerMachine> = super.getMachines(environment, runtime);
    if (environment && environment.recipe && environment.recipe.content) {
      let recipe: IPodList;
      try {
        recipe = this.parseRecipe(environment.recipe.content);
      } catch (e) {
        this.$log.error('EnvironmentManager: cannot parse recipe.');
        return machines;
      }

      recipe.items.forEach((podItem: IPodItem) => {
        if (!podItem || podItem.kind.toString().toLowerCase() !== 'pod' || !podItem.metadata.name && podItem.spec || !angular.isArray(podItem.spec.containers)) {
          return;
        }
        podItem.spec.containers.forEach((container: IPodItemContainer) => {
          if (!container && !container.name) {
            return;
          }
          const podName = podItem.metadata.name ? podItem.metadata.name : podItem.metadata.generateName;
          const containerName: string = `${podName}/${container.name}`;
          let machine: IEnvironmentManagerMachine = machines.find((_machine: IEnvironmentManagerMachine) => {
            return _machine.name === containerName;
          });
          if (!machine) {
            machine = {name: containerName};
            machines.push(machine);
          }
          const machinePodItem = angular.copy(podItem);
          machinePodItem.spec.containers = [container];
          machine.recipe = machinePodItem;

          if (environment.machines && environment.machines[containerName]) {
            angular.merge(machine, environment.machines[containerName]);
          }

          // memory
          let memoryLimitBytes = this.getMemoryLimit(machine);
          if (memoryLimitBytes !== -1) {
            return machines;
          }
          const containerMemoryLimitBytes = this.getContainerMemoryLimit(container);
          if (containerMemoryLimitBytes !== -1) {
            this.setMemoryLimit(machine, containerMemoryLimitBytes);
          } else {
            // set default value of memory limit
            this.setMemoryLimit(machine, this.DEFAULT_MEMORY_LIMIT);
          }
        });
      });
    }
    return machines;
  }

  /**
   * Provides the environment configuration based on machines format.
   * @param {che.IWorkspaceEnvironment} environment origin of the environment to be edited
   * @param {IEnvironmentManagerMachine} machines the list of machines
   * @returns {che.IWorkspaceEnvironment} environment's configuration
   */
  getEnvironment(environment: che.IWorkspaceEnvironment, machines: IEnvironmentManagerMachine[]): che.IWorkspaceEnvironment {
    const newEnvironment = super.getEnvironment(environment, machines);
    if (environment && environment.recipe && environment.recipe.content) {
      let recipe: IPodList;
      try {
        recipe = this.parseRecipe(newEnvironment.recipe.content);
      } catch (e) {
        this.$log.error('EnvironmentManager: cannot parse recipe.');
        return environment;
      }
      if (recipe) {
        machines.forEach((machine: IEnvironmentManagerMachine) => {
          const [podName, containerName] = this.splitName(machine.name);
          if (podName && containerName) {
            let item;
            if (recipe.kind.toString().toLowerCase() === 'list' && angular.isArray(recipe.items)) {
              item = recipe.items.find((podItem: any) => {
                const podItemName = podItem.metadata.name ? podItem.metadata.name : podItem.metadata.generateName;
                return podItemName === podName;
              });
            }
            if (item && item.kind.toString().toLowerCase() === 'pod' && item.metadata.name && item.spec && angular.isArray(item.spec.containers)) {
              const containerIndex = item.spec.containers.findIndex((container: any) => {
                return container.name === containerName;
              });
              if (containerIndex !== -1 && item.spec.containers && machine.recipe && machine.recipe.spec && machine.recipe.spec.containers) {
                item.spec.containers[containerIndex] = machine.recipe.spec.containers[containerIndex];
              }
            }
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
   * @param {IEnvironmentManagerMachine} machine
   * @returns {*}
   */
  getSource(machine: IEnvironmentManagerMachine): any {
    const container = this.getPodContainer(machine);
    if (container && container.image) {
      return container.image;
    }

    return null;
  }

  /**
   * Updates machine's image
   * @param {IEnvironmentManagerMachine} machine
   * @param {String} image
   */
  setSource(machine: IEnvironmentManagerMachine, image: string) {
    const container = this.getPodContainer(machine);
    if (!container || !container.image) {
      return;
    }
    container.image = image;
  }

  /**
   * Retrieves the machines name.
   * @param {IEnvironmentManagerMachine} machine
   * @returns {string}
   */
  getMachineName(machine: IEnvironmentManagerMachine): string {
    if (!machine && !machine.name) {
      return '';
    }
    const machineRecipe = machine.recipe;
    if (machineRecipe && machineRecipe.spec && machineRecipe.spec.containers && machineRecipe.spec.containers.length === 1) {
      return machineRecipe.spec.containers[0].name;
    }
    const [, containerName] = this.splitName(machine.name);

    return containerName;
  }

  /**
   * Renames machine.
   * @param {che.IWorkspaceEnvironment} environment
   * @param {string} fullOldName
   * @param {string} newName
   * @returns {che.IWorkspaceEnvironment} new environment
   */
  renameMachine(environment: che.IWorkspaceEnvironment, fullOldName: string, newName: string): che.IWorkspaceEnvironment {
    let [, newMachineName] = this.splitName(newName);
    if (!newMachineName) {
      newMachineName = newName;
    }
    const environmentRecipe = environment && environment.recipe ? environment.recipe.content : null;
    if (!environmentRecipe || !fullOldName || !newMachineName) {
      this.$log.error('EnvironmentManager: cannot rename machine.');
      return environment;
    }
    try {
      const recipe = <IPodList>this.parseRecipe(environment.recipe.content);
      const [podName, containerName] = this.splitName(fullOldName);
      if (podName && containerName) {
        let item;
        if (recipe && recipe.kind.toString().toLowerCase() === 'list' && angular.isArray(recipe.items)) {
          item = recipe.items.find((podItem: any) => {
            const podItemName = podItem.metadata.name ? podItem.metadata.name : podItem.metadata.generateName;
            return podItemName === podName;
          });
        }
        if (item && item.kind.toString().toLowerCase() === 'pod' && item.metadata.name && item.spec && angular.isArray(item.spec.containers)) {
          const containerIndex = item.spec.containers.findIndex((container: any) => {
            return container.name === containerName;
          });
          // rename machine in recipe
          if (containerIndex > -1) {
            item.spec.containers[containerIndex].name = newMachineName;
            const newEnvironment = angular.copy(environment);
            newEnvironment.recipe.content = this.stringifyRecipe(recipe);
            newEnvironment.machines[newName] = environment.machines[fullOldName];
            delete newEnvironment.machines[fullOldName];
            return newEnvironment;
          }
        } else {
          this.$log.error('EnvironmentManager: cannot rename machine.');
        }
      }
    } catch (e) {
      this.$log.error('EnvironmentManager: cannot rename machine.');
    }

    return environment;
  }

  /**
   * Gets unique name for new machine based on prefix.
   * @param {che.IWorkspaceEnvironment}environment
   * @returns {string}
   */
  getUniqueMachineName(environment: che.IWorkspaceEnvironment): string {
    const usedMachinesNames: Array<string> = environment && environment.machines ? Object.keys(environment.machines) : [];
    const podNames = usedMachinesNames.map((name: string) => {
      return this.splitName(name)[0];
    });
    let namePrefix = 'pod';
    for (let pos = 1; pos < 1000; pos++) {
      if (podNames.indexOf(namePrefix + pos.toString()) === -1) {
        namePrefix += pos.toString();
        break;
      }
    }
    namePrefix += '/machine';

    return super.getUniqueMachineName(environment, namePrefix);
  }

  /**
   * Create a new default machine.
   * @param {che.IWorkspaceEnvironment} environment
   * @param {string} image
   * @return {IEnvironmentManagerMachine}
   */
  createNewDefaultMachine(environment: che.IWorkspaceEnvironment, image?: string): IEnvironmentManagerMachine {
    const uniqueMachineName = this.getUniqueMachineName(environment);
    const [podName, containerName] = this.splitName(uniqueMachineName);
    const machineImage = !image ? 'rhche/centos_jdk8:latest' : image;

    return {
      name: uniqueMachineName,
      installers: [this.TERMINAL_AGENT_NAME],
      attributes: {
        memoryLimitBytes: this.DEFAULT_MEMORY_LIMIT
      },
      recipe: jsyaml.load(`apiVersion: v1\nkind: Pod\nmetadata:\n  name: ${podName}\nspec:\n  containers:\n    -\n      image: ${machineImage}\n      name: ${containerName}`)
    };
  }

  /**
   * Add machine.
   * @param {che.IWorkspaceEnvironment} environment
   * @param {IEnvironmentManagerMachine} machine
   * @return {che.IWorkspaceEnvironment}
   */
  addMachine(environment: che.IWorkspaceEnvironment, machine: IEnvironmentManagerMachine): che.IWorkspaceEnvironment {
    const machineRecipe = machine ? machine.recipe : null;
    const environmentRecipe = environment && environment.recipe ? environment.recipe.content : null;
    if (!environmentRecipe || !machineRecipe) {
      this.$log.error('EnvironmentManager: cannot add machine.');
      return environment;
    }
    try {
      const recipe = <IPodList>this.parseRecipe(environmentRecipe);
      if (recipe && angular.isArray(recipe.items) && angular.isObject(machineRecipe)) {
        recipe.items.push(machineRecipe);
        // try to update recipe
        environment.recipe.content = this.stringifyRecipe(recipe);
        // update machine name
        if (machineRecipe.metadata) {
          const [, containerName] = this.splitName(machine.name);
          const podItemName = machineRecipe.metadata.name ? machineRecipe.metadata.name : machineRecipe.metadata.generateName;
          machine.name = `${podItemName}/${containerName}`;
        }
        const copyMachine = angular.copy(machine);
        delete copyMachine.recipe;
        const containerName = copyMachine.name;
        delete copyMachine.name;

        environment.machines[containerName] = copyMachine;

        return environment;
      }
    } catch (error) {
      this.$log.error('EnvironmentManager: cannot add machine.');
    }

    return environment;
  }

  /**
   * Removes machine.
   * @param {che.IWorkspaceEnvironment} environment
   * @param {string} name name of machine
   * @returns {che.IWorkspaceEnvironment} new environment
   */
  deleteMachine(environment: che.IWorkspaceEnvironment, name: string): che.IWorkspaceEnvironment {
    if (!environment || !environment.recipe || !environment.recipe.content || !name) {
      return environment;
    }
    environment = angular.copy(environment);
    if (environment.machines[name]) {
      delete environment.machines[name];
    }
    try {
      const envRecipe = this.parseRecipe(environment.recipe.content);
      if (!envRecipe || !angular.isArray(envRecipe.items)) {
        return environment;
      }
      const [podName, containerName] = this.splitName(name);

      const podIndex = envRecipe.items.findIndex((machinePodItem: IPodItem) => {
        const podItemName = machinePodItem.metadata.name ? machinePodItem.metadata.name : machinePodItem.metadata.generateName;
        return podName === podItemName;
      });
      if (podIndex > -1) {
        const podItem = envRecipe.items[podIndex];
        if (podItem && podItem.kind.toString().toLowerCase() === 'pod' && podItem.metadata.name && podItem.spec && angular.isArray(podItem.spec.containers)) {
          if (podItem.spec.containers.length) {
            const containerIndex = podItem.spec.containers.findIndex((podItemContainer: IPodItemContainer) => {
              return podItemContainer.name === containerName;
            });
            // delete needed containers in the pod
            if (containerIndex > -1) {
              podItem.spec.containers.splice(containerIndex, 1);
            }
          }
          // in case with empty pod
          if (!podItem.spec.containers.length) {
            // delete pod
            envRecipe.items.splice(podIndex, 1);
          }
        } else {
          this.$log.error('Cannot delete machine.');
        }
      } else {
        this.$log.error('Cannot delete machine.');
      }
      environment.recipe.content = this.stringifyRecipe(envRecipe);
    } catch (e) {
      this.$log.error('Cannot delete machine, error: ', e);
    }

    return environment;
  }

  /**
   * Returns memory limit from machine's attributes
   * @param {IEnvironmentManagerMachine} machine
   * @returns {number} memory limit in bytes
   */
  getMemoryLimit(machine: IEnvironmentManagerMachine): number {
    const memoryLimitBytes = super.getMemoryLimit(machine);
    if (memoryLimitBytes !== -1) {
      return memoryLimitBytes;
    }

    return this.getContainerMemoryLimit(this.getPodContainer(machine));
  }

  /**
   * Sets the memory limit of the pointed machine.
   * Value in attributes has the highest priority,
   * @param {IEnvironmentManagerMachine} machine machine to change memory limit
   * @param {number} memoryLimitBytes
   */
  setMemoryLimit(machine: IEnvironmentManagerMachine, memoryLimitBytes: number): void {
    super.setMemoryLimit(machine, memoryLimitBytes);
    this.setContainerMemoryLimit(this.getPodContainer(machine), memoryLimitBytes);
  }

  /**
   * Gets pod container from machine recipe.
   * @param {IEnvironmentManagerMachine} machine
   * @returns {IPodItemContainer}
   */
  private getPodContainer(machine: IEnvironmentManagerMachine): IPodItemContainer {
    if (!machine || !machine.name || !machine.recipe) {
      return null;
    }
    const [podName, containerName] = this.splitName(machine.name);
    return this.getPodContainersByPodName([machine.recipe], podName).find((podItemContainer: IPodItemContainer) => {
      return podItemContainer && podItemContainer.name === containerName;
    });
  }

  /**
   * Gets pod containers from podItems by pod name.
   * @param {Array<IPodItem>} podItems
   * @param {string} podName
   * @returns {Array<IPodItemContainer>}
   */
  private getPodContainersByPodName(podItems: Array<IPodItem>, podName: string): Array<IPodItemContainer> {
    let containers: Array<IPodItemContainer> = [];
    if (!podItems || !podName) {
      return [];
    }
    const machinePodItems: Array<IPodItem> = angular.isArray(podItems) ? podItems : [];
    machinePodItems.forEach((machinePodItem: IPodItem) => {
      const podItemName = machinePodItem.metadata.name ? machinePodItem.metadata.name : machinePodItem.metadata.generateName;
      if (podItemName === podName && machinePodItem.kind.toString().toLowerCase() === 'pod' && machinePodItem.spec && angular.isArray(machinePodItem.spec.containers)) {
        containers = machinePodItem.spec.containers;
        return;
      }
    });

    return containers;
  }

  /**
   * Returns container's memory limit.
   * @param {IPodItemContainer} container
   * @returns {number}
   */
  private getContainerMemoryLimit(container: IPodItemContainer): number {
    if (!container || !container.resources || !container.resources.limits || !container.resources.limits.memory) {
      return -1;
    }
    const regExpExecArray = /^([0-9]+)([a-zA-Z]{1,3})$/.exec(container.resources.limits.memory);
    if (regExpExecArray === null) {
      return -1;
    }
    const [, memoryLimitNumber, memoryLimitUnit] = regExpExecArray;
    const power = MemoryUnit[memoryLimitUnit];
    if (!power) {
      return -1;
    }

    return parseInt(memoryLimitNumber, 10) * Math.pow(1024, power);
  }

  /**
   * Sets container's memory limit.
   * @param {IPodItemContainer} container
   * @param {number} memoryLimitBytes
   */
  private setContainerMemoryLimit(container: IPodItemContainer, memoryLimitBytes: number): void {
    if (!container) {
      return;
    }
    if (!container.resources) {
      container.resources = {};
    }
    if (!container.resources.limits) {
      container.resources.limits = {};
    }
    const memoryUnit = MemoryUnit.Mi;
    container.resources.limits.memory = (memoryLimitBytes / (Math.pow(1024, memoryUnit))).toString() + MemoryUnit[memoryUnit];
  }

  /**
   * Splits machine name to podName and containerName.
   * @param {string} machineName
   * @returns {Array<string>}
   */
  private splitName(machineName: string): Array<string> {
    return machineName.split(/\//);
  }
}
