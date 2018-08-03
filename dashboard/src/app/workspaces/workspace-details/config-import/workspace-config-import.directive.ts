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
 * Defines a directive for displaying config import widget.
 * @author Oleksii Kurinnyi
 */
export class WorkspaceConfigImport {
  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/workspace-details/config-import/workspace-config-import.html';
  replace: boolean = false;

  controller: string = 'WorkspaceConfigImportController';
  controllerAs: string = 'workspaceConfigImportController';

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
      workspaceConfig: '=',
      workspaceConfigOnChange: '&'
    };
  }

}
