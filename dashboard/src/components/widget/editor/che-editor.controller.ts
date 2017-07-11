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

interface IEditor {
  on(name: string, listener: (...args: any[]) => any);
  getDoc(): any;
}

interface IEditorState {
  isValid: boolean;
  errors: Array<string>;
}

/**
 * @ngdoc controller
 * @name components.directive:cheEditorController
 * @description This class is handling the controller for the editor.
 * @author Oleksii Orel
 */
export class CheEditorController {
  /**
   * Editor options object.
   */
  private editorOptions: {
    mode?: string;
    onLoad: Function;
  };
  /**
   * Editor state object.
   */
  private editorState: IEditorState = {isValid: true, errors: []};
  /**
   * Custom validator callback.
   */
  private validator: Function;
  /**
   * On content change callback.
   */
  private onContentChange: Function;
  /**
   * Editor mode.
   */
  private editorMode: string;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($timeout: ng.ITimeoutService) {
    this.editorOptions = {
      mode: angular.isString(this.editorMode) ? this.editorMode : 'application/json',
      onLoad: (editor: IEditor) => {
        const doc = editor.getDoc();
        editor.on('change', () => {
          $timeout(() => {
            this.editorState.errors.length = 0;
            let editorErrors: Array<{ id: string; message: string }> = doc.getAllMarks().filter((mark: any) => {
              return mark.className && mark.className.includes('error');
            }).map((mark: any) => {
              const annotation = '__annotation';
              let message: string;
              if (mark[annotation]) {
                message = mark[annotation].message.split('\n').map((part: string) => {
                  return !part.includes('-^') ? part : '... -';
                }).join(' ');
              } else {
                message = 'Parse error';
              }
              return {id: mark.id, message: message};
            });
            if (angular.isFunction(this.validator)) {
              const customValidatorState: IEditorState = this.validator();
              if (customValidatorState && angular.isArray(customValidatorState.errors)) {
                customValidatorState.errors.forEach((error: string) => {
                  this.editorState.errors.push(error);
                });
              }
            }
            editorErrors.forEach((editorError: { id: string; message: string }) => {
              this.editorState.errors.push(editorError.message);
            });
            this.editorState.isValid = this.editorState.errors.length === 0;
            if (angular.isFunction(this.onContentChange)) {
              this.onContentChange({editorState: this.editorState});
            }
          }, 1000);
        });
      }
    };
  }

  /**
   * Returns validation state of the editor content.
   * @returns {boolean}
   */
  isEditorValid(): boolean {
    return this.editorState && this.editorState.isValid;
  }
}
