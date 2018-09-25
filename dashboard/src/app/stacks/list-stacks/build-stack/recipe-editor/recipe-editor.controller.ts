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

import {IRecipeEditorControllerScope} from './recipe-editor.directive';

/**
 * This class is handling the controller for the recipe editor.
 * @author Oleksii Kurinnyi
 */
export class RecipeEditorController implements IRecipeEditorControllerScope {

  static $inject = ['$timeout', '$scope'];

  /**
   * Allows to pass a not valid recipe to onChange callback if `true`
   * Passed from parent controller.
   */
  allowInvalidRecipe: boolean;
  /**
   * Editor mode.
   * Passed from parent controller.
   */
  editorMode: string;
  /**
   * Callback which is called when editor content changes.
   * Passed from parent controller.
   */
  changeRecipeFn: (data: {content: string}) => void;
  /**
   * Callback which is called when editor content changes. It returns validation error if recipe is not valid.
   * Passed from parent controller.
   */
  validateRecipeFn: (data: {content: string}) => string;
  /**
   * Contains recipe validation error.
   */
  recipeValidationError: string;
  /**
   * Recipe content.
   */
  recipeContent: string;
  /**
   * Recipe editor's form controller.
   */
  form: ng.IFormController;
  /**
   * <code>true</code> if recipe content was edited.
   */
  isFormDirty = false;

  /**
   * Timeout service.
   */
  $timeout: ng.ITimeoutService;
  /**
   * Timeout promise.
   */
  timeoutPromise: ng.IPromise<any>;
  /**
   * CodeMirror's editor options.
   */
  private editorOptions: {
    lineWrapping: boolean,
    lineNumbers: boolean,
    matchBrackets: boolean,
    mode: string,
    onLoad: Function
  };

  /**
   * Default constructor that is using resource
   */
  constructor($timeout: ng.ITimeoutService,
              $scope: ng.IScope) {
    this.$timeout = $timeout;

    this.editorOptions = {
      lineWrapping: true,
      lineNumbers: true,
      matchBrackets: true,
      mode: this.editorMode,
      onLoad: (editor: any) => {
        this.setEditor(editor);
        editor.focus();
        $timeout(() => {
          editor.refresh();
        }, 100);
      }
    };

    $scope.$on('$destroy', () => {
      if (this.timeoutPromise) {
        this.$timeout.cancel(this.timeoutPromise);
      }
    });
  }

  /**
   * Handle editor events.
   *
   * @param editor
   */
  setEditor(editor: any): void {
    editor.on('paste', () => {
      let content = editor.getValue();
      this.trackChangesInProgress(content);
    });
    editor.on('change', () => {
      let content = editor.getValue();
      this.trackChangesInProgress(content);
    });
  }

  /**
   * Callback which is called when editor content is changed.
   *
   * @param {string} content
   */
  trackChangesInProgress(content: string): void {
    if (this.timeoutPromise) {
      this.$timeout.cancel(this.timeoutPromise);
    }

    this.timeoutPromise = this.$timeout(() => {
      // trigger form validation
      this.recipeContent = content;
      this.isFormDirty = this.isFormDirty || this.form.$dirty;

      this.recipeValidationError = this.validateRecipeFn({content: content});
      if (this.recipeValidationError && !this.allowInvalidRecipe) {
        content = null;
      }
      this.changeRecipeFn({content: content});
    }, 500);
  }

  /**
   * Returns validation state of the recipe.
   * Used in custom-validation directive in the template.
   *
   * @returns {boolean}
   */
  isRecipeValid(): boolean {
    return !this.recipeValidationError || this.recipeValidationError.length === 0;
  }

}
