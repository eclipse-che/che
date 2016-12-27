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
   * @ngInject for Dependency injection
   */
  constructor () {
    // scope values
    this.scope = {
      agents: '=',
      agentsOnChange: '&'
    };
  }
}
