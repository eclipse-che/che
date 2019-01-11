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
import {CheRecipeTypes} from '../recipe/che-recipe-types';
import {ISupportedItemList, KubernetesEnvironmentRecipeParser} from './kubernetes-environment-recipe-parser';
import {ISupportedListItem, IPodItem, IPodItemContainer, KubernetesMachineRecipeParser, getPodItemOrNull} from './kubernetes-machine-recipe-parser';

enum MemoryUnit { 'B', 'Ki', 'Mi', 'Gi' }

const POD: string = 'Pod';
const LIST: string = 'List';
const MACHINE_NAME: string = 'machine_name';
const NAME_ANNOTATION_PREFIX: string = 'org.eclipse.che.container';

/**
 * This is the implementation of environment manager that handles the kubernetes format.
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
 * The recipe type is <code>kubernetes</code>.
 * Machines are described both in recipe and in machines attribute of the environment (machine configs).
 * The machine configs contain memoryLimitBytes in attributes, servers and agent.
 *
 *  @author Oleksii Orel
 */

export class KubernetesEnvironmentManager extends EnvironmentManager {
  parser: KubernetesEnvironmentRecipeParser;
  private machineParser: KubernetesMachineRecipeParser;

  constructor($log: ng.ILogService) {
    super($log);

    this.parser = new KubernetesEnvironmentRecipeParser();
    this.machineParser = new KubernetesMachineRecipeParser();
  }

  get type(): string {
    return CheRecipeTypes.KUBERNETES;
  }

  get editorMode(): string {
    return 'text/x-yaml';
  }

  /**
   * Parses machine recipe content.
   * @param content {string} recipe content
   * @returns {ISupportedListItem} recipe object
   */
  parseMachineRecipe(content: string): ISupportedListItem {
    return this.machineParser.parse(content);
  }

  /**
   * Parses recipe content.
   * @param content {string} recipe content
   * @returns {ISupportedItemList} recipe object
   */
  parseRecipe(content: string): ISupportedItemList {
    let recipe: ISupportedItemList;
    try {
      recipe = this.parser.parse(content);
    } catch (e) {
      this.$log.error(e);
    }
    return recipe;
  }

