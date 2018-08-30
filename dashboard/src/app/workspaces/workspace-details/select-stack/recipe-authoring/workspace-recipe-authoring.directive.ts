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
 * Defines a directive for displaying recipe authoring widget.
 * @author Oleksii Orel
 * @author Oleksii Kurinnyi
 */
export class WorkspaceRecipeAuthoring {
  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/workspace-details/select-stack/recipe-authoring/workspace-recipe-authoring.html';
  replace: boolean = false;

  controller: string = 'WorkspaceRecipeAuthoringController';
  controllerAs: string = 'workspaceRecipeAuthoringController';

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
      recipeScript: '=cheRecipeScript',
      recipeFormat: '=cheRecipeFormat',
      recipeChange: '&cheRecipeChange'
    };

  }

}
