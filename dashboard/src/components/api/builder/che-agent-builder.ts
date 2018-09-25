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

/**
 * This class is providing a builder for Agent
 * @author Oleksii Kurinny
 */
export class CheAgentBuilder {
  agent: che.IAgent;

  /**
   * Default constructor.
   */
  constructor() {
    this.agent = {} as che.IAgent;
    this.agent.servers = {};
    this.agent.dependencies = [];
  }

  /**
   * Sets the id of the agent
   * @param {string} id the id to use
   * @returns {CheAgentBuilder}
   */
  withId(id: string): CheAgentBuilder {
    this.agent.id = id;
    return this;
  }

  /**
   * Sets the name of the agent
   * @param {string} name the name to use
   * @returns {CheAgentBuilder}
   */
  withName(name: string): CheAgentBuilder {
    this.agent.name = name;
    return this;
  }

  /**
   * Sets the description of the agent
   * @param {string} description the description to use
   * @returns {CheAgentBuilder}
   */
  withDescription(description: string): CheAgentBuilder {
    this.agent.description = description;
    return this;
  }

  /**
   * Sets the version of the agent
   * @param {string} version the version to use
   * @returns {CheAgentBuilder}
   */
  withVersion(version: string): CheAgentBuilder {
    this.agent.version = version;
    return this;
  }

  /**
   * Sets the properties of the agent
   * @param {any} properties the properties to use
   * @returns {CheAgentBuilder}
   */
  withProperties(properties: any): CheAgentBuilder {
    this.agent.properties = properties;
    return this;
  }

  /**
   * Sets the script of the agent
   * @param {string} script the script to use
   * @returns {CheAgentBuilder}
   */
  withScript(script: string): CheAgentBuilder {
    this.agent.script = script;
    return this;
  }

  /**
   * Sets the servers of the agent
   * @param {any} servers the servers to use
   * @returns {CheAgentBuilder}
   */
  withServers(servers: any): CheAgentBuilder {
    this.agent.servers = servers;
    return this;
  }

  /**
   * Sets the dependencies of the agent
   * @param {string[]} dependencies the servers to use
   * @returns {CheAgentBuilder}
   */
  withDependencies(dependencies: string[]): CheAgentBuilder {
    this.agent.dependencies = dependencies;
    return this;
  }

  /**
   * Build the agent
   * @return {che.IAgent}
   */
  build(): che.IAgent {
    return this.agent;
  }

}
