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
 * @name workspaces.details.tools.directive:workspaceEditors
 * @restrict E
 * @element
 *
 * @description
 * <workspace-editors></workspace-editors>` for displaying workspace editors.
 *
 * @author Ann Shumilova
 */
export class WorkspaceEditors implements ng.IDirective {
  restrict = 'E';
  templateUrl = 'app/workspaces/workspace-details/workspace-editors/workspace-editors.html';

  controller = 'WorkspaceEditorsController';
  controllerAs = 'workspaceEditorsController';

  bindToController = true;

  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   */
  constructor() {
    this.scope = {
      onChange: '&',
      workspace: '=',
      pluginRegistryLocation: '='
    };
  }
}
