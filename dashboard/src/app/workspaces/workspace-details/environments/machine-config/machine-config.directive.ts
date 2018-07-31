/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

/**
 * @ngdoc directive
 * @name workspaces.details.directive:workspaceMachineConfig
 * @restrict E
 * @element
 *
 * @description
 * <workspace-machine-config></workspace-machine-config>` for displaying workspace config.
 *
 * @usage
 *   <workspace-machine-config></workspace-machine-config>
 *
 * @author Oleksii Kurinnyi
 */
export class WorkspaceMachineConfig implements ng.IDirective {
  restrict = 'E';
  templateUrl = 'app/workspaces/workspace-details/environments/machine-config/machine-config.html';

  controller = 'WorkspaceMachineConfigController';
  controllerAs = 'workspaceMachineConfigController';
  bindToController = true;

  scope = {
    machine: '=',
    machinesList: '=',
    environmentManager: '=',
    machineDevOnChange: '&',
    machineNameOnChange: '&',
    machineConfigOnChange: '&',
    machineOnDelete: '&',
    machineIsOpened: '='
  };

}

