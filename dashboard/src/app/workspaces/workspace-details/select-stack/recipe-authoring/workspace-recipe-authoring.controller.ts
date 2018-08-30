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
import {ComposeParser} from '../../../../../components/api/environment/compose-parser';
import {DockerfileParser} from '../../../../../components/api/environment/docker-file-parser';
import {CheBranding} from '../../../../../components/branding/che-branding.factory';

const DOCKERFILE = 'dockerfile';
const COMPOSE = 'compose';

/**
 * @ngdoc controller
 * @name workspaces.recipe-authoring.controller:WorkspaceRecipeAuthoringController
 * @description This class is handling the controller for the workspace recipe authoring widget
 * @author Oleksii Orel
 * @author Oleksii Kurinnyi
 */
export class WorkspaceRecipeAuthoringController {

  static $inject = ['$scope', '$timeout', 'cheBranding'];

  $timeout: ng.ITimeoutService;

  composeParser: ComposeParser;
  dockerfileParser: DockerfileParser;
  recipeValidationError: string;

  editingTimeoutPromise: ng.IPromise<any>;

  recipeFormat: string;
  recipeScript: string;
  recipeFormatCopy: string;
  recipeScriptCopy: string;
  stackDocsUrl: string;
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
   */
  constructor($scope: ng.IScope, $timeout: ng.ITimeoutService, cheBranding: CheBranding) {
    this.$timeout = $timeout;
    this.composeParser = new ComposeParser();
    this.dockerfileParser = new DockerfileParser();
    this.stackDocsUrl = cheBranding.getDocs().stack;

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
      this.recipeFormatCopy = this.recipeFormat || COMPOSE;
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
      this.validateRecipe(content);
    }, 100);
  }

  detectFormat(content: string): void {
    // compose format detection:
    if (content.match(/^services:\n/m)) {
      this.recipeFormatCopy = COMPOSE;
      this.editorOptions.mode = 'text/x-yaml';
    }

    // docker file format detection
    if (content.match(/^FROM\s+\w+/m)) {
      this.recipeFormatCopy = DOCKERFILE;
      this.editorOptions.mode = 'text/x-dockerfile';
    }
  }

  validateRecipe(content: string): void {
    this.recipeValidationError = '';

    if (!content) {
      return;
    }

    try {
      if (this.recipeFormatCopy === DOCKERFILE) {
        this.dockerfileParser.parse(content);
      } else if (this.recipeFormatCopy === COMPOSE) {
        this.composeParser.parse(content);
      }
    } catch (e) {
      this.recipeValidationError = e.message;
    }
  }

  /**
   * Returns validation state of the recipe.
   * @returns {boolean}
   */
  isRecipeValid(): boolean {
    return angular.isUndefined(this.recipeValidationError) || this.recipeValidationError.length === 0;
  }

  onRecipeChange(): void {
    this.$timeout(() => {
      this.detectFormat(this.recipeScriptCopy);
      this.recipeChange({
        recipeFormat: this.recipeFormatCopy,
        recipeScript: this.recipeScriptCopy
      });
    }, 10);
  }
}
