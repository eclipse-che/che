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
/* --------------------------------------------------------------------------------------------
    * Copyright (c) 2018 TypeFox GmbH (http://www.typefox.io). All rights reserved.
    * Licensed under the MIT License. See License.txt in the project root for license information.
    * ------------------------------------------------------------------------------------------ */
'use strict';

const UI_MONACO_CONFIG = {
  wordWrap: 'on',
  lineNumbers: 'on',
  matchBrackets: true,
  autoClosingBrackets: 'always',
  readOnly: false
};

/**
 * Binds a Monaco widget to a div element.
 *
 * @author Oleksii Orel
 * @author Josh Pinkney
 */

/**
 * @ngdoc directive
 * @name components.directive:uiMonaco
 * @restrict A
 * @function
 * @element
 *
 * @description
 * `ui-monaco` defines an attribute for Binds a Monaco widget to a div element.
 *
 * @usage
 *   <div ui-monaco="editorOptions" ng-model="val"></div>
 *
 * @author Oleksii Orel
 * @author Josh Pinkney
 */
export class UiMonaco implements ng.IDirective {

  restrict = 'A';
  require = 'ngModel';

  link($scope: ng.IScope, $element: ng.IAugmentedJQuery, $attrs: ng.IAttributes, $ctrl: ng.INgModelController): void {
    const element = $element[0];
    if (element.tagName !== 'DIV') {
      throw new Error(`the ui-monaco attribute should be used with a div elements only`);
    }

    const Monaco = (window as any).Monaco;
    const uiMonaco: string = ($attrs as any).uiMonaco;
    const monacoOptions = angular.extend(
      {value: ''},
      UI_MONACO_CONFIG,
      $scope.$eval(uiMonaco),
      {
        model: Monaco.editor.createModel('',  'yaml'),
        automaticLayout: true
      });

    const editor = Monaco.editor.create(element, monacoOptions);
    (window as any).MonacoEditor = editor;

    editor.layout({ height: '300' });

    editor.getModel().updateOptions({ tabSize: 2 });

    const monacoDefaults = Object.keys(UI_MONACO_CONFIG);
    this.configOptionsWatcher(editor, monacoDefaults, uiMonaco, $scope);
    this.configNgModelLink(editor, $ctrl, $scope);

    // allow access to the Monaco instance through a broadcasted event
    // eg: $broadcast('Monaco', function(cm){...});
    $scope.$on('Monaco', (event: ng.IAngularEvent, callback: Function) => {
      if (angular.isFunction(callback)) {
        callback(editor);
      } else {
        throw new Error('the Monaco event requires a callback function');
      }
    });

    $scope.$on('$destroy', () => {
        if (editor) {
          editor.getModel().dispose();
          editor.dispose();
        }
    });

    // onLoad callback
    if (angular.isFunction(monacoOptions.onLoad)) {
      // always reset the schema when the editor is loaded so that if its not a devfile editor we don't get the support
      (window as any).yamlService.configure({
        schemas: []
      });
      monacoOptions.onLoad(editor);
    }
  }

  private configOptionsWatcher(editor: any, monacoDefaults: string[], uiMonacoAttr: string, scope: ng.IScope): void {
    if (!uiMonacoAttr) {
      return;
    }

    scope.$watch(uiMonacoAttr, (newValues: Object, oldValue: Object) => {
      if (!angular.isObject(newValues)) {
        return;
      }
      monacoDefaults.forEach((key: string) => {
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
    // monaco expects a string, so make sure it gets one.
    // this does not change the model.
    formatter.push((value: any) => {
      if (angular.isUndefined(value) || value === null) {
        return '';
      } else if (angular.isObject(value) || angular.isArray(value)) {
        throw new Error('ui-monaco cannot use an object or an array as a model');
      }
      return value;
    });

    // override the ngModelController $render method, which is what gets called when the model is updated.
    // this takes care of the synchronizing the monaco element with the underlying model.
    ngModel.$render = () => {
      const safeViewValue = ngModel.$viewValue || '';
      editor.setValue(safeViewValue);
    };

    editor.onDidChangeModelContent(function () {
        var newValue = editor.getValue();
        if (newValue !== ngModel.$viewValue) {
            scope.$evalAsync(function () {
                ngModel.$setViewValue(newValue);
            });
        }
    });
  }

}
