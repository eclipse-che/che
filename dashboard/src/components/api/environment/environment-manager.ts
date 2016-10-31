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

/**
 * This is base class, which describes the environment manager.
 * It's aim is to handle machines retrieval and editing, based on the type of environment.
 */

const WS_AGENT_NAME: string = 'org.eclipse.che.ws-agent';
const TERMINAL_AGENT_NAME: string = 'org.eclipse.che.terminal';
const SSH_AGENT_NAME: string = 'org.eclipse.che.ssh';

export class EnvironmentManager {

  constructor() { }

  get editorMode(): string {
    return '';
  }

  canRenameMachine(machine: any): boolean {
    return false;
  }

  canDeleteMachine(machine: any): boolean {
    return false;
  }

  canEditEnvVariables(machine: any): boolean {
    return false;
  }

  /**
   * Retrieves the list of machines.
   *
   * @param {*} environment
   * @returns {Array} list of machines defined in environment
   */
  getMachines(environment: any): any[] {
    return [];
  }

  /**
   * Renames machine.
   *
   * @param environment {object}
   * @param oldName {string}
   * @param newName {string}
   */
  renameMachine(environment: any, oldName: string, newName: string): void {
    throw new TypeError('EnvironmentManager: cannot rename machine.');
  }

  /**
   * Removes machine.
   *
   * @param environment {object}
   * @param name {string} name of machine
   */
  deleteMachine(environment: any, name: string): void {
    throw new TypeError('EnvironmentManager: cannot delete machine.');
  }

  /**
   * Provides the environment configuration based on machines format.
   *
   * @param environment origin of the environment to be edited
   * @param machines the list of machines
   * @returns environment's configuration
   */
  getEnvironment(environment: any, machines: any): any {
    let newEnvironment = angular.copy(environment);

    machines.forEach((machine) => {
      let machineName = machine.name;

      if (angular.isUndefined(newEnvironment.machines)) {
        newEnvironment.machines = {};
      }
      if (angular.isUndefined(newEnvironment.machines[machineName])) {
        newEnvironment.machines[machineName] = {'attributes': {}};
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
   * @param machine
   * @returns {boolean}
   */
  isDev(machine: any): boolean {
    return machine.agents && machine.agents.includes(WS_AGENT_NAME);
  }

  /**
   * Set machine as developer one - contains 'ws-agent' agent.
   *
   * @param machine machine to edit
   * @param isDev defined whether machine is developer or not
   */
  setDev(machine: any, isDev: boolean): void {
    let hasWsAgent = this.isDev(machine);
    if (isDev) {
      machine.agents = machine.agents ? machine.agents : [];
      if (!hasWsAgent) {
        machine.agents.push(WS_AGENT_NAME);
      }
      if (!machine.agents.includes(SSH_AGENT_NAME)) {
        machine.agents.push(SSH_AGENT_NAME);
      }
      if (!machine.agents.includes(TERMINAL_AGENT_NAME)) {
        machine.agents.push(TERMINAL_AGENT_NAME);
      }
      return;
    }

    if (!isDev && hasWsAgent) {
      machine.agents.splice(machine.agents.indexOf(WS_AGENT_NAME), 1);
    }
  }

  getServers(machine: any): any {
    return machine.servers || {};
  }

  setServers(machine: any, servers: any): void {
    machine.servers = angular.copy(servers);
  }

  getAgents(machine: any): any[] {
    return machine.agents || [];
  }

  setAgents(machine: any, agents: any[]): void {
    machine.agents = angular.copy(agents);
  }

  /**
   * Returns memory limit from machine's attributes
   *
   * @param machine
   * @returns {number|string} memory limit in bytes
   */
  getMemoryLimit(machine: any): number|string {
    if (machine && machine.attributes && machine.attributes.memoryLimitBytes) {
      return machine.attributes.memoryLimitBytes;
    }

    return -1;
  }

  /**
   * Sets the memory limit of the pointed machine.
   * Value in attributes has the highest priority,
   *
   * @param machine machine to change memory limit
   * @param limit memory limit
   */
  setMemoryLimit(machine: any, limit: number): void {
    machine.attributes = machine.attributes ? machine.attributes : {};
    machine.attributes.memoryLimitBytes = limit;
  }

  getEnvVariables(machine: any): any {
    return null;
  }
}
