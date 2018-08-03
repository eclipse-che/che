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
 * Defines a directive for displaying select stack widget.
 * @author Oleksii Orel
 */
export class WorkspaceSelectStack {
  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/workspace-details/select-stack/workspace-select-stack.html';
  replace: boolean = true;
  bindToController: boolean = true;
  controller: string = 'WorkspaceSelectStackController';
  controllerAs: string = 'workspaceSelectStackCtrl';

  scope: {
    [propName: string]: string;
  };

  /**
   * Default constructor that is using resource
   */
  constructor() {
    // scope values
    this.scope = {
      environmentName: '=',
      workspaceName: '=',
      workspaceImportedRecipe: '=',
      workspaceStackOnChange: '&'
    };

  }

}
