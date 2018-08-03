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
 * @ngdoc directive
 * @name workspaces.details.directive:listAgents
 * @restrict E
 * @element
 *
 * @description
 * `<list-agents agents="ctrl.agents" agents-on-change="agents.onChangeCallback()"></list-agents>` for displaying list of agents
 *
 * @usage
 *   <list-agents agents="ctrl.agents" agents-on-change="agents.onChangeCallback()"></list-agents>
 *
 * @author Ilya Buziuk
 */
export class ListAgents {
  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/workspace-details/environments/list-agents/list-agents.html';

  controller: string = 'ListAgentsController';
  controllerAs: string = 'listAgentsController';
  bindToController: boolean = true;

  scope: {
    [paramName: string]: string
  };

  /**
   * Default constructor that is using resource
   */
  constructor () {
    // scope values
    this.scope = {
      agents: '=',
      agentsOnChange: '&'
    };
  }
}
