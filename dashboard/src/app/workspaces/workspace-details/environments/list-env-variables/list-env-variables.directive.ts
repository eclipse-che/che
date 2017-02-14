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
 * @name workspaces.details.directive:listEnvVariables
 * @restrict E
 * @element
 *
 * @description
 * `<list-env-variables env-variables="ctrl.envVariables" env-variables-on-change="ctrl.onChangeCallback()"></list-env-variables>` for displaying list of environment variables.
 *
 * @usage
 *   <list-env-variables env-variables="ctrl.envVariables" env-variables-on-change="ctrl.onChangeCallback()"></list-env-variables>
 *
 * @author Oleksii Kurinnyi
 */
export class ListEnvVariables {
  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/workspace-details/environments/list-env-variables/list-env-variables.html';

  controller: string = 'ListEnvVariablesController';
  controllerAs: string = 'listEnvVariablesController';
  bindToController: boolean = true;

  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    // scope values
    this.scope = {
      envVariables: '=',
      envVariablesOnChange: '&'
    };
  }
}
