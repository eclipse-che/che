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
import {IEnvironmentManagerMachine, IEnvironmentManagerMachineServer} from './environment-manager-machine';

/**
 * This is base class, which describes the environment manager.
 * It's aim is to handle machines retrieval and editing, based on the type of environment.
 */

const WS_AGENT_NAME: string = 'org.eclipse.che.ws-agent';
const TERMINAL_AGENT_NAME: string = 'org.eclipse.che.terminal';
const SSH_AGENT_NAME: string = 'org.eclipse.che.ssh';
const DEFAULT_MEMORY_LIMIT: number = 2 * 1073741824;

export abstract class EnvironmentManager {
  $log: ng.ILogService;

  constructor($log: ng.ILogService) {
    this.$log = $log;
  }

  get SSH_AGENT_NAME(): string {
    return SSH_AGENT_NAME;
  }

  get TERMINAL_AGENT_NAME(): string {
    return TERMINAL_AGENT_NAME;
  }

  get DEFAULT_MEMORY_LIMIT(): number {
    return DEFAULT_MEMORY_LIMIT;
  }

  get type(): string {
    return '';
  }

  get editorMode(): string {
    return '';
  }

  parseMachineRecipe(content: string): any {
    return this.parseRecipe(content);
  };

  abstract parseRecipe(content: string): any;

  abstract stringifyRecipe(recipe: any): string;

  abstract getSource(machine: IEnvironmentManagerMachine): {[sourceType: string]: string};

  abstract setSource(machine: IEnvironmentManagerMachine, image: string): void;

  abstract createNewDefaultMachine(environment: che.IWorkspaceEnvironment, image?: string): IEnvironmentManagerMachine;

  abstract addMachine(environment: che.IWorkspaceEnvironment, machine: IEnvironmentManagerMachine): che.IWorkspaceEnvironment


  /**
   * Returns true if environment recipe content is present.
   *
   * @param {IEnvironmentManagerMachine} machine
   * @returns {boolean}
   */
  canEditEnvVariables(machine: IEnvironmentManagerMachine): boolean {
    return angular.isDefined(machine);
  }

  /**
   * Returns object with environment variables.
   *
   * @param {IEnvironmentManagerMachine} machine
   * @returns {any}
   */
  getEnvVariables(machine: IEnvironmentManagerMachine): any {
    return machine && machine.env ? machine.env : {};
  }

  /**
   * Sets env variables.
   * @param {IEnvironmentManagerMachine} machine
   * @param {any} envVariables
   */
  setEnvVariables(machine: IEnvironmentManagerMachine, envVariables: any): void {
    if (!machine || !envVariables) {
      return;
    }
    machine.env = envVariables;
  };

  /**
   * Returns object with volumes.
   *
   * @param {IEnvironmentManagerMachine} machine
   * @returns {any}
   */
  getMachineVolumes(machine: IEnvironmentManagerMachine): any {
    return machine && machine.volumes ? machine.volumes : {};
  }

  /**
   * Sets volumes.
   * @param {IEnvironmentManagerMachine} machine
   * @param {any} volumes
   */
  setMachineVolumes(machine: IEnvironmentManagerMachine, volumes: any): any {
    if (!machine || !volumes) {
      return;
    }
    machine.volumes = volumes;
  }

  /**
   * Gets unique name for new machine based on prefix.
   *
   * @param environment
   * @param namePrefix
   * @returns {string}
   */
  getUniqueMachineName(environment: che.IWorkspaceEnvironment, namePrefix?: string): string {
    let newMachineName =  namePrefix ? namePrefix : 'new-machine';
    const usedMachinesNames: Array<string> = environment && environment.machines ? Object.keys(environment.machines) : [];
    for (let pos: number = 1; pos < 1000; pos++) {
      if (usedMachinesNames.indexOf(newMachineName + pos.toString()) === -1) {
        newMachineName += pos.toString();
        break;
      }
    }

    return newMachineName;
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

    Object.keys(runtime.machines).forEach((machineName: string) => {
      let runtimeMachine = runtime.machines[machineName];
      let machine: any = {name: machineName, servers: {}};
      if (runtimeMachine && runtimeMachine.servers) {
        machine.runtime = {
          servers: runtimeMachine.servers
        };
      }
      machines.push(machine);
    });

    return machines;
  }

