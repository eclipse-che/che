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
 * @name machine.agents.directive:MachineAgents
 * @restrict E
 * @element
 *
 * @description
 * `<che-machine-agents></che-machine-agents>` for displaying list of machine agents.
 *
 * @usage
 *   <che-machine-agents selected-machine="machine" on-change="ctrl.onChangeCallback()"></che-machine-agents>
 *
 * @author Oleksii Orel
 */
export class MachineAgents implements ng.IDirective {
  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/workspace-details/workspace-machine-agents/machine-agents.html';
  controller: string = 'MachineAgentsController';
  controllerAs: string = 'machineAgentsController';
  bindToController: boolean = true;
  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   */
  constructor() {
    // scope values
    this.scope = {
      machine: '=selectedMachine',
      onChange: '&',
      isPluginsEnabled: '='
    };
  }
}
