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
 * Defines a directive for displaying devfile editor widget.
 * @author Anna Shumilova
 */
export class WorkspaceDevfileEditor {
  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/workspace-details/devfile/workspace-devfile-editor.html';
  replace: boolean = false;

  controller: string = 'WorkspaceDevfileEditorController';
  controllerAs: string = 'workspaceDevfileEditorController';

  bindToController: boolean = true;

  scope: {
    [paramName: string]: string;
  };

  /**
   * Default constructor that is using resource
   */
  constructor() {
    // scope values
    this.scope = {
      isActive: '=?',
      editorReadOnly: '=?',
      workspaceDevfile: '=',
      workspaceDevfileOnChange: '&'
    };
  }

}
