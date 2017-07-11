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
 * @name workspace.machines.item.directive:workspaceMachineItem
 * @restrict E
 * @element
 *
 * @description
 * `<workspace-machine-item></workspace-machine-item>` for displaying workspace machine item.
 *
 * @param {Object=} machine   the machine object
 * @param {string=} page-url  the url of parent page
 * @param {ICheListHelper=} che-list-helper   the list helper
 * @param {Function=} edit-machine  callback
 * @param {Function=} on-ram-change  callback
 * @param {Function=} delete-machine  callback
 * @param {Function=} machine-dev-on-change  callback
 *
 * @usage
 *   <workspace-machine-item  machine="machine"
 *                            che-list-helper="ctrl.cheListHelper"
 *                            edit-machine="ctrl.editMachine(name)"
 *                            delete-machine="ctrl.deleteMachine(name)"
 *                            machine-dev-on-change="ctrl.changeDevMachine(name)"
 *                            on-ram-change="ctrl.onRamChange(name, memoryLimitGBytes)"
 *                            page-url="http://codenvy.com/dashboard/#/workspace/test/wksp"></workspace-machine-item>
 *
 * Defines a directive for displaying machine's item.
 *
 * @author Oleksii Orel
 */
export class WorkspaceMachineItem implements ng.IDirective {
  restrict: string = 'E';
  replace: boolean = true;
  templateUrl: string = 'app/workspaces/workspace-details/workspace-machines/machine-item/workspace-machine-item.html';

  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.scope = {
      pageUrl: '@',
      machine: '=',
      onRamChange: '&',
      machineDevOnChange: '&',
      editMachine: '&',
      deleteMachine: '&',
      cheListHelper: '='
    };
  }
}
