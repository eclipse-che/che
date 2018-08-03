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

import {ComposeEnvironmentManager} from './compose-environment-manager';
import {IEnvironmentManagerMachine, IEnvironmentManagerMachineServer} from './environment-manager-machine';

/**
 * Test the environment manager for compose based recipes
 * @author Oleksii Kurinnyi
 */

describe('ComposeEnvironmentManager', () => {
  let envManager: ComposeEnvironmentManager;

  describe('regardless of recipe location or content', () => {
    let environment: che.IWorkspaceEnvironment, machines: IEnvironmentManagerMachine[];

    beforeEach(inject(($log: ng.ILogService) => {
      envManager = new ComposeEnvironmentManager($log);

      environment = {
        'machines': {
          'another-machine': {
            'attributes': {'memoryLimitBytes': '2147483648'},
            'servers': {},
            'volumes': {},
            'installers': []
          },
          'db': {
            'attributes': {},
            'servers': {},
            'volumes': {},
            'installers': []
          },
          'dev-machine': {
            'attributes': {'memoryLimitBytes': '5368709120'},
            'servers': {
              '1024/tcp': {'port': '1024', 'properties': {}, 'protocol': 'http', 'path': ''},
              '1025/tcp': {'port': '1025', 'properties': {}, 'protocol': 'http', 'path': ''}
            },
            'volumes': {
              'volume1': {'path': '/some/path'}
            },
            'installers': ['org.eclipse.che.ws-agent', 'org.eclipse.che.terminal', 'org.eclipse.che.ssh']
          }
        },
        'recipe': {
          'type': 'compose',
          'content': 'services:\n  dev-machine:\n    image: codenvy/ubuntu_jdk8\n    mem_limit: 2147483648\n    depends_on:\n      - another-machine\n    environment:\n      myName: John Doe\n      myDog: Rex The Dog\n      myCat: fluffy\n  another-machine:\n    build:\n      context: \'https://github.com/eclipse/che\'\n      dockerfile: dockerfiles/che-dev/Dockerfile\n    command:\n      - tail\n      - \'-f\'\n      - /dev/null\n    entrypoint:\n      - /bin/bash\n      - \'-c\'\n    environment:\n      SOME_ENV: development\n      SHOW: \'true\'\n      SESSION_SECRET: null\n    expose:\n      - \'3000\'\n      - \'8000\'\n    labels:\n      com.example.description: Accounting webapp\n      com.example.department: Finance\n      com.example.label-with-empty-value: \'\'\n    links:\n      - \'db:database\'\n    mem_limit: 2147483648\n  db:\n    image: redis\n',
          'contentType': 'application/x-yaml'
        }
      };

      machines = envManager.getMachines(environment);
    }));

    it('should return servers', () => {
      let machineName = 'dev-machine',
          machine     = machines.find((machine: IEnvironmentManagerMachine) => {
            return machine.name === machineName;
          });
      let servers = envManager.getServers(machine);

      let expectedServers = <{[serverRef: string]: IEnvironmentManagerMachineServer}> environment.machines[machineName].servers;
      Object.keys(expectedServers).forEach((serverRef: string) => {
        expectedServers[serverRef].userScope = true;
      });

      expect(servers).toEqual(expectedServers);
    });

    it('at least one machine should contain \'ws-agent\'', () => {
      let devMachinesList = machines.filter((machine: IEnvironmentManagerMachine) => {
        return envManager.isDev(machine);
      });

      expect(devMachinesList.length).toBeGreaterThan(0);
    });

  });

  describe('for recipe from content', () => {
    let environment: che.IWorkspaceEnvironment, machines: IEnvironmentManagerMachine[], testMachine: IEnvironmentManagerMachine;

    beforeEach(inject(($log: ng.ILogService) => {
      envManager = new ComposeEnvironmentManager($log);

      environment = {
        'machines': {
          'another-machine': {
            'attributes': {'memoryLimitBytes': '2147483648'},
            'servers': {},
            'volumes': {},
            'installers': []
          },
          'db': {'attributes': {}, 'servers': {}, 'volumes': {}, 'installers': []},
          'dev-machine': {
            'attributes': {'memoryLimitBytes': '5368709120'},
            'servers': {
              '1024/tcp': {'port': '1024', 'properties': {}, 'protocol': 'http', 'path': ''},
              '1025/tcp': {'port': '1025', 'properties': {}, 'protocol': 'http', 'path': ''}
            },
            'volumes': {
              'vol1': {'path': '/some/path'},
              'm22': {'path': '/home/user/.m2/repository'}
            },
            'installers': ['org.eclipse.che.ws-agent', 'org.eclipse.che.terminal', 'org.eclipse.che.ssh']
          }
        },
        'recipe': {
          'type': 'compose',
          'content': 'services:\n  dev-machine:\n    image: codenvy/ubuntu_jdk8\n    mem_limit: 2147483648\n    depends_on:\n      - another-machine\n    environment:\n      myName: John Doe\n      myDog: Rex The Dog\n      myCat: fluffy\n  another-machine:\n    build:\n      context: \'https://github.com/eclipse/che\'\n      dockerfile: dockerfiles/che-dev/Dockerfile\n    command:\n      - tail\n      - \'-f\'\n      - /dev/null\n    entrypoint:\n      - /bin/bash\n      - \'-c\'\n    environment:\n      SOME_ENV: development\n      SHOW: \'true\'\n      SESSION_SECRET: null\n    expose:\n      - \'3000\'\n      - \'8000\'\n    labels:\n      com.example.description: Accounting webapp\n      com.example.department: Finance\n      com.example.label-with-empty-value: \'\'\n    links:\n      - \'db:database\'\n    mem_limit: 2147483648\n  db:\n    image: redis\n',
          'contentType': 'application/x-yaml'
        }
      };

      machines = envManager.getMachines(environment);

      let testMachineName = 'dev-machine';
      testMachine = machines.find((machine: IEnvironmentManagerMachine) => {
        return machine.name === testMachineName;
      });
    }));

    it('should be allowed edit environment variables', () => {
      let canEditEnvVariables = envManager.canEditEnvVariables(testMachine);

      expect(canEditEnvVariables).toBe(true);
    });

    it('should return source', () => {
      let source = envManager.getSource(testMachine);

      let expectedSource = {image: 'codenvy/ubuntu_jdk8'};
      expect(source).toEqual(expectedSource);
    });

    it('from machine\'s attributes', () => {
      let memoryLimit = envManager.getMemoryLimit(testMachine);

      // machine's attributes are more preferable than recipe to get memory limit
      let expectedMemoryLimit = environment.machines[testMachine.name].attributes.memoryLimitBytes;
      expect(memoryLimit.toString()).toEqual(expectedMemoryLimit);
    });

    it('from recipe', () => {
      let someMachineName = 'dev-machine';
      delete environment.machines[someMachineName].attributes.memoryLimitBytes;

      let machines    = envManager.getMachines(environment),
          someMachine = machines.find((machine: IEnvironmentManagerMachine) => {
            return machine.name === someMachineName;
          });

      let memoryLimit = envManager.getMemoryLimit(someMachine);

      // if there is no 'memoryLimitBytes' in attributes then mem_limit is returned
      delete environment.machines[someMachineName].attributes.memoryLimitBytes;
      expect(memoryLimit).toEqual(2147483648);
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

  describe('for recipe from location', () => {
    let environment: che.IWorkspaceEnvironment, machines: IEnvironmentManagerMachine[];

    beforeEach(inject(($log: ng.ILogService) => {
      envManager = new ComposeEnvironmentManager($log);

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

    it('shouldn\'t return any source', () => {
      let source = envManager.getSource(machines[0]);

      expect(source).toEqual(null);
    });

    it('should return memory limit from machine\'s attributes', () => {
      let memoryLimit = envManager.getMemoryLimit(machines[0]);

      let expectedMemoryLimit = environment.machines['dev-machine'].attributes.memoryLimitBytes;
      expect(memoryLimit.toString()).toEqual(expectedMemoryLimit);
    });

  });

});

