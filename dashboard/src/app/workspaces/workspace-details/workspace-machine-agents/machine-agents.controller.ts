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
import {IEnvironmentManagerMachine} from '../../../../components/api/environment/environment-manager-machine';
import {EnvironmentManager} from '../../../../components/api/environment/environment-manager';
import {CheAgent} from '../../../../components/api/che-agent.factory';

export interface IAgentItem {
  id: string;
  name: string;
  description: string;
  isEnabled: boolean;
  [propName: string]: any;
}

/** List of the locked agents which shouldn't be switched by user */
const LOCKED_AGENTS: Array<string> = ['org.eclipse.che.ws-agent', 'com.codenvy.rsync_in_machine', 'com.codenvy.external_rsync'];

/**
 * @ngdoc controller
 * @name machine.agents.controller:MachineAgentsController
 * @description This class is handling the controller for list of machine agents.
 * @author Oleksii Orel
 */
export class MachineAgentsController {
  onChange: Function;
  agentOrderBy = 'name';
  agentsList: Array<IAgentItem>;

  private cheAgent: CheAgent;
  private selectedMachine: IEnvironmentManagerMachine;
  private environmentManager: EnvironmentManager;
  private agents: Array<string>;


  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($scope: ng.IScope, cheAgent: CheAgent) {
    this.cheAgent = cheAgent;

    cheAgent.fetchAgents().then(() => {
      this.agents = this.selectedMachine ? this.environmentManager.getAgents(this.selectedMachine) : [];
      this.buildAgentsList();
    });

    const deRegistrationFn = $scope.$watch(() => {
      return angular.isDefined(this.environmentManager) && this.selectedMachine;
    }, (selectedMachine: IEnvironmentManagerMachine) => {
      if (!selectedMachine) {
        return;
      }
      this.agents = this.environmentManager.getAgents(selectedMachine);
      this.buildAgentsList();
    }, true);

    $scope.$on('$destroy', () => {
      deRegistrationFn();
    });
  }

  /**
   * Builds agents list.
   */
  buildAgentsList(): void {
    const agents = this.cheAgent.getAgents();
    this.agentsList = agents.map((agentItem: IAgentItem) => {
      agentItem.isEnabled = this.isEnabled(agentItem.id);
      return agentItem;
    });
  }

  /**
   * Updates the agent.
   * @param agent {IAgentItem}
   */
  updateAgent(agent: IAgentItem): void {
    if (agent.isEnabled) {
      this.agents.push(agent.id);
    } else {
      this.agents.splice(this.agents.indexOf(agent.id), 1);
    }
    this.environmentManager.setAgents(this.selectedMachine, this.agents);
    this.onChange();
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
   * Returns true if the agent is enabled.
   * @param agentId {string}
   * @returns {boolean}
   */
  isEnabled(agentId: string): boolean {
    return (this.agents.indexOf(agentId) !== -1);
  }
}
