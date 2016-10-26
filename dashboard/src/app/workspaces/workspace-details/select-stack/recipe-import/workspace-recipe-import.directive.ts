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
   * @ngInject for Dependency injection
   */
  constructor() {
    // scope values
    this.scope = {
      recipeUrl: '=cheRecipeUrl',
      recipeFormat: '=cheRecipeFormat'
    };

  }

}
