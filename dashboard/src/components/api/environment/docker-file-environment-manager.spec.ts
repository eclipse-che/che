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

import {DockerFileEnvironmentManager} from './docker-file-environment-manager';

/**
 * Test the environment manager for docker file based recipes
 * @author Oleksii Kurinnyi
 */

describe('If recipe has content', () => {
  let envManager, environment, machines;

  beforeEach(() => {
    envManager = new DockerFileEnvironmentManager();

    environment = {
      "machines": {
        "dev-machine": {
          "attributes": {"memoryLimitBytes": "2147483648"},
          "servers": {},
          "agents": ["org.eclipse.che.ws-agent", "org.eclipse.che.terminal", "org.eclipse.che.ssh"]
        }
      },
      "recipe": {
        "type": "dockerfile",
        "content": "FROM codenvy/ubuntu_jdk8\nENV myName=\"John Doe\" myDog=Rex\\ The\\ Dog \\\n    myCat=fluffy",
        "contentType": "text/x-dockerfile"
      }
    };

    machines = envManager.getMachines(environment);
  });

  describe('DockerFileEnvironmentManager', () => {

    it('cannot rename machine', () => {
      let canRenameMachine = envManager.canRenameMachine(machines[0]);

      expect(canRenameMachine).toBe(false);
    });

    it('cannot delete machine', () => {
      let canDeleteMachine = envManager.canDeleteMachine(machines[0]);

      expect(canDeleteMachine).toBe(false);
    });

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
      expect(memoryLimit).toEqual(expectedMemoryLimit);
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

  });

});

describe('If recipe has location', () => {
  let envManager, environment, machines;

  beforeEach(() => {
    envManager = new DockerFileEnvironmentManager();

    environment = {"machines":{"dev-machine":{"servers":{},"agents":["org.eclipse.che.ws-agent","org.eclipse.che.terminal","org.eclipse.che.ssh"],"attributes":{"memoryLimitBytes":"2147483648"}}},"recipe":{"contentType":"text/x-dockerfile","location":"https://gist.githubusercontent.com/garagatyi/14c3d1587a4c5b630d789f85340426c7/raw/8db09677766b82ec8b034698a046f8fdf53ebcb1/script","type":"dockerfile"}};

    machines = envManager.getMachines(environment);
  });

  describe('DockerFileEnvironmentManager', () => {

    it('cannot rename machine', () => {
      let canRenameMachine = envManager.canRenameMachine(machines[0]);

      expect(canRenameMachine).toBe(false);
    });

    it('cannot delete machine', () => {
      let canDeleteMachine = envManager.canDeleteMachine(machines[0]);

      expect(canDeleteMachine).toBe(false);
    });

    it('cannot edit environment variables', () => {
      let canEditEnvVariables = envManager.canEditEnvVariables(machines[0]);

      expect(canEditEnvVariables).toBe(false);
    });

    it('should return servers', () => {
      let servers = envManager.getServers(machines[0]);

      let expectedServers = environment.machines['dev-machine'].servers;
      expect(servers).toEqual(expectedServers);
    });

    it('should return memory limit', () => {
      let memoryLimit = envManager.getMemoryLimit(machines[0]);

      let expectedMemoryLimit = environment.machines['dev-machine'].attributes.memoryLimitBytes;
      expect(memoryLimit).toEqual(expectedMemoryLimit);
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

