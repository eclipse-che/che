/*
 * Copyright (c) 2015-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
  templateUrl: string = 'components/widget/editor/che-editor.html';
  controller: string = 'CheEditorController';
  controllerAs: string = 'cheEditorController';
  transclude: boolean = true;
  bindToController: boolean = true;

  // scope values
  scope = {
    editorContent: '=',
    editorState: '=?',
    editorMode: '@?',
    validator: '&?',
    onContentChange: '&?'
  };

  compile(element: ng.IRootElementService, attrs: ng.IAttributes): ng.IDirectiveCompileFn {
    const avoidAttrs = ['ng-model', 'editor-content', 'editor-state', 'editor-mode', 'validator', 'on-content-change'];
    const avoidStartWithAttrs: Array<string> = ['$'];

    const keys = Object.keys(attrs.$attr);
    // search the input field
    const inputJqEl = element.find('.custom-checks che-input');

    keys.forEach((key: string) => {
      const attr = attrs.$attr[key];
      if (!attr) {
        return;
      }
      if (avoidStartWithAttrs.find((avoidStartWithAttr: string) => {
          return attr.indexOf(avoidStartWithAttr) === 0;
        })) {
        return;
      }
      if (avoidAttrs.indexOf(attr) !== -1) {
        return;
      }
      let value = attrs[key];

      // set the value of the attribute
      inputJqEl.attr(attr, value);
      // add also the material version of max length (only one the first input which is the md-input)
      element.removeAttr(attr);
    });

    return;
  }

}
