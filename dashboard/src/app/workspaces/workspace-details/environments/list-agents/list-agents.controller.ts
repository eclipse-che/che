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
    }, (error) => {
      if (error.status === 304) {
        this.buildAgentsList();
      }
    });
  }

  buildAgentsList() {
    this.agentsList = [];
    this.availableAgents = this.cheAgent.getAgents();
    this.availableAgents.forEach(agent => {
      let isEnabled = this.isEnabled(agent, this.agents);
      this.agentsList.push({ "name": agent, "isEnabled": isEnabled });
    });
  }

  updateAgent(agent) {
    if (agent.isEnabled) {
      this.agents.push(agent.name);
    } else {
      this.agents.splice(this.agents.indexOf(agent.name), 1);
    }
    return this.agentsOnChange().then(() => { this.buildAgentsList() });
  }

  /**
   * Switching of the "ws-agent" must happen only via "Dev" slider.
   * "ws-agent" should be listed, but always disabled regardless of the state
   * @param agentName {string}
   */
  needToDisable(agentName) {
    return (agentName === "org.eclipse.che.ws-agent");
  }

  isEnabled(agentName, agents) {
    return (-1 !== agents.indexOf(agentName));
  }

}

