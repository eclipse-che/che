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

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    this.restrict = 'E';
    this.templateUrl = 'app/workspaces/workspace-details/environments/list-env-variables/list-env-variables.html';

    this.controller = 'ListEnvVariablesController';
    this.controllerAs = 'listEnvVariablesController';
    this.bindToController = true;

    // scope values
    this.scope = {
      envVariables: '=',
      envVariablesOnChange: '&'
    };
  }
}
