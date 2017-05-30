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
import {CheAgent} from '../../../../../components/api/che-agent.factory';
import {CheAPI} from '../../../../../components/api/che-api.factory';

/**
 * @ngdoc controller
 * @name workspace.details.controller:ListAgentsController
 * @description This class is handling the controller for list of agents
 * @author Ilya Buziuk
 */

/** List of the agents which shouldn't be switched by user */
const DISABLED_AGENTS: Array<string> = ['org.eclipse.che.ws-agent',
                                        'com.codenvy.rsync_in_machine',
                                        'com.codenvy.external_rsync'];

export class ListAgentsController {
  cheAgent: CheAgent;

  agents: string[];
  agentsList: any[];
  allAgents: any[];

  agentsOnChange: Function;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
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

