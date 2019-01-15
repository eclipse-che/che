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

interface IEditor {
  refresh: Function;
  on(name: string, listener: (...args: any[]) => any);
  getDoc(): any;
  getCursor(): ICursorPos;
  setCursor(cursorPos: ICursorPos): void;
}

interface ICursorPos {
  line: number;
  ch: number;
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

  static $inject = ['$timeout'];

  setEditorValue: (content: string) => void;
  /**
   * Editor options object.
   */
  private editorOptions: {
    mode?: string;
    readOnly?: boolean;
    lineWrapping?: boolean;
    lineNumbers?: boolean;
    onLoad: Function;
  };
  /**
   * Editor form controller.
   */
  private editorForm: ng.IFormController;
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
   * Is editor read only.
   */
  private editorReadOnly: boolean;
  /**
   * Editor mode.
   */
  private editorMode: string;
  /**
   * Cursor position.
   */
  private cursorPos: ICursorPos = {line: 0, ch: 0};

  /**
   * Default constructor that is using resource injection
   */
  constructor($timeout: ng.ITimeoutService) {
    this.editorOptions = {
      mode: angular.isString(this.editorMode) ? this.editorMode : 'application/json',
      readOnly: this.editorReadOnly ? this.editorReadOnly : false,
      lineWrapping: true,
      lineNumbers: true,
      onLoad: (editor: IEditor) => {
        $timeout(() => {
          //to avoid Ctrl+Z clear the content
          editor.getDoc().clearHistory();
          editor.refresh();
        }, 2500);
        const doc = editor.getDoc();
        this.setEditorValue = (content: string) => {
          doc.setValue(content);
        };
        editor.on('change', () => {
          const {line, ch} = editor.getCursor();
          if (line === 0 && ch === 0) {
            editor.setCursor(this.cursorPos);
          } else {
            this.cursorPos.ch = ch;
            this.cursorPos.line = line;
          }
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
            editorErrors.forEach((editorError: { id: string; message: string }) => {
              if (!editorError || !editorError.message) {
                return;
              }
              this.editorState.errors.push(editorError.message);
            });
            if (angular.isFunction(this.validator)) {
              try {
                const customValidatorState: IEditorState = this.validator();
                if (customValidatorState && angular.isArray(customValidatorState.errors)) {
                  customValidatorState.errors.forEach((error: string) => {
                    this.editorState.errors.push(error);
                  });
                }
              } catch (error) {
                this.editorState.errors.push(error.toString());
              }
            }
            this.editorState.isValid = this.editorState.errors.length === 0;
            if (angular.isFunction(this.onContentChange)) {
              this.onContentChange({editorState: this.editorState});
            }

            this.editorForm.$setValidity('custom-validator', this.editorState.isValid, null);
          }, 500);
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
