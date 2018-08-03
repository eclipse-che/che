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
 * @ngdoc controller
 * @name workspaces.recipe-import.controller:WorkspaceRecipeImportController
 * @description This class is handling the controller for the workspace recipe import widget
 * @author Oleksii Orel
 * @author Oleksii Kurinnyi
 */
export class WorkspaceRecipeImportController {

  static $inject = ['$scope', '$timeout'];

  $timeout: ng.ITimeoutService;

  recipeUrl: string;
  recipeFormat: string;
  recipeUrlCopy: string;
  recipeFormatCopy: string;
  recipeChange: Function;

  /**
   * Default constructor that is using resource
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
