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
 * @name machine.servers.directive:MachineServers
 * @restrict E
 * @element
 *
 * @description
 * `<che-machine-servers></che-machine-servers>` for displaying list of machine servers.
 *
 * @usage
 *   <che-machine-servers selected-machine="machine" on-change="ctrl.onChangeCallback()"></che-machine-servers>
 *
 * @author Oleksii Orel
 */
export class MachineServers implements ng.IDirective {
  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/workspace-details/workspace-machine-servers/machine-servers.html';
  controller: string = 'MachineServersController';
  controllerAs: string = 'machineServersController';
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
      environmentManager: '=',
      selectedMachine: '=',
      onChange: '&'
    };
  }
}