  getMachineName(machine: IEnvironmentManagerMachine): string {
    return machine && machine.name ? angular.copy(machine.name) : '';
  }

  /**
   * Renames machine.
   *
   * @param {che.IWorkspaceEnvironment} environment
   * @param {string} oldName
   * @param {string} newName
   * @returns {che.IWorkspaceEnvironment} new environment
   */
  renameMachine(environment: che.IWorkspaceEnvironment, oldName: string, newName: string): che.IWorkspaceEnvironment {

    // update environment config
    environment.machines[newName] = environment.machines[oldName];
    delete environment.machines[oldName];

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
      newEnvironment.machines[machineName].attributes.memoryLimitBytes = machine.attributes ? machine.attributes.memoryLimitBytes : DEFAULT_MEMORY_LIMIT;
      newEnvironment.machines[machineName].installers = angular.copy(machine.installers);
      newEnvironment.machines[machineName].servers = angular.copy(machine.servers);
      newEnvironment.machines[machineName].volumes = angular.copy(machine.volumes);
      newEnvironment.machines[machineName].env = angular.copy(machine.env);
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
    return machine.installers && machine.installers.indexOf(WS_AGENT_NAME) >= 0;
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
      machine.installers = machine.installers ? machine.installers : [];
      if (!hasWsAgent) {
        machine.installers.push(WS_AGENT_NAME);
      }
      if (machine.installers.indexOf(TERMINAL_AGENT_NAME) < 0) {
        machine.installers.push(TERMINAL_AGENT_NAME);
      }
      return;
    }

    if (!isDev && hasWsAgent) {
      machine.installers.splice(machine.installers.indexOf(WS_AGENT_NAME), 1);
    }
  }

  getServers(machine: IEnvironmentManagerMachine): {[serverName: string]: IEnvironmentManagerMachineServer} {
    let servers = angular.copy(machine.servers);

    if (!servers) {
      return {};
    }

    Object.keys(servers).forEach((serverName: string) => {
      servers[serverName].userScope = true;
    });

    if (!machine.runtime) {
      return servers;
    }
    Object.keys(machine.runtime.servers).forEach((runtimeServerName: string) => {
      const runtimeServer: che.IWorkspaceRuntimeMachineServer = machine.runtime.servers[runtimeServerName],
        [protocol] = runtimeServer.url ? runtimeServer.url.split('://') : '-',
        port = runtimeServer.port ? runtimeServer.port : protocol.includes('http') ? '80' : '-';

      if (servers[runtimeServerName]) {
        servers[runtimeServerName].runtime = runtimeServer;
      } else {
        servers[runtimeServerName] = {
          userScope: false,
          path: runtimeServer.url,
          runtime: runtimeServer,
          protocol: protocol,
          port: port
        };
      }
    });

    return servers;
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
    return machine.installers || [];
  }

  setAgents(machine: IEnvironmentManagerMachine, agents: string[]): void {
    machine.installers = angular.copy(agents);
  }

  /**
   * Returns memory limit from machine's attributes
   *
   * @param {IEnvironmentManagerMachine} machine
   * @returns {number} memory limit in bytes
   */
  getMemoryLimit(machine: IEnvironmentManagerMachine): number {
    if (machine && machine.attributes && machine.attributes.memoryLimitBytes) {
      if (angular.isString(machine.attributes.memoryLimitBytes)) {
        return parseInt(<string>machine.attributes.memoryLimitBytes, 10);
      }
      return <number>machine.attributes.memoryLimitBytes;
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
    if (limit) {
      machine.attributes.memoryLimitBytes = limit;
    }
  }
}
