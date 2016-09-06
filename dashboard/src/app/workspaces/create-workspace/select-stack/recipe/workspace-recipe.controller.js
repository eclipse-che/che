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
  constructor($timeout) {
    this.$timeout = $timeout;
    this.recipeUrl = null;

    //set default selection
    this.selectSourceOption = 'upload-custom-stack';

    this.setDefaultData();

    this.editorOptions = {
      lineWrapping: true,
      lineNumbers: true,
      matchBrackets: true,
      mode: this.recipeFormat,
      onLoad: (editor) => {
        this.setEditor(editor);
      }
    };
  }

  setDefaultData() {
    this.recipeUrl = null;
    this.recipeScript = '';
    this.recipeFormat = 'compose';
  }

  setEditor(editor) {
    this.editor = editor;
    editor.on('paste', () => {
      this.detectFormat(editor, true);
    });
    editor.on('change', () => {
      this.trackChangesInProgress(editor);
    });
  }

  trackChangesInProgress(editor) {
    if (this.editingTimeoutPromise) {
      this.$timeout.cancel(this.editingTimeoutPromise);
    }

    this.editingTimeoutPromise = this.$timeout(() => {
      this.detectFormat(editor, false);
    }, 1000);
  }

  detectFormat(editor, doFormating) {
    let content = editor.getValue();

    //compose format detection:
    if (content.match(/^services:\n/m)) {
      this.recipeFormat = 'compose';
      this.editorOptions.mode = 'text/x-yaml';
    }

    //docker file format detection
    if (content.match(/^FROM /m)) {
      this.recipeFormat = 'dockerfile';
      this.editorOptions.mode = 'text/x-dockerfile';
    }
  }

  formatLines(editor) {
    this.$timeout(() => {
      for(var i = 0; i <= editor.lineCount(); i++) {
        editor.indentLine(i);
      }
    }, 100);
  }
}