  /**
   * Dumps recipe object.
   * @param recipe {ISupportedItemList} recipe object
   * @returns {string} recipe content
   */
  stringifyRecipe(recipe: ISupportedItemList): string {
    return this.parser.dump(recipe);
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

    const recipe: ISupportedItemList = this.parseRecipe(environment.recipe.content);
    if (!recipe) {
      this.$log.error('EnvironmentManager: cannot parse recipe.');
      return machines;
    }

    recipe.items.forEach((item: ISupportedListItem) => {
      const podItem = getPodItemOrNull(item);
      if (!podItem) {
        return;
      }

      const annotations = podItem.metadata.annotations;
      podItem.spec.containers.forEach((container: IPodItemContainer) => {
        if (!container || !container.name) {
          return;
        }
        const podName = podItem.metadata.name ? podItem.metadata.name : podItem.metadata.generateName;
        const nameAnnotation = `${NAME_ANNOTATION_PREFIX}.${container.name}.${MACHINE_NAME}`;
        const machineName = annotations && annotations[nameAnnotation] ? annotations[nameAnnotation] : `${podName}/${container.name}`;
        let machine: IEnvironmentManagerMachine = machines.find((_machine: IEnvironmentManagerMachine) => {
          return _machine.name === machineName;
        });
        if (!machine) {
          machine = {name: machineName};
          machines.push(machine);
        }
        const machinePodItem = angular.copy(podItem);
        machinePodItem.spec.containers = [container];
        machine.recipe = machinePodItem;

        if (environment.machines && environment.machines[machineName]) {
          angular.merge(machine, environment.machines[machineName]);
        }
        // memory
        let memoryLimitBytes = this.getMemoryLimit(machine);
        if (memoryLimitBytes !== -1) {
          return;
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

    return machines;
  }

  /**
   * Provides the environment configuration based on machines format.
   * @param {che.IWorkspaceEnvironment} environment origin of the environment to be edited
   * @param {IEnvironmentManagerMachine} machines the list of machines
   * @returns {che.IWorkspaceEnvironment} environment's configuration
   */
  getEnvironment(environment: che.IWorkspaceEnvironment, machines: IEnvironmentManagerMachine[]): che.IWorkspaceEnvironment {
    let newEnvironment: che.IWorkspaceEnvironment = angular.copy(environment);

    machines.forEach((machine: IEnvironmentManagerMachine) => {
      const podItem = machine.recipe;
      const podItemContainer = machine.recipe.spec.containers[0];
      const annotations = podItem.metadata.annotations;
      const podName = podItem.metadata.name ? podItem.metadata.name : podItem.metadata.generateName;
      const nameAnnotation = `${NAME_ANNOTATION_PREFIX}.${podItemContainer.name}.${MACHINE_NAME}`;
      const machineName = annotations && annotations[nameAnnotation] ? annotations[nameAnnotation] : `${podName}/${podItemContainer.name}`;

      if (angular.isUndefined(newEnvironment.machines)) {
        newEnvironment.machines = {};
      }
      if (angular.isUndefined(newEnvironment.machines[machineName])) {
        newEnvironment.machines[machineName] = {attributes: {}};
      }
      newEnvironment.machines[machineName].attributes.memoryLimitBytes = machine.attributes ? machine.attributes.memoryLimitBytes : super.DEFAULT_MEMORY_LIMIT;
      newEnvironment.machines[machineName].installers = angular.copy(machine.installers);
      newEnvironment.machines[machineName].servers = angular.copy(machine.servers);
      newEnvironment.machines[machineName].volumes = angular.copy(machine.volumes);
      newEnvironment.machines[machineName].env = angular.copy(machine.env);
    });

    if (!environment || !environment.recipe || !environment.recipe.content) {
      return newEnvironment;
    }
    const recipe = this.parseRecipe(newEnvironment.recipe.content);
    if (!recipe) {
      this.$log.error('EnvironmentManager: cannot parse recipe.');
      return newEnvironment;
    }
    if (!recipe || recipe.kind !== LIST || !angular.isArray(recipe.items)) {
      return newEnvironment;
    }
    machines.forEach((machine: IEnvironmentManagerMachine) => {
      let containerName: string;
      let podOrDeployment = recipe.items.find((item: ISupportedListItem) => {
        const podItem = getPodItemOrNull(item);
        if (!podItem || !podItem.metadata) {
          return false;
        }
        const podItemName = podItem.metadata.name ? podItem.metadata.name : podItem.metadata.generateName;
        if (podItem.metadata.annotations) {
          const nameAnnotation = Object.keys(podItem.metadata.annotations).find((annotation: string) => {
            return podItem.metadata.annotations[annotation] === machine.name;
          });
          if (nameAnnotation) {
            const regExpExecArray = new RegExp(`^${NAME_ANNOTATION_PREFIX}.([-_\\w]+).${MACHINE_NAME}$`, 'i').exec(nameAnnotation);
            if (regExpExecArray !== null) {
              containerName = regExpExecArray[1];
            }
            return true;
          }
          return false;
        }
        let podName: string;
        [podName, containerName] = this.splitName(machine.name);
        return podItemName === podName;
      });

      const pod = getPodItemOrNull(podOrDeployment);
      if (pod && pod.kind === POD && pod.metadata.name && pod.spec && angular.isArray(pod.spec.containers)) {
        const containerIndex = pod.spec.containers.findIndex((container: IPodItemContainer) => {
          return container.name === containerName;
        });
        if (containerIndex !== -1 && pod.spec.containers && machine.recipe && machine.recipe.spec && machine.recipe.spec.containers) {
          pod.spec.containers[containerIndex] = machine.recipe.spec.containers[0];
        }
      }
    });

    try {
      newEnvironment.recipe.content = this.stringifyRecipe(recipe);
    } catch (e) {
      this.$log.error('Cannot retrieve environment\'s recipe, error: ', e);
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
      const nameAnnotation = `${NAME_ANNOTATION_PREFIX}.${machineRecipe.spec.containers[0].name}.${MACHINE_NAME}`;
      if (machineRecipe.metadata && machineRecipe.metadata.annotations && machineRecipe.metadata.annotations[nameAnnotation]) {
        return machineRecipe.metadata.annotations[nameAnnotation];
      }
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
    const recipe = this.parseRecipe(environment.recipe.content);
    if (!recipe) {
      this.$log.error('EnvironmentManager: cannot rename machine.');
      return environment;
    }
    let pod;
    let containerName: string;
    if (recipe && recipe.kind === LIST && angular.isArray(recipe.items)) {
      pod = getPodItemOrNull(recipe.items.find((item: ISupportedListItem) => {
        const podItem = getPodItemOrNull(item);
        if (!podItem || podItem.kind !== POD || !podItem.metadata.name || !podItem.spec || !angular.isArray(podItem.spec.containers)) {
          return false;
        }

        let containerIndex = podItem.spec.containers.findIndex((container: IPodItemContainer) => {
          const nameAnnotation = `${NAME_ANNOTATION_PREFIX}.${container.name}.${MACHINE_NAME}`;
          if (podItem.metadata && podItem.metadata.annotations && podItem.metadata.annotations[nameAnnotation]) {
            return podItem.metadata.annotations[nameAnnotation] === fullOldName;
          }
          return false;
        });
        if (containerIndex > -1) {
          containerName = podItem.spec.containers[containerIndex].name;
          return true;
        }
        const podName = podItem.metadata.name ? podItem.metadata.name : podItem.metadata.generateName;
        const [podItemName, containerItemName] = this.splitName(fullOldName);
        if (podName === podItemName) {
          containerName = containerItemName;
          return true;
        }
        return false;
      }));
    }
    if (!pod) {
      this.$log.error('EnvironmentManager: cannot rename machine.');
      return environment;
    }
    const containerIndex = pod.spec.containers.findIndex((container: IPodItemContainer) => {
      return container.name === containerName;
    });
    // rename machine in recipe
    if (containerIndex > -1) {
      const nameAnnotation = `${NAME_ANNOTATION_PREFIX}.${pod.spec.containers[containerIndex].name}.${MACHINE_NAME}`;
      if (pod.metadata && pod.metadata.annotations && pod.metadata.annotations[nameAnnotation]) {
        pod.metadata.annotations[nameAnnotation] = newMachineName;
      } else {
        pod.spec.containers[containerIndex].name = newMachineName;
      }
      const newEnvironment = angular.copy(environment);
      newEnvironment.recipe.content = this.stringifyRecipe(recipe);
      if (newEnvironment.machines[fullOldName]) {
        newEnvironment.machines[newName] = environment.machines[fullOldName];
        delete newEnvironment.machines[fullOldName];
      }

      return newEnvironment;
    }

    return environment;
  }

  /**
   * Gets unique name for new machine based on prefix.
   * @param {che.IWorkspaceEnvironment}environment
   * @returns {string}
   */
  getUniqueMachineName(environment: che.IWorkspaceEnvironment): string {
    const envRecipe = this.parseRecipe(environment.recipe.content);
    if (!envRecipe || !angular.isArray(envRecipe.items)) {
      return super.getUniqueMachineName(environment);
    }
    const pod = envRecipe.items.find((podItem: IPodItem) => {
      return podItem && podItem.kind === POD;
    });
    if (!pod  || !pod.metadata || !pod.metadata.name) {
      return super.getUniqueMachineName(environment);
    }

    return super.getUniqueMachineName(environment, pod.metadata.name);
  }

  /**
   * Create a new default machine.
   * @param {che.IWorkspaceEnvironment} environment
   * @param {string} image
   * @return {IEnvironmentManagerMachine}
   */
  createMachine(environment: che.IWorkspaceEnvironment, image?: string): IEnvironmentManagerMachine {
    const uniqueMachineName = this.getUniqueMachineName(environment);
    const [podName, containerName] = this.splitName(uniqueMachineName);
    const machineImage = !image ? 'rhche/centos_jdk8:latest' : image;

    return {
      name: uniqueMachineName,
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
    const machineRecipe: IPodItem = machine ? machine.recipe : null;
    const environmentRecipe = environment && environment.recipe ? environment.recipe.content : null;
    if (!environmentRecipe || !machineRecipe) {
      this.$log.error('EnvironmentManager: cannot add machine.');
      return environment;
    }
    const recipe = this.parseRecipe(environmentRecipe);
    if (!recipe) {
      this.$log.error('EnvironmentManager: cannot add machine.');
      return environment;
    }

    const nameAnnotation = `${NAME_ANNOTATION_PREFIX}.${machineRecipe.spec.containers[0].name}.${MACHINE_NAME}`;
    if (angular.isArray(recipe.items)) {
      const machineRecipePod = this.getMachinePod(machineRecipe);
      const usedPodIndex = recipe.items.findIndex((item: ISupportedListItem) => {
        const pod = getPodItemOrNull(item);
        if (!pod) {
          return false;
        }
        return machineRecipePod.metadata.name === this.getMachinePod(pod).metadata.name;
      });
      if (usedPodIndex === -1) {
        recipe.items.push(machineRecipe);
      } else {
        getPodItemOrNull(recipe.items[usedPodIndex]).spec.containers.push(machineRecipe.spec.containers[0]);
      }

      // update machine name
      if (usedPodIndex > -1 && machineRecipePod.metadata.annotations && machineRecipePod.metadata.annotations[nameAnnotation]) {
        const name = machineRecipePod.metadata.annotations[nameAnnotation];
        if (name) {
          recipe.items[usedPodIndex].metadata.annotations[nameAnnotation] = name;
          machine.name = name;
        }
      } else {
        const [, containerName] = this.splitName(machine.name);
        const podItemName = machineRecipe.metadata.name ? machineRecipe.metadata.name : machineRecipe.metadata.generateName;
        machine.name = `${podItemName}/${containerName}`;
      }

      // try to update recipe
      environment.recipe.content = this.stringifyRecipe(recipe);

      const copyMachine = angular.copy(machine);
      delete copyMachine.recipe;
      const containerName = copyMachine.name;
      delete copyMachine.name;

      environment.machines[containerName] = copyMachine;

      return environment;
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
    delete environment.machines[name];

    const envRecipe = this.parseRecipe(environment.recipe.content);
    if (!envRecipe) {
      this.$log.error('Cannot delete machine, error: ');
      return environment;
    }
    if (!envRecipe || !angular.isArray(envRecipe.items)) {
      return environment;
    }

    let podIndex: number;
    let containerName: string;
    if (envRecipe && envRecipe.kind === LIST && angular.isArray(envRecipe.items)) {
      podIndex = envRecipe.items.findIndex((item: ISupportedListItem) => {
        const podItem = getPodItemOrNull(item);
        if (!podItem || !podItem.metadata.name || !podItem.spec || !angular.isArray(podItem.spec.containers)) {
          return false;
        }
        const containerIndex = podItem.spec.containers.findIndex((container: IPodItemContainer) => {
          const nameAnnotation = `${NAME_ANNOTATION_PREFIX}.${container.name}.${MACHINE_NAME}`;
          if (podItem.metadata && podItem.metadata.annotations && podItem.metadata.annotations[nameAnnotation]) {
            return podItem.metadata.annotations[nameAnnotation] === name;
          }
          return false;
        });
        if (containerIndex > -1) {
          containerName = podItem.spec.containers[containerIndex].name;
          return true;
        }
        const podName = podItem.metadata.name ? podItem.metadata.name : podItem.metadata.generateName;
        const [podItemName, containerItemName] = this.splitName(name);
        if (podName === podItemName) {
          containerName = containerItemName;
          return true;
        }
        return false;
      });
    }
    if (podIndex > -1) {
      const podItem = getPodItemOrNull(envRecipe.items[podIndex]);
      if (podItem && podItem.kind === POD && podItem.metadata.name && podItem.spec && angular.isArray(podItem.spec.containers)) {
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
   * Gets empty pod from machine's recipe.
   * @param {IPodItem} machineRecipe
   * @returns {IPodItem}
   */
  getMachinePod(machineRecipe: IPodItem): IPodItem {
    if (!machineRecipe || !machineRecipe.metadata) {
      return machineRecipe;
    }
    const pod = angular.copy(machineRecipe);
    delete pod.spec;
    if (!angular.isArray(machineRecipe.metadata.annotations)) {
      return pod;
    }
    Object.keys(machineRecipe.metadata.annotations).forEach((annotation: string) => {
      if (annotation.startsWith(NAME_ANNOTATION_PREFIX)) {
        delete pod.metadata.annotations[annotation];
      }
    });
    return pod;
  }

  /**
   * Gets pod container from machine recipe.
   * @param {IEnvironmentManagerMachine} machine
   * @returns {IPodItemContainer}
   */
  private getPodContainer(machine: IEnvironmentManagerMachine): IPodItemContainer {
    if (!machine || !machine.name || !machine.recipe || !machine.recipe.metadata || !machine.recipe.spec) {
      return null;
    }
    const podName = machine.recipe.metadata.name ? machine.recipe.metadata.name : machine.recipe.metadata.generateName;
    const containerName = machine.recipe.spec.containers[0].name;
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
      if (podItemName === podName && machinePodItem.kind === POD && machinePodItem.spec && angular.isArray(machinePodItem.spec.containers)) {
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
