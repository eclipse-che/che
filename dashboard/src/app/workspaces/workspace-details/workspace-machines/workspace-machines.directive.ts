/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

/**
 * @ngdoc directive
 * @name workspace.machines.directive:workspaceMachines
 * @restrict E
 * @element
 *
 * @description
 * `<workspace-machines></workspace-machines>` for displaying workspace machines entry.
 *
 * @param {che.IWorkspace=} workspace-details  the workspace details
 * @param {Function=} on-change  the callback which is called when workspace is changed.
 *
 * @usage
 *   <workspace-machines  workspace-details="ctrl.workspaceDetails"
 *                        on-change="ctrl.onChange()"></workspace-machines>
 *
 * @author Oleksii Orel
 */
export class WorkspaceMachines implements ng.IDirective {
  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/workspace-details/workspace-machines/workspace-machines.html';
  bindToController: boolean = true;
  controller: string = 'WorkspaceMachinesController';
  controllerAs: string = 'workspaceMachinesController';

  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   */
  constructor() {
    this.scope = {
      onChange: '&',
      workspaceDetails: '='
    };
  }
}
