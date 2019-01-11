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

import {KubernetesEnvironmentManager} from './kubernetes-environment-manager';
import {IEnvironmentManagerMachine, IEnvironmentManagerMachineServer} from './environment-manager-machine';
import {ISupportedItemList} from './kubernetes-environment-recipe-parser';
import {IPodItem, IPodItemContainer, getPodItemOrNull, ISupportedListItem} from './kubernetes-machine-recipe-parser';
import {CheRecipeTypes} from '../recipe/che-recipe-types';

/**
 * Test the environment manager for kubernetes based recipes
 * @author Oleksii Orel
 */

describe('KubernetesEnvironmentManager', () => {
  const podName = 'pod1';
  const podAnnotatedContainerName = 'podannotatedcontainer';
  const machineImage = 'machineimage1';
  const containerName = 'container1';
  let envManager: KubernetesEnvironmentManager;
  let environment: che.IWorkspaceEnvironment;
  let machines: IEnvironmentManagerMachine[];

  beforeEach(inject(($log: ng.ILogService) => {
    envManager = new KubernetesEnvironmentManager($log);
  }));

  it(`should return 'kubernetes' recipe type`, () => {
    expect(envManager.type).toEqual(CheRecipeTypes.KUBERNETES);
  });

  describe('without matching of a machine by pod annotation', () => {
    beforeEach(() => {
      environment = {
        'machines': {
          [`${podName}/${containerName}`]: {
            'servers': {
              '10240/tcp': {
                'properties': {},
                'protocol': 'http',
                'port': '10240',
                'path': ''
              }
            },
            'volumes': {
              'volume1': {
                'path': '/123'
              }
            }, 'installers': ['org.eclipse.che.ws-agent'], 'attributes': {'memoryLimitBytes': '16642998272'}
          }
        }, 'recipe': {
          'contentType': 'application/x-yaml',
          'type': 'kubernetes',
          'content': `kind: List\nitems:\n-\n  apiVersion: v1\n  kind: Pod\n  metadata:\n   name: ${podName}\n  spec:\n    containers:\n      -\n        image: ${machineImage}\n        name: ${containerName}`
        }
      };

      machines = envManager.getMachines(environment);
    });

    it(`should return source`, () => {
      const source = envManager.getSource(machines[0]);

      expect(source).toEqual(machineImage);
    });

    it(`should return servers`, () => {
      let servers = envManager.getServers(machines[0]);

      let expectedServers = environment.machines[`${podName}/${containerName}`].servers;
      Object.keys(expectedServers).forEach((serverRef: string) => {
        (expectedServers[serverRef] as IEnvironmentManagerMachineServer).userScope = true;
      });

      expect(servers).toEqual(expectedServers);
    });

    it(`should return memory limit`, () => {
      let memoryLimit = envManager.getMemoryLimit(machines[0]);

      let expectedMemoryLimit = environment.machines[`${podName}/${containerName}`].attributes.memoryLimitBytes;
      expect(memoryLimit.toString()).toEqual(expectedMemoryLimit.toString());
    });

    it(`the machine should be a dev machine`, () => {
      let isDev = envManager.isDev(machines[0]);

      expect(isDev).toBe(true);
    });

    it(`should update environment's recipe via machine's source`, () => {
      const machines = envManager.getMachines(environment);
      const newSource = 'eclipse/node';

      let getEnvironmentSource = (environment: che.IWorkspaceEnvironment, machine: IEnvironmentManagerMachine): string => {
        const [podName, containerName] = machine.name.split(/\//);
        const recipe: ISupportedItemList = jsyaml.load(environment.recipe.content);
        const machinePodItem = getPodItemOrNull(recipe.items.find((item: ISupportedListItem) => {
          const machinePodItem = getPodItemOrNull(item);
          if (!machinePodItem) {
            return false;
          }
          const podItemName = machinePodItem.metadata.name ? machinePodItem.metadata.name : machinePodItem.metadata.generateName;
          return podItemName === podName;
        }));
        const podContainer = machinePodItem.spec.containers.find((podContainer: IPodItemContainer) => {
          return podContainer.name === containerName;
        });

        return podContainer.image;
      };

      envManager.setSource(machines[0], newSource);
      let newEnvironment = envManager.getEnvironment(environment, machines);
      expect(getEnvironmentSource(newEnvironment, machines[0])).toEqual(newSource);
    });
  });
  describe('with matching of a machine by pod annotation', () => {
    beforeEach(() => {
      environment = {
        'machines': {
          [`${podAnnotatedContainerName}`]: {
            'servers': {
              '10240/tcp': {
                'properties': {},
                'protocol': 'http',
                'port': '10240',
                'path': ''
              }
            },
            'volumes': {
              'volume1': {
                'path': '/123'
              }
            }, 'installers': ['org.eclipse.che.ws-agent'], 'attributes': {'memoryLimitBytes': '16642998272'}
          }
        }, 'recipe': {
          'contentType': 'application/x-yaml',
          'type': 'kubernetes',
          'content': `kind: List\nitems:\n-\n  apiVersion: v1\n  kind: Pod\n  metadata:\n   name: ${podName}\n   annotations:\n    org.eclipse.che.container.${containerName}.machine_name: ${podAnnotatedContainerName}\n  spec:\n    containers:\n      -\n        image: ${machineImage}\n        name: ${containerName}`
        }
      };

      machines = envManager.getMachines(environment);
    });

    it(`should return source`, () => {
      const source = envManager.getSource(angular.copy(machines[0]));

      expect(source).toEqual(machineImage);
    });

    it(`should return servers`, () => {
      let servers = envManager.getServers(machines[0]);

      let expectedServers = environment.machines[`${podAnnotatedContainerName}`].servers;
      Object.keys(expectedServers).forEach((serverRef: string) => {
        (expectedServers[serverRef] as IEnvironmentManagerMachineServer).userScope = true;
      });

      expect(servers).toEqual(expectedServers);
    });

    it(`should return memory limit`, () => {
      let memoryLimit = envManager.getMemoryLimit(machines[0]);

      let expectedMemoryLimit = environment.machines[`${podAnnotatedContainerName}`].attributes.memoryLimitBytes;
      expect(memoryLimit.toString()).toEqual(expectedMemoryLimit.toString());
    });

    it(`the machine should be a dev machine`, () => {
      let isDev = envManager.isDev(machines[0]);

      expect(isDev).toBe(true);
    });

    it(`should update environment's recipe via machine's source`, () => {
      const machines = envManager.getMachines(environment);
      const newSource = 'eclipse/node';

      let getEnvironmentSource = (environment: che.IWorkspaceEnvironment, machine: IEnvironmentManagerMachine): string => {
        const podName = machine.recipe.metadata.name;
        const containerName = machine.recipe.spec.containers[0].name;
        const recipe: ISupportedItemList = jsyaml.load(environment.recipe.content);
        const machinePodItem = getPodItemOrNull(recipe.items.find((item: ISupportedListItem) => {
          const machinePodItem = getPodItemOrNull(item);
          if (!machinePodItem) {
            return false;
          }
          const podItemName = machinePodItem.metadata.name ? machinePodItem.metadata.name : machinePodItem.metadata.generateName;
          return podItemName === podName;
        }));

        const podContainer = machinePodItem.spec.containers.find((podContainer: IPodItemContainer) => {
          return podContainer.name === containerName;
        });

        return podContainer.image;
      };

      envManager.setSource(machines[0], newSource);
      let newEnvironment = envManager.getEnvironment(environment, machines);
      expect(getEnvironmentSource(newEnvironment, machines[0])).toEqual(newSource);
    });
  });
});

