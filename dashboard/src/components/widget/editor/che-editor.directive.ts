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
 * @ngdoc directive
 * @name components.directive:cheEditor
 * @restrict E
 * @element
 *
 * @description
 * `<che-editor editor-content="ctrl.editorContent"></che-editor>` for displaying the editor
 *
 * @usage
 *   <che-editor editor-content="ctrl.editorContent"
 *               editor-state="ctrl.editorState"></che-editor>
 *
 * @author Oleksii Orel
 */
export class CheEditor implements ng.IDirective {

  restrict: string = 'E';
  controller: string = 'CheEditorController';
  controllerAs: string = 'cheEditorController';
  transclude: boolean = true;
  bindToController: boolean = true;

  // scope values
  scope = {
    editorContent: '=',
    editorState: '=?',
    editorMode: '@?',
    editorReadOnly: '=?',
    validator: '&?',
    onContentChange: '&?'
  };

  template($element: ng.IAugmentedJQuery, $attrs: ng.IAttributes): string {
    const avoidAttrs = ['ng-model', 'editor-content', 'editor-state', 'editor-mode', 'validator', 'on-content-change'];
    const avoidStartWithAttrs: Array<string> = ['$'];

    let additionalElement = '';

    let keys = Object.keys($attrs.$attr).filter((key: string) => {
      const attr = $attrs.$attr[key];
      return attr && avoidAttrs.indexOf(attr) === -1 && avoidStartWithAttrs.findIndex((avoidStartWithAttr: string) => {
          return attr.startsWith(avoidStartWithAttr);
        }) === -1;
    });

    if (keys.length) {
      let additionalAttr = '';
      keys.forEach((key: string) => {
        const attr = $attrs.$attr[key];
        additionalAttr += `${attr}="${$attrs[key]}" `;
        $element.removeAttr(attr);
      });
      additionalElement += `<div class="custom-checks">
                             <che-input ${additionalAttr} 
                                      type="hidden" 
                                      che-form="cheEditorController.editorForm"
                                      ng-model-options="{ allowInvalid: true }"
                                      ng-model="cheEditorController.editorContent">
                               <ng-transclude></ng-transclude>
                             </che-input>
                           </div>`;
    }

    return `<div class="che-codemirror-editor">
              <ng-form name="cheEditorController.editorForm">
                <textarea ui-codemirror="cheEditorController.editorOptions"
                          aria-label="editor"
                          ng-model-options="{ updateOn: 'default blur', debounce: { 'default': 100, 'blur': 0 }, allowInvalid: true }"
                          ng-model="cheEditorController.editorContent"></textarea>
                <div class="validator-checks">
                  <div ng-messages="cheEditorController.editorForm.$invalid">
                    <div ng-repeat="error in cheEditorController.editorState.errors">{{error}}</div>
                  </div>
                </div>
                ${additionalElement}
              </ng-form>
            </div>`;
  }

}
