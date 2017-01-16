/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';
import {IEnvironmentManagerMachine, IEnvironmentManagerMachineServer} from './environment-manager-machine';

/**
 * This is base class, which describes the environment manager.
 * It's aim is to handle machines retrieval and editing, based on the type of environment.
 */

const WS_AGENT_NAME: string = 'org.eclipse.che.ws-agent';
const TERMINAL_AGENT_NAME: string = 'org.eclipse.che.terminal';
const SSH_AGENT_NAME: string = 'org.eclipse.che.ssh';

export class EnvironmentManager {
  $log: ng.ILogService;

  constructor($log: ng.ILogService) {
    this.$log = $log;
  }

  get editorMode(): string {
    return '';
  }

  canRenameMachine(machine: IEnvironmentManagerMachine): boolean {
    return false;
  }

  canDeleteMachine(machine: IEnvironmentManagerMachine): boolean {
    return false;
  }

  canEditEnvVariables(machine: IEnvironmentManagerMachine): boolean {
    return false;
  }

  /**
   * Retrieves the list of machines.
   *
   * @param {che.IWorkspaceEnvironment} environment
   * @param {any=} runtime runtime of active environment
   * @returns {IEnvironmentManagerMachine} list of machines defined in environment
   */
  getMachines(environment: che.IWorkspaceEnvironment, runtime?: any): IEnvironmentManagerMachine[] {
    if (!runtime) {
      return [];
    }

    let machines: IEnvironmentManagerMachine[] = [];
    runtime.machines.forEach((runtimeMachine: any) => {
      let name = runtimeMachine.config.name,
          machine: any = {name: name};
      if (runtimeMachine.runtime && runtimeMachine.runtime.servers) {
        machine.runtime = {
          servers: runtimeMachine.runtime.servers
        };
      }
      machines.push(machine);
    });

    return machines;
  }

  /**
   * Renames machine.
   *
   * @param {che.IWorkspaceEnvironment} environment
   * @param {string} oldName
   * @param {string} newName
   *
   * @return {che.IWorkspaceEnvironment}
   */
  renameMachine(environment: che.IWorkspaceEnvironment, oldName: string, newName: string): che.IWorkspaceEnvironment {
    this.$log.error('EnvironmentManager: cannot rename machine.');
    return environment;
  }

  /**
   * Removes machine.
   *
   * @param {che.IWorkspaceEnvironment} environment
   * @param {string} name name of machine
   *
   * @return {che.IWorkspaceEnvironment}
   */
  deleteMachine(environment: che.IWorkspaceEnvironment, name: string): che.IWorkspaceEnvironment {
    this.$log.error('EnvironmentManager: cannot delete machine.');
    return environment;
  }

  /**
   * Provides the environment configuration based on machines format.
   *
   * @param {che.IWorkspaceEnvironment} environment origin of the environment to be edited
   * @param {IEnvironmentManagerMachine} machines the list of machines
   * @returns {che.IWorkspaceEnvironment} environment's configuration
   */
  getEnvironment(environment: che.IWorkspaceEnvironment, machines: IEnvironmentManagerMachine[]): che.IWorkspaceEnvironment {
    let newEnvironment: che.IWorkspaceEnvironment = angular.copy(environment);

    machines.forEach((machine: IEnvironmentManagerMachine) => {
      let machineName = machine.name;

      if (angular.isUndefined(newEnvironment.machines)) {
        newEnvironment.machines = {};
      }
      if (angular.isUndefined(newEnvironment.machines[machineName])) {
        newEnvironment.machines[machineName] = {attributes: {}};
      }
      newEnvironment.machines[machineName].attributes.memoryLimitBytes = machine.attributes.memoryLimitBytes;
      newEnvironment.machines[machineName].agents = angular.copy(machine.agents);
      newEnvironment.machines[machineName].servers = angular.copy(machine.servers);
    });

    return newEnvironment;
  }

