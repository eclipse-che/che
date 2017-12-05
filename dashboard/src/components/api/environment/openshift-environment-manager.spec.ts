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

import {OpenshiftEnvironmentManager} from './openshift-environment-manager';
import {IEnvironmentManagerMachine, IEnvironmentManagerMachineServer} from './environment-manager-machine';
import {IPodList} from './openshift-environment-recipe-parser';
import {IPodItem, IPodItemContainer} from './openshift-machine-recipe-parser';

/**
 * Test the environment manager for openshift based recipes
 * @author Oleksii Orel
 */

describe('OpenshiftEnvironmentManager', () => {
  const podName = 'pod1';
  const machineImage = 'machineimage1';
  const containerName = 'container1';
  let envManager: OpenshiftEnvironmentManager;
  let environment: che.IWorkspaceEnvironment;
  let machines: IEnvironmentManagerMachine[];

  beforeEach(inject(($log: ng.ILogService) => {
    envManager = new OpenshiftEnvironmentManager($log);

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
        'type': 'openshift',
        'content': `kind: List\nitems:\n-\n  apiVersion: v1\n  kind: Pod\n  metadata:\n    name: ${podName}\n  spec:\n    containers:\n      -\n        image: ${machineImage}\n        name: ${containerName}`
      }
    };

    machines = envManager.getMachines(environment);
  }));

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
      const recipe: IPodList = jsyaml.load(environment.recipe.content);
      const machinePodItem = recipe.items.find((machinePodItem: IPodItem) => {
        const podItemName = machinePodItem.metadata.name ? machinePodItem.metadata.name : machinePodItem.metadata.generateName;
        return podItemName === podName;
      });
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

