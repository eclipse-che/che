/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {CheAgent} from '../../../../components/api/che-agent.factory';

export interface IAgentItem extends che.IAgent {
  isEnabled: boolean;
  isLatest: boolean;
}

/** List of the locked agents which shouldn't be switched by user */
const LOCKED_AGENTS: Array<string> = ['com.codenvy.rsync_in_machine', 'com.codenvy.external_rsync'];
const LATEST: string = 'latest';
/**
 * @ngdoc controller
 * @name machine.agents.controller:MachineAgentsController
 * @description This class is handling the controller for list of machine agents.
 * @author Oleksii Orel
 */
export class MachineAgentsController {

  static $inject = ['$scope', 'cheAgent', '$timeout'];

  onChange: Function;
  agentOrderBy = 'name';
  agentItemsList: Array<IAgentItem>;

  private cheAgent: CheAgent;
  private $timeout: ng.ITimeoutService;
  private timeoutPromise: ng.IPromise<any>;
  private agents: Array<string>;
  private availableAgents: Array<che.IAgent>;
  private agentsToUpdate: Array<IAgentItem> = [];

  /**
   * Default constructor that is using resource
   */
  constructor($scope: ng.IScope, cheAgent: CheAgent, $timeout: ng.ITimeoutService) {
    this.cheAgent = cheAgent;
    this.$timeout = $timeout;

    this.availableAgents = cheAgent.getAgents();
    if (this.availableAgents && this.availableAgents.length) {
      this.buildAgentsList();
    } else {
      cheAgent.fetchAgents().then(() => {
        this.availableAgents = cheAgent.getAgents();
        this.buildAgentsList();
      });
    }

    let agentsCopy = [];
    const deRegistrationFn = $scope.$watch(() => {
      return this.agents;
    }, (newList: string[]) => {
      if (angular.equals(newList, agentsCopy)) {
        return;
      }
      agentsCopy = angular.copy(newList);
      this.buildAgentsList();
    });

    $scope.$on('$destroy', () => {
      deRegistrationFn();
      if (this.timeoutPromise) {
        this.$timeout.cancel(this.timeoutPromise);
      }
    });
  }

  /**
   * Builds agents list.
   */
  buildAgentsList(): void {
    if (!this.agents) {
      return;
    }

    this.agentItemsList = this.availableAgents.map((agentItem: IAgentItem) => {
      this.checkAgentLatestVersion(agentItem);
      return agentItem;
    });
  }

  /**
   * Updates the agent.
   * @param agent {IAgentItem}
   */
  updateAgent(agent: IAgentItem): void {
    this.$timeout.cancel(this.timeoutPromise);

    this.agentsToUpdate.push(agent);

    this.timeoutPromise = this.$timeout(() => {
      this.agentsToUpdate.forEach((agent: IAgentItem) => {
        const index = this.agents.indexOf(agent.id);
        if (agent.isEnabled) {
          if (index === -1) {
            this.agents.push(agent.id);
          }
        } else if (index >= 0) {
          this.agents.splice(index, 1);
        }
      });
      this.agentsToUpdate.length = 0;

      this.buildAgentsList();
      this.onChange();
    }, 500);
  }

  /**
   * Returns true if the agent is locked.
   * @param agentId {string}
   * @returns {boolean}
   */
  isLocked(agentId: string): boolean {
    return LOCKED_AGENTS.indexOf(agentId) !== -1;
  }

  /**
   * Checks agent enabled.
   *
   * @param agentItem {IAgentItem}
   */
  checkEnabled(agentItem: IAgentItem): void {
    for (let i = 0; i < this.agents.length; i++) {
      let agent = this.agents[i];
      // try to extract agent's version in format id:version:
      let groups = agent.match(/[^:]+(:(.+)){0,1}/);
      let id;
      let version = null;
      if (groups && groups.length === 3) {
        id = groups[0];
        version = groups[2];
      } else {
        id = agent;
      }

      // compare by id and version, if no version - consider as latest:
      if (agentItem.id === id && agentItem.version === version) {
        agentItem.isEnabled = true;
        return;
      } else if (agentItem.id === id && (!version || version === LATEST)) {
        agentItem.isEnabled = agentItem.isLatest;
        return;
      }
    }

    agentItem.isEnabled = false;
  }

  /**
   * Fetch the agent by id of the latest version and compare with provided one.
   *
   * @param {IAgentItem} agentItem agent to check on latest version
   */
  private checkAgentLatestVersion(agentItem: IAgentItem): void {
    let latestAgent = this.cheAgent.getAgent(agentItem.id);
    if (latestAgent && latestAgent.version === agentItem.version) {
      agentItem.isLatest = true;
      this.checkEnabled(agentItem);
    } else if (!latestAgent) {
      this.cheAgent.fetchAgent(agentItem.id).then((agent: che.IAgent) => {
        agentItem.isLatest = agentItem.version === agent.version;
        this.checkEnabled(agentItem);
      });
    }
  }
}
