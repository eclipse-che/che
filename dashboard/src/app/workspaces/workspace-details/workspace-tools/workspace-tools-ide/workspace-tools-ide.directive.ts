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
 * @name workspaces.details.tools.directive:workspaceToolsIde
 * @restrict E
 *
 * @author Ann Shumilova
 */
export class WorkspaceToolsIde implements ng.IDirective {
  restrict = 'E';
  templateUrl = 'app/workspaces/workspace-details/workspace-tools/workspace-tools-ide/workspace-tools-ide.html';

  controller = 'WorkspaceToolsIdeController';
  controllerAs = 'workspaceToolsIdeController';

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
      machine: '=',
      environmentManager: '='
    };
  }
}
