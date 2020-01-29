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

import { CheAPI } from '../../api/che-api.factory';

interface IEditor {
  render: Function;
  onDidBlurEditorWidget: Function;
  getModel(): any;
  getCursor(): ICursorPos;
  setCursor(cursorPos: ICursorPos): void;
}

interface ICursorPos {
  line: number;
  column: number;
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

  $timeout: ng.ITimeoutService;

  setEditorValue: (content: string) => void;
  /**
   * Editor options object.
   */
  private editorOptions: {
    mode?: string;
    readOnly?: boolean;
    wordWrap?: string;
    lineNumbers?: string;
    onLoad: Function;
  };

  /**
   * Editor form controller.
   */
  private editorForm: ng.IFormController;
  /**
   * Editor state object.
   */
  private editorState: IEditorState;
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
  private cursorPos: ICursorPos = { line: 0, column: 0 };

  /**
   * Default constructor that is using resource injection
   */
  constructor($timeout: ng.ITimeoutService) {
    this.$timeout = $timeout;
  }

  $onInit(): void {
    this.editorState = { isValid: true, errors: [] };
    this.editorOptions = {
      mode: angular.isString(this.editorMode) ? this.editorMode : 'application/json',
      readOnly: this.editorReadOnly ? this.editorReadOnly : false,
      lineNumbers: 'on',
      wordWrap: 'on',
      onLoad: (editor: IEditor) => {
        const doc = editor.getModel();
        this.setEditorValue = (content: string) => {
          doc.setValue(content);
        };
        doc.onDidChangeContent(() => {
          this.$timeout(() => {
            this.editorState.errors.length = 0;
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
              this.onContentChange({ editorState: this.editorState });
            }

            this.editorForm.$setValidity('custom-validator', this.editorState.isValid, null);
          }, 500);
        });
        this.YAMLValidation();
      }
    };
  }

  YAMLValidation() {
    require('regenerator-runtime');
    const MODEL_URI = 'inmemory://model.yaml';
    const MONACO_URI = monaco.Uri.parse(MODEL_URI);

    const p2m = new (window as any).monacoConversion.ProtocolToMonacoConverter();

    function createDocument(model) {
      return (window as any).yamlLanguageServer.TextDocument.create(
        MODEL_URI,
        model.getModeId(),
        model.getVersionId(),
        model.getValue()
      );
    }

    const yamlService = (window as any).yamlService;
    const pendingValidationRequests = new Map();

    const getModel = () => monaco.editor.getModels()[0];

    const cleanPendingValidation = (document) => {
      const request = pendingValidationRequests.get(document.uri);
      if (request !== undefined) {
        clearTimeout(request);
        pendingValidationRequests.delete(document.uri);
      }
    };

    const cleanDiagnostics = () =>
      monaco.editor.setModelMarkers(monaco.editor.getModel(MONACO_URI), 'default', []);

    const doValidate = (document) => {
      if (document.getText().length === 0) {
        cleanDiagnostics();
        return;
      }
      yamlService.doValidation(document, false).then((diagnostics) => {
        const markers = p2m.asDiagnostics(diagnostics);
        monaco.editor.setModelMarkers(getModel(), 'default', markers);
      });
    };

    getModel().onDidChangeContent(() => {
      const document = createDocument(getModel());
      cleanPendingValidation(document);
      pendingValidationRequests.set(
        document.uri,
        setTimeout(() => {
          pendingValidationRequests.delete(document.uri);
          doValidate(document);
        })
      );
    });
  }

  /**
   * Returns validation state of the editor content.
   * @returns {boolean}
   */
  isEditorValid(): boolean {
    return this.editorState && this.editorState.isValid;
  }
}
