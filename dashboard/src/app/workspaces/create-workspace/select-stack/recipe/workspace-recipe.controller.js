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
 * @ngdoc controller
 * @name workspaces.recipe.controller:WorkspacesRecipeCtrl
 * @description This class is handling the controller for the workspace recipe widget
 * @author Oleksii Orel
 */
export class WorkspaceRecipeController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.recipeUrl = null;

    //set default selection
    this.selectSourceOption = 'upload-custom-stack';

    this.editorOptions = {
      lineWrapping: true,
      lineNumbers: true,
      matchBrackets: true,
      mode: 'text/x-dockerfile'
    };

    this.setDefaultData();
  }

  setDefaultData() {
    this.recipeUrl = null;
    this.recipeScript = '';
  }
}
