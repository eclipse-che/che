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
import {CheAgent} from '../../../../../components/api/che-agent.factory';
import {CheAPI} from '../../../../../components/api/che-api.factory';

/**
 * @ngdoc controller
 * @name workspace.details.controller:ListAgentsController
 * @description This class is handling the controller for list of agents
 * @author Ilya Buziuk
 */

/** List of the agents which shouldn't be switched by user */
const DISABLED_AGENTS: Array<string> = ['com.codenvy.rsync_in_machine',
                                        'com.codenvy.external_rsync'];

export class ListAgentsController {
  static $inject = ['cheAPI'];

  cheAgent: CheAgent;

  agents: string[];
  agentsList: any[];
  allAgents: any[];

  agentsOnChange: Function;

  /**
   * Default constructor that is using resource
   */
  constructor(cheAPI: CheAPI) {
    this.cheAgent = cheAPI.getAgent();

    this.cheAgent.fetchAgents().then(() => {
      this.buildAgentsList();
    });
  }

  buildAgentsList(): void {
    this.agentsList = [];
    this.allAgents = this.cheAgent.getAgents();

    this.allAgents.forEach((agent: any) => {
        let agentItem = angular.copy(agent);
        let isEnabled = this.isEnabled(agent.id, this.agents);
        agentItem.isEnabled = isEnabled;
        this.agentsList.push(agentItem);
    });
  }

  updateAgent(agent: any): void {
    if (agent.isEnabled) {
      this.agents.push(agent.id);
    } else {
      this.agents.splice(this.agents.indexOf(agent.id), 1);
    }
    this.agentsOnChange();
    this.buildAgentsList();
  }

  /**
   * Disables agents which shouldn't be switched by user.
   * Switching of the "ws-agent" must happen only via "Dev" slider.
   * "ws-agent" should be listed, but always disabled regardless of the state
   * @param agentId {string}
   */
  needToDisable(agentId: string): boolean {
    return DISABLED_AGENTS.indexOf(agentId) !== -1;
  }

  isEnabled(agentId: string, agents: string[]): boolean {
    return (-1 !== agents.indexOf(agentId));
  }

}

