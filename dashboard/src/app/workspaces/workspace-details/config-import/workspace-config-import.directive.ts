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
   * @ngInject for Dependency injection
   */
  constructor() {
    // scope values
    this.scope = {
      workspaceConfig: '=',
      workspaceConfigOnChange: '&'
    };
  }

}
