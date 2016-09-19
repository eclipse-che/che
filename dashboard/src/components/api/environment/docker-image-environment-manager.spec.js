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

import {DockerImageEnvironmentManager} from './docker-image-environment-manager';

/**
 * Test the environment manager for docker image based recipes
 * @author Oleksii Kurinnyi
 */

describe('DockerImageEnvironmentManager', () => {
  let envManager, environment, machines;

  beforeEach(() => {
    envManager = new DockerImageEnvironmentManager();

    environment = {'machines':{'dev-machine':{'servers':{'10240/tcp':{'properties':{},'protocol':'http','port':'10240'}},'agents':['ws-agent','org.eclipse.che.ws-agent'],'attributes':{'memoryLimitBytes':'16642998272'}}},'recipe':{'location':'codenvy/ubuntu_jdk8','type':'dockerimage'}};

    machines = envManager.getMachines(environment);
  });

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

  it('should return source', () => {
    let source = envManager.getSource(machines[0]);

    let expectedSource = {image: environment.recipe.location};
    expect(source).toEqual(expectedSource);
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

  it('the machine should be a dev machine', () => {
    let isDev = envManager.isDev(machines[0]);

    expect(isDev).toBe(true);
  })
});

