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
 * @name environment.variables.directive:EnvVariables
 * @restrict E
 * @element
 *
 * @description
 * `<che-env-variables></che-env-variables>` for displaying list of environment variables.
 *
 * @usage
 *   <che-env-variables selected-machine="machine" on-change="ctrl.onChangeCallback()"></che-env-variables>
 *
 * @author Oleksii Orel
 */
export class EnvVariables implements ng.IDirective {
  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/workspace-details/workspace-machine-env-variables/env-variables.html';
  controller: string = 'EnvVariablesController';
  controllerAs: string = 'envVariablesController';
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
