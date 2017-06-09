/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
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
 * @name workspaces.recipe-import.controller:WorkspaceRecipeImportController
 * @description This class is handling the controller for the workspace recipe import widget
 * @author Oleksii Orel
 * @author Oleksii Kurinnyi
 */
export class WorkspaceRecipeImportController {
  $timeout: ng.ITimeoutService;

  recipeUrl: string;
  recipeFormat: string;
  recipeUrlCopy: string;
  recipeFormatCopy: string;
  recipeChange: Function;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($scope: ng.IScope, $timeout: ng.ITimeoutService) {
    this.recipeFormat = this.recipeFormat || 'compose';
    this.$timeout = $timeout;

    $scope.$watch(() => { return this.recipeFormat; }, () => {
      this.recipeFormatCopy = this.recipeFormat || 'compose';
    });
    $scope.$watch(() => { return this.recipeUrl; }, () => {
      this.recipeUrlCopy = this.recipeUrl;
    });
  }

  onRecipeChange() {
    this.$timeout(() => {
      this.recipeChange({
        recipeUrl: this.recipeUrlCopy,
        recipeFormat: this.recipeFormatCopy
      });
    }, 10);

  }

}
