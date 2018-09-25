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
  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/workspace-details/environments/environments.html';

  controller: string = 'WorkspaceEnvironmentsController';
  controllerAs: string = 'workspaceEnvironmentsController';
  bindToController: boolean = true;

  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   */
  constructor () {
    // scope values
    this.scope = {
      workspaceRuntime: '=',
      workspaceCreationFlow: '=',
      workspaceName: '=',
      stackId: '=',
      environmentName: '=',
      machinesViewStatus: '=',
      workspaceConfig: '=',
      workspaceImportedRecipe: '=',
      environmentOnChange: '&'
    };
  }
}

