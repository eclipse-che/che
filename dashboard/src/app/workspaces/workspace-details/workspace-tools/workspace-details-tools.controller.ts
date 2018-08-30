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
import {IEnvironmentManagerMachine} from '../../../../components/api/environment/environment-manager-machine';
import {EnvironmentManager} from '../../../../components/api/environment/environment-manager';

const IDE_TOOL_TYPE: string = 'ide';

/**
 * @ngdoc controller
 * @name workspaces.details.tools.controller:WorkspaceDetailsToolsController
 * @description This class is handling the controller for details of workspace : section tools
 * @author Ann Shumilova
 */
export class WorkspaceDetailsToolsController {
  static $inject = ['lodash'];

  lodash: any;
  selectedMachine: IEnvironmentManagerMachine;
  environmentManager: EnvironmentManager;
  onChange: Function;

  /**
   * Default constructor that is using resource
   */
  constructor(lodash: any) {
    this.lodash = lodash;
  }

  isIDE(machine: IEnvironmentManagerMachine): boolean {
    if (!machine) {
      return false;
    }

    let serverAttributes = this.lodash.pluck(machine.servers, 'attributes');
    for (let i = 0; i < serverAttributes.length; i++) {
      // todo needs refinements when the way of defining tools will be implemented
      if (serverAttributes[i].type === IDE_TOOL_TYPE) {
        return true;
      }
    }
    return false;
  }
}
