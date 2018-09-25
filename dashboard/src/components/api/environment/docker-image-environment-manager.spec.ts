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

import {DockerImageEnvironmentManager} from './docker-image-environment-manager';
import {IEnvironmentManagerMachine, IEnvironmentManagerMachineServer} from './environment-manager-machine';

/**
 * Test the environment manager for docker image based recipes
 * @author Oleksii Kurinnyi
 */

describe('DockerImageEnvironmentManager', () => {
  let envManager: DockerImageEnvironmentManager, environment: che.IWorkspaceEnvironment, machines: IEnvironmentManagerMachine[];

  beforeEach(inject(($log: ng.ILogService) => {
    envManager = new DockerImageEnvironmentManager($log);

    environment = {
      'machines': {
        'dev-machine': {
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
          }, 'installers': ['ws-agent', 'org.eclipse.che.ws-agent'], 'attributes': {'memoryLimitBytes': '16642998272'}
        }
      }, 'recipe': {'content': 'codenvy/ubuntu_jdk8', 'type': 'dockerimage'}
    };

    machines = envManager.getMachines(environment);
  }));

  it('should return source', () => {
    let source = envManager.getSource(machines[0]);

    let expectedSource = {image: environment.recipe.content};
    expect(source).toEqual(expectedSource);
  });

  it('should return servers', () => {
    let servers = envManager.getServers(machines[0]);

    let expectedServers = environment.machines['dev-machine'].servers;
    Object.keys(expectedServers).forEach((serverRef: string) => {
      (expectedServers[serverRef] as IEnvironmentManagerMachineServer).userScope = true;
    });

    expect(servers).toEqual(expectedServers);
  });

  it('should return memory limit', () => {
    let memoryLimit = envManager.getMemoryLimit(machines[0]);

    let expectedMemoryLimit = environment.machines['dev-machine'].attributes.memoryLimitBytes;
    expect(memoryLimit.toString()).toEqual(expectedMemoryLimit.toString());
  });

  it('the machine should be a dev machine', () => {
    let isDev = envManager.isDev(machines[0]);

    expect(isDev).toBe(true);
  });

  it('should update environment\'s recipe via machine\'s source', () => {
    let machines = envManager.getMachines(environment),
        newSource = 'eclipse/node';

    envManager.setSource(machines[0], newSource);
    let newEnvironment = envManager.getEnvironment(environment, machines);
    expect(newEnvironment.recipe.content).toEqual(newSource);
  });

});