  /**
   * Returns whether machine is developer or not.
   *
   * @param {IEnvironmentManagerMachine} machine
   * @returns {boolean}
   */
  isDev(machine: IEnvironmentManagerMachine): boolean {
    return machine.agents && machine.agents.indexOf(WS_AGENT_NAME) >= 0;
  }

  /**
   * Set machine as developer one - contains 'ws-agent' agent.
   *
   * @param {IEnvironmentManagerMachine} machine machine to edit
   * @param {boolean} isDev defined whether machine is developer or not
   */
  setDev(machine: IEnvironmentManagerMachine, isDev: boolean): void {
    let hasWsAgent = this.isDev(machine);
    if (isDev) {
      machine.agents = machine.agents ? machine.agents : [];
      if (!hasWsAgent) {
        machine.agents.push(WS_AGENT_NAME);
      }
      if (machine.agents.indexOf(SSH_AGENT_NAME) < 0) {
        machine.agents.push(SSH_AGENT_NAME);
      }
      if (machine.agents.indexOf(TERMINAL_AGENT_NAME) < 0) {
        machine.agents.push(TERMINAL_AGENT_NAME);
      }
      return;
    }

    if (!isDev && hasWsAgent) {
      machine.agents.splice(machine.agents.indexOf(WS_AGENT_NAME), 1);
    }
  }

  getServers(machine: IEnvironmentManagerMachine): {[serverName: string]: IEnvironmentManagerMachineServer} {
    if (!machine.servers) {
      return {};
    }

    Object.keys(machine.servers).forEach((serverName: string) => {
      machine.servers[serverName].userScope = true;
    });

    if (!machine.runtime) {
      return machine.servers;
    }

    Object.keys(machine.runtime.servers).forEach((runtimeServerName: string) => {
      let runtimeServer: che.IWorkspaceRuntimeMachineServer = machine.runtime.servers[runtimeServerName],
          runtimeServerReference = runtimeServer.ref;

      if (machine.servers[runtimeServerReference]) {
        machine.servers[runtimeServerReference].runtime = runtimeServer;
      } else {
        let port;
        if (runtimeServer.port) {
          port = runtimeServer.port;
        } else {
          [port, ] = runtimeServerName.split('/');
        }
        machine.servers[runtimeServerReference] = {
          userScope: false,
          port: port,
          protocol: runtimeServer.protocol,
          runtime: runtimeServer
        };
      }
    });

    return machine.servers;
  }

  setServers(machine: IEnvironmentManagerMachine, _servers: {[serverRef: string]: IEnvironmentManagerMachineServer}): void {
    let servers = angular.copy(_servers);

    Object.keys(_servers).forEach((serverName: string) => {
      // remove system defined servers
      if (!_servers[serverName].userScope) {
        delete servers[serverName];
        return;
      }

      // remove unnecessary keys from user defined servers
      let server = servers[serverName];
      delete server.userScope;
      delete server.runtime;
    });

    machine.servers = servers;
  }

  getAgents(machine: IEnvironmentManagerMachine): string[] {
    return machine.agents || [];
  }

  setAgents(machine: IEnvironmentManagerMachine, agents: string[]): void {
    machine.agents = angular.copy(agents);
  }

  /**
   * Returns memory limit from machine's attributes
   *
   * @param {IEnvironmentManagerMachine} machine
   * @returns {number|string} memory limit in bytes
   */
  getMemoryLimit(machine: IEnvironmentManagerMachine): number|string {
    if (machine && machine.attributes && machine.attributes.memoryLimitBytes) {
      return machine.attributes.memoryLimitBytes;
    }

    return -1;
  }

  /**
   * Sets the memory limit of the pointed machine.
   * Value in attributes has the highest priority,
   *
   * @param {IEnvironmentManagerMachine} machine machine to change memory limit
   * @param {number} limit memory limit
   */
  setMemoryLimit(machine: IEnvironmentManagerMachine, limit: number): void {
    machine.attributes = machine.attributes ? machine.attributes : {};
    machine.attributes.memoryLimitBytes = limit;
  }

  getEnvVariables(machine: IEnvironmentManagerMachine): any {
    return null;
  }
}
