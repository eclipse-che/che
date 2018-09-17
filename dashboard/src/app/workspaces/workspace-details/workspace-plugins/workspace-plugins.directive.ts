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
 * @name workspaces.details.tools.directive:workspacePlugins
 * @restrict E
 * @element
 *
 * @description
 * <workspace-plugins></workspace-plugins>` for displaying workspace plugins.
 *
 * @author Ann Shumilova
 */
export class WorkspacePlugins implements ng.IDirective {
  restrict = 'E';
  templateUrl = 'app/workspaces/workspace-details/workspace-plugins/workspace-plugins.html';

  controller = 'WorkspacePluginsController';
  controllerAs = 'workspacePluginsController';

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
      workspaceConfig: '=',
      pluginRegistryLocation: '='
    };
  }
}
