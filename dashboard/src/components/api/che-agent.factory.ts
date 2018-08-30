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
interface IAgentsResource<T> extends ng.resource.IResourceClass<T> {
  getAgents: any;
  getAgent: any;
}

/**
 * This class is handling the agents retrieval
 * It sets to the array of agents
 * @author Ilya Buziuk
 */
export class CheAgent {

  static $inject = ['$resource', '$q'];

  private $resource: ng.resource.IResourceService;
  private $q: ng.IQService;
  private agentsMap: Map<string, che.IAgent> = new Map();
  private agents: che.IAgent[];
  private remoteAgentAPI: IAgentsResource<any>;

  /**
   * Default constructor that is using resource
   */
  constructor($resource: ng.resource.IResourceService, $q: ng.IQService) {
    this.$resource = $resource;
    this.$q = $q;

    // agents
    this.agents = [];

    // remote call
    this.remoteAgentAPI = <IAgentsResource<any>>this.$resource('/api/installer', {}, {
      getAgents: { method: 'GET', url: '/api/installer', isArray: true },
      getAgent: {method: 'GET', url: '/api/installer/:id'}
    });
  }

  /**
   * Fetch the agents.
   */
  fetchAgents(): ng.IPromise<che.IAgent[]> {
    const defer = this.$q.defer();
    const promise = this.remoteAgentAPI.getAgents().$promise;

    promise.then((agents: che.IAgent[]) => {
      // reset global list
      this.agents.length = 0;

      agents.forEach((agent: che.IAgent) => {
        this.agentsMap.set(agent.id, agent);
        this.agents.push(agent);
      });
      defer.resolve(this.agents);
    }, (error: any) => {
      if (error.status !== 304) {
        defer.reject(error);
      } else {
        defer.resolve(this.agents);
      }
    });

    return defer.promise;
  }

  /**
   * Returns the list of all agents.
   *
   * @returns {che.IAgent[]}
   */
  getAgents(): che.IAgent[] {
    return this.agents;
  }

  /**
   * Fetches the info of the agent by id (the latest version will be returned).
   *
   * @param {string} agentId agent's id to fetch
   * @returns {angular.IPromise<che.IAgent>}
   */
  fetchAgent(agentId: string): ng.IPromise<che.IAgent> {
    let defer = this.$q.defer();
    let promise = this.remoteAgentAPI.getAgent({id: agentId}).$promise;
    promise.then((agent: any) => {
      this.agentsMap.set(agentId, agent);
      defer.resolve(agent);
    }, (error: any) => {
      if (error.status !== 304) {
        defer.reject(error);
      } else {
        defer.resolve(this.agentsMap.get(agentId));
      }
    });

    return defer.promise;
  }

  /**
   * Returns agent by id.
   *
   * @param {string} agentId
   * @returns {che.IAgent}
   */
  getAgent(agentId: string): che.IAgent {
    return this.agentsMap.get(agentId);
  }
}
