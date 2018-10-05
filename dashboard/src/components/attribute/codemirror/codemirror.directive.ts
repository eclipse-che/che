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


const UD_CODEMIRROR_CONFIG = {
  lineWrapping: true,
  lineNumbers: true,
  mode: 'application/json',
  gutters: ['CodeMirror-lint-markers', 'CodeMirror-linenumbers', 'CodeMirror-foldgutter'],
  lint: true,
  matchBrackets: true,
  autoCloseBrackets: true,
  foldGutter: true,
  styleActiveLine: true,
  theme: 'che'
};

/**
 * Binds a CodeMirror widget to a textarea element.
 *
 * @author Oleksii Orel
 */

/**
 * @ngdoc directive
 * @name components.directive:uiCodemirror
 * @restrict A
 * @function
 * @element
 *
 * @description
 * `ui-codemirror` defines an attribute for Binds a CodeMirror widget to a textarea element.
 *
 * @usage
 *   <textarea ui-codemirror="editorOptions" ng-model="val"></textarea>
 *
 * @author Oleksii Orel
 */
export class UiCodemirror implements ng.IDirective {

  restrict = 'A';
  require = 'ngModel';

  link($scope: ng.IScope, $element: ng.IAugmentedJQuery, $attrs: ng.IAttributes, $ctrl: ng.INgModelController): void {
    const element = $element[0];
    if (element.tagName !== 'TEXTAREA') {
      throw new Error(`the ui-codemirror attribute should be used with a textarea elements only`);
    }

    const uiCodemirror: string = ($attrs as any).uiCodemirror;
    const codeMirrorOptions = angular.extend(
      {value: ''},
      UD_CODEMIRROR_CONFIG,
      $scope.$eval(uiCodemirror));

    const CodeMirror = (window as any).CodeMirror;
    const codeMirrorDefaults = Object.keys(CodeMirror.defaults);
    const editor = CodeMirror.fromTextArea(element, codeMirrorOptions);

    this.configOptionsWatcher(editor, codeMirrorDefaults, uiCodemirror, $scope);
    this.configNgModelLink(editor, $ctrl, $scope);

    // allow access to the CodeMirror instance through a broadcasted event
    // eg: $broadcast('CodeMirror', function(cm){...});
    $scope.$on('CodeMirror', (event: ng.IAngularEvent, callback: Function) => {
      if (angular.isFunction(callback)) {
        callback(editor);
      } else {
        throw new Error('the CodeMirror event requires a callback function');
      }
    });

    // onLoad callback
    if (angular.isFunction(codeMirrorOptions.onLoad)) {
      codeMirrorOptions.onLoad(editor);
    }
  }

  private configOptionsWatcher(editor: any, codeMirrorDefaults: string[], uiCodemirrorAttr: string, scope: ng.IScope): void {
    if (!uiCodemirrorAttr) {
      return;
    }

    scope.$watch(uiCodemirrorAttr, (newValues: Object, oldValue: Object) => {
      if (!angular.isObject(newValues)) {
        return;
      }
      codeMirrorDefaults.forEach((key: string) => {
        if (newValues.hasOwnProperty(key)) {
          if (oldValue && newValues[key] === oldValue[key]) {
            return;
          }
          editor.setOption(key, newValues[key]);
        }
      });
    }, true);

  }

  private configNgModelLink(editor: any, ngModel: ng.INgModelController, scope: ng.IScope): void {
    if (!ngModel) {
      return;
    }

    const formatter: ng.IModelFormatter[] = ngModel.$formatters;
    // codeMirror expects a string, so make sure it gets one.
    // this does not change the model.
    formatter.push((value: any) => {
      if (angular.isUndefined(value) || value === null) {
        return '';
      } else if (angular.isObject(value) || angular.isArray(value)) {
        throw new Error('ui-codemirror cannot use an object or an array as a model');
      }
      return value;
    });

    // override the ngModelController $render method, which is what gets called when the model is updated.
    // this takes care of the synchronizing the codeMirror element with the underlying model.
    ngModel.$render = () => {
      const safeViewValue = ngModel.$viewValue || '';
      editor.setValue(safeViewValue);
    };

    // keep the ngModel in sync with changes from CodeMirror
    editor.on('change', (instance: any) => {
      let newValue = instance.getValue();
      if (newValue !== ngModel.$viewValue) {
        scope.$evalAsync(() => {
          ngModel.$setViewValue(newValue);
        });
      }
    });
  }
}
