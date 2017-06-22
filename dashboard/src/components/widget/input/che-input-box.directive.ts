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


  compile(element: ng.IRootElementService, attrs: ng.IAttributes) {
    const keys = Object.keys(attrs);
    // search the input field
    const inputJqEl = element.find('input');
    let tabIndex;
    keys.forEach((key: string) => {
      // don't reapply internal properties
      if (key.indexOf('$') === 0) {
        return;
      }
      // don't reapply internal element properties
      if (key.indexOf('che') === 0) {
        return;
      }
      // avoid model
      if ('ngModel' === key) {
        return;
      }
      let value = attrs[key];
      // remember tabindex
      if (key === 'tabindex') {
        tabIndex = value;
      }
      // handle empty values as boolean
      if (value === '') {
        value = 'true';
      }
      // set the value of the attribute
      inputJqEl.attr(attrs.$attr[key], value);
      // add also the material version of max length (only one the first input which is the md-input)
      element.removeAttr(attrs.$attr[key]);
    });
    // the focusable element is the input, remove tabIndex from top-level element
    element.attr('tabindex', -1);
    // the default value for tabindex on the input is 0 (meaning: set 0 if no value was set)
    if (!tabIndex) {
      inputJqEl.attr('tabindex', 0);
    }
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
