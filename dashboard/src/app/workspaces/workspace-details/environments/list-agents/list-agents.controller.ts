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
 * @ngdoc controller
 * @name workspace.details.controller:ListAgentsController
 * @description This class is handling the controller for list of agents
 * @author Ilya Buziuk
 */
export class ListAgentsController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(cheAPI) {
    this.cheAgent = cheAPI.getAgent();

    this.cheAgent.fetchAgents().then(() => {
      this.buildAgentsList();
    });
  }

  buildAgentsList() {
    this.agentsList = [];
    this.allAgents = this.cheAgent.getAgents();

    this.allAgents.forEach(agent => {
        let agentItem = angular.copy(agent);
        let isEnabled = this.isEnabled(agent.id, this.agents);
        agentItem.isEnabled = isEnabled;
        this.agentsList.push(agentItem);
    });
  }

  updateAgent(agent) {
    if (agent.isEnabled) {
      this.agents.push(agent.id);
    } else {
      this.agents.splice(this.agents.indexOf(agent.id), 1);
    }
    this.agentsOnChange();
    this.buildAgentsList();
  }

  /**
   * Switching of the "ws-agent" must happen only via "Dev" slider.
   * "ws-agent" should be listed, but always disabled regardless of the state
   * @param agentId {string}
   */
  needToDisable(agentId) {
    return (agentId === "org.eclipse.che.ws-agent");
  }

  isEnabled(agentId, agents) {
    return (-1 !== agents.indexOf(agentId));
  }

}

