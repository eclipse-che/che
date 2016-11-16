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
   * @ngInject for Dependency injection
   */
  constructor() {
    // scope values
    this.scope = {
      tabName: '=cheTabName',
      onTabChange: '&cheOnTabChange',
      stack: '=cheStack',
      onStackChange: '&cheStackChange',
      recipeScript: '=cheRecipeScript',
      recipeUrl: '=cheRecipeUrl',
      recipeFormat: '=cheRecipeFormat'
    };

  }

}
