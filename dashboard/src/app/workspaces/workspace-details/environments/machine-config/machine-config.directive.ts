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
export class WorkspaceMachineConfig {
  restrict = 'E';
  templateUrl = 'app/workspaces/workspace-details/environments/machine-config/machine-config.html';

  controller = 'WorkspaceMachineConfigController';
  controllerAs = 'workspaceMachineConfigController';
  bindToController = true;

  scope = {
    machineName: '=',
    machinesList: '=',
    environmentManager: '=',
    machineDevOnChange: '&',
    machineNameOnChange: '&',
    machineConfigOnChange: '&',
    machineOnDelete: '&',
    machineIsOpened: '='
  };

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () { }
}

