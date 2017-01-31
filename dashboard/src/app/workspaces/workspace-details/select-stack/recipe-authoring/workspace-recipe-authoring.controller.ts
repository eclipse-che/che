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
 * @name workspaces.recipe-authoring.controller:WorkspaceRecipeAuthoringController
 * @description This class is handling the controller for the workspace recipe authoring widget
 * @author Oleksii Orel
 * @author Oleksii Kurinnyi
 */
export class WorkspaceRecipeAuthoringController {
  $timeout: ng.ITimeoutService;

  editingTimeoutPromise: ng.IPromise<any>;

  recipeFormat: string;
  recipeScript: string;
  recipeFormatCopy: string;
  recipeScriptCopy: string;
  recipeChange: Function;

  editorOptions: {
    lineWrapping: boolean,
    lineNumbers: boolean,
    matchBrackets: boolean,
    mode: string,
    onLoad: Function
  };

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($scope: ng.IScope, $timeout: ng.ITimeoutService) {
    this.$timeout = $timeout;

    this.editorOptions = {
      lineWrapping: true,
      lineNumbers: true,
      matchBrackets: true,
      mode: this.recipeFormat,
      onLoad: (editor: any) => {
        this.setEditor(editor);
      }
    };

    $scope.$watch(() => { return this.recipeScript; }, () => {
      this.recipeScriptCopy = this.recipeScript;
    });
    $scope.$watch(() => { return this.recipeFormat; }, () => {
      this.recipeFormatCopy = this.recipeFormat || 'compose';
    });

    this.onRecipeChange();
  }

  setEditor(editor: any): void {
    editor.on('paste', () => {
      let content = editor.getValue();
      this.detectFormat(content);
    });
    editor.on('change', () => {
      let content = editor.getValue();
      this.trackChangesInProgress(content);
    });
  }

  trackChangesInProgress(content: string): void {
    if (this.editingTimeoutPromise) {
      this.$timeout.cancel(this.editingTimeoutPromise);
    }

    this.editingTimeoutPromise = this.$timeout(() => {
      this.detectFormat(content);
    }, 100);
  }

  detectFormat(content: string): void {
    // compose format detection:
    if (content.match(/^services:\n/m)) {
      this.recipeFormatCopy = 'compose';
      this.editorOptions.mode = 'text/x-yaml';
    }

    // docker file format detection
    if (content.match(/^FROM\s+\w+/m)) {
      this.recipeFormatCopy = 'dockerfile';
      this.editorOptions.mode = 'text/x-dockerfile';
    }
  }

  onRecipeChange() {
    this.$timeout(() => {
      this.detectFormat(this.recipeScriptCopy);
      this.recipeChange({
        recipeFormat: this.recipeFormatCopy,
        recipeScript: this.recipeScriptCopy
      });
    }, 10);
  }
}
