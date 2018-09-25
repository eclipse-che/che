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

interface IInputBoxScope extends ng.IScope {
  valueModel: string;
  inputName: string;
  labelName?: string;
  labelDescription?: string;
  placeHolder?: string;
  pattern?: string;
  myForm: ng.IFormController;
  onChange?: Function;
  isReadonly?: boolean;
}

/**
 * Defines a directive for creating input that are working either on desktop or on mobile devices.
 * It will change upon width of the screen
 * @author Ann Shumilova
 * @author Oleksii Orel
 */
export class CheInputBox {
  restrict = 'E';
  replace = true;
  transclude = true;
  templateUrl: string = 'components/widget/input/che-input-box.html';

  // we require ngModel as we want to use it inside our directive
  require = ['ngModel'];

  // scope values
  scope = {
    valueModel: '=ngModel',
    inputName: '@?cheName',
    labelName: '@?cheLabelName',
    labelDescription: '@?cheLabelDescription',
    placeHolder: '@?chePlaceHolder',
    myForm: '=cheForm',
    onChange: '&?cheOnChange',
    isReadonly: '=?cheReadonly'
  };


  compile(element: ng.IAugmentedJQuery, attrs: ng.IAttributes): ng.IDirectivePrePost {
    const avoidAttrs = ['ng-model'];
    const avoidStartWithAttrs: Array<string> = ['$', 'che-'];

    const keys = Object.keys(attrs.$attr);
    // search the input field
    const inputJqEl = element.find('input');
    let tabIndex;
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
      // remember tabindex
      if (attr === 'tabindex') {
        tabIndex = value;
      }
      // set the value of the attribute
      inputJqEl.attr(attr, value);
      // add also the material version of max length (only one the first input which is the md-input)
      element.removeAttr(attr);
    });
    // the focusable element is the input, remove tabIndex from top-level element
    element.attr('tabindex', -1);
    // the default value for tabindex on the input is 0 (meaning: set 0 if no value was set)
    if (!tabIndex) {
      inputJqEl.attr('tabindex', 0);
    }

    return;
  }

  /**
   * Keep reference to the model controller
   */
  link($scope: IInputBoxScope, element: ng.IRootElementService, attrs: ng.IAttributes) {
    const inputJqEl = element.find('input');
    const required = 'required';
    if (angular.isDefined($scope.placeHolder)) {
      inputJqEl.attr('placeholder', attrs.$attr[required] === required ? $scope.placeHolder + ' *' : $scope.placeHolder);
    }
    if (!angular.isFunction($scope.onChange)) {
      inputJqEl.removeAttr('ng-change');
    }
  }
}
