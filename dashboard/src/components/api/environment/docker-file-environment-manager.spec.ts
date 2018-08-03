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

import {DockerFileEnvironmentManager} from './docker-file-environment-manager';
import {IEnvironmentManagerMachine} from './environment-manager-machine';

/**
 * Test the environment manager for docker file based recipes
 * @author Oleksii Kurinnyi
 */

describe('If recipe has content', () => {
  let envManager: DockerFileEnvironmentManager, environment: che.IWorkspaceEnvironment, machines: IEnvironmentManagerMachine[];

  beforeEach(inject(($log: ng.ILogService) => {
    envManager = new DockerFileEnvironmentManager($log);

    environment = {
      'machines': {
        'dev-machine': {
          'attributes': {'memoryLimitBytes': '2147483648'},
          'servers': {},
          'volumes': {},
          'installers': ['org.eclipse.che.ws-agent', 'org.eclipse.che.terminal', 'org.eclipse.che.ssh']
        }
      },
      'recipe': {
        'type': 'dockerfile',
        'content': 'FROM codenvy/ubuntu_jdk8\nENV myName="John Doe" myDog=Rex\\ The\\ Dog \\\n    myCat=fluffy',
        'contentType': 'text/x-dockerfile'
      }
    };

    machines = envManager.getMachines(environment);
  }));

  describe('DockerFileEnvironmentManager', () => {

    it('can edit environment variables', () => {
      let canEditEnvVariables = envManager.canEditEnvVariables(machines[0]);

      expect(canEditEnvVariables).toBe(true);
    });

    it('should return servers', () => {
      let servers = envManager.getServers(machines[0]);

      let expectedServers = environment.machines['dev-machine'].servers;
      expect(servers).toEqual(expectedServers);
    });

    it('should return memory limit', () => {
      let memoryLimit = envManager.getMemoryLimit(machines[0]);

      let expectedMemoryLimit = environment.machines['dev-machine'].attributes.memoryLimitBytes;
      expect(memoryLimit.toString()).toEqual(expectedMemoryLimit);
    });

    it('should return source', () => {
      let source = envManager.getSource(machines[0]);

      let expectedSource = {image: 'codenvy/ubuntu_jdk8'};
      expect(source).toEqual(expectedSource);
    });

    it('the machine should be a dev machine', () => {
      let isDev = envManager.isDev(machines[0]);

      expect(isDev).toBe(true);
    });

    it('should update environment\'s recipe via machine\'s source', () => {
      let oldMachines = envManager.getMachines(environment),
          oldSource = envManager.getSource(oldMachines[0]),
          source = 'eclipse/node';

      envManager.setSource(oldMachines[0], source);
      let newEnvironment = envManager.getEnvironment(environment, oldMachines),
          newMachines = envManager.getMachines(newEnvironment),
          newSource = envManager.getSource(newMachines[0]);

      expect(newSource.image).toEqual(source);

      expect(newSource.image).not.toEqual(oldSource);
    });

  });

});

describe('If recipe has location', () => {
  let envManager, environment, machines;

  beforeEach(inject(($log: ng.ILogService) => {
    envManager = new DockerFileEnvironmentManager($log);

    environment = {
      'machines': {
        'dev-machine': {
          'servers': {},
          'volumes': {},
          'installers': ['org.eclipse.che.ws-agent', 'org.eclipse.che.terminal', 'org.eclipse.che.ssh'],
          'attributes': {'memoryLimitBytes': '2147483648'}
        }
      },
      'recipe': {
        'contentType': 'text/x-dockerfile',
        'location': 'https://gist.githubusercontent.com/garagatyi/14c3d1587a4c5b630d789f85340426c7/raw/8db09677766b82ec8b034698a046f8fdf53ebcb1/script',
        'type': 'dockerfile'
      }
    };

    machines = envManager.getMachines(environment);
  }));

  describe('DockerFileEnvironmentManager', () => {

    it('can edit environment variables always true', () => {
      let canEditEnvVariables = envManager.canEditEnvVariables(machines[0]);

      expect(canEditEnvVariables).toBe(true);
    });

    it('should return servers', () => {
      let servers = envManager.getServers(machines[0]);

      let expectedServers = environment.machines['dev-machine'].servers;
      expect(servers).toEqual(expectedServers);
    });

    it('should return memory limit', () => {
      let memoryLimit = envManager.getMemoryLimit(machines[0]);

      let expectedMemoryLimit = environment.machines['dev-machine'].attributes.memoryLimitBytes;
      expect(memoryLimit.toString()).toEqual(expectedMemoryLimit);
    });

    it('cannot return the source', () => {
      let source = envManager.getSource(machines[0]);

      expect(source).toEqual(null);
    });

    it('the machine should be a dev machine', () => {
      let isDev = envManager.isDev(machines[0]);

      expect(isDev).toBe(true);
    });

  });

});

