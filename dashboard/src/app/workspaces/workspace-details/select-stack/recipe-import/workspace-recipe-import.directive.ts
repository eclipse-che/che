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
 * Defines a directive for displaying recipe import widget.
 * @author Oleksii Orel
 * @author Oleksii Kurinnyi
 */
export class WorkspaceRecipeImport {
  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/workspace-details/select-stack/recipe-import/workspace-recipe-import.html';
  replace: boolean = false;

  controller: string = 'WorkspaceRecipeImportController';
  controllerAs: string = 'workspaceRecipeImportCtrl';

  bindToController: boolean = true;

  scope: {
    [paramName: string]: string
  };

  /**
   * Default constructor that is using resource
   */
  constructor() {
    // scope values
    this.scope = {
      recipeUrl: '=cheRecipeUrl',
      recipeFormat: '=cheRecipeFormat',
      recipeChange: '&cheRecipeChange'
    };

  }

}
