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
 * @name workspaces.details.directive:workspaceEnvironments
 * @restrict E
 * @element
 *
 * @description
 * <workspace-environments></workspace-environmentss>` for displaying workspace environments.
 *
 * @usage
 *   <workspace-environments></workspace-environments>
 *
 * @author Oleksii Kurinnyi
 */
export class WorkspaceEnvironments {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    this.restrict = 'E';
    this.templateUrl = 'app/workspaces/workspace-details/environments/environments.html';

    this.controller = 'WorkspaceEnvironmentsController';
    this.controllerAs = 'workspaceEnvironmentsController';
    this.bindToController = true;

    this.scope = {
      environmentName: '=',
      machinesViewStatus: '=',
      workspaceConfig: '=',
      environmentOnChange: '&'
    }
  }
}

