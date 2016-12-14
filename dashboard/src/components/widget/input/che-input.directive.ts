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

interface IInputScope extends ng.IScope {
  inputName: string;
  isChanged: Function;
}

/**
 * Defines a directive for creating input that are working either on desktop or on mobile devices.
 * It will change upon width of the screen
 * @author Florent Benoit
 */
export class CheInput {
  restrict: string = 'E';
  replace: boolean = true;
  transclude: boolean = true;

  // we require ngModel as we want to use it inside our directive
  require: string[] = ['ngModel'];

  // scope values
  scope: {
    [propName: string]: string;
  };

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    // scope values
    this.scope = {
      valueModel: '=ngModel',
      inputName: '@cheName',
      placeHolder: '@chePlaceHolder',
      pattern: '@chePattern',
      myForm: '=cheForm',
      isChanged: '&ngChange'
    };
  }


  /**
   * Template for the current toolbar
   * @param element
   * @param attrs
   * @returns {string} the template
   */
  template(element: ng.IAugmentedJQuery, attrs: any): string {

    let inputName = attrs.cheName,
        placeHolder = attrs.chePlaceHolder,
        pattern = attrs.chePattern;

    let template = '<div class="che-input">'
      + '<div class="che-input-desktop" layout="column">'
      + '<div layout="column" class="che-input-desktop-value-column">'
      + '<input type="text" placeholder="' + placeHolder + '" ng-trim="false" name="' + inputName + '"';
    if (attrs.chePattern) {
      template = template + ' pattern="' + pattern + '"';
    }
    template = template + ' data-ng-model="valueModel">';

    template = template +
      '<!-- display error messages for the form -->'
      + '<div ng-messages="myForm.' + inputName + '.$error" role="alert" ng-transclude></div>'
      + '</div>'
      + '</div>'
      + '</div>';

    return template;
  }

  compile(element: ng.IAugmentedJQuery, attrs: any) {

    let keys = Object.keys(attrs);

    // search the input field
    let inputElement = element.find('input');

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
      // don't reapply ngChange
      if ('ngChange' === key) {
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
      inputElement.attr(attrs.$attr[key], value);

      element.removeAttr(attrs.$attr[key]);

    });

    // the focusable element is the input, remove tabIndex from top-level element
    element.attr('tabindex', -1);
    // the default value for tabindex on the input is 0 (meaning: set 0 if no value was set)
    if (!tabIndex) {
      inputElement.attr('tabindex', 0);
    }
  }

  /**
   * Keep reference to the model controller
   */
  link($scope: IInputScope, element: ng.IAugmentedJQuery, attr: any): void {
    $scope.$watch('myForm.desk' + $scope.inputName + '.$pristine', (isPristine: boolean) => {
      if (isPristine) {
        element.addClass('desktop-pristine');
      } else {
        element.removeClass('desktop-pristine');
      }
    });

    if (!attr.ngChange) {
      return;
    }
    // for ngChange attribute only
    $scope.$watch('valueModel', () => {
      $scope.isChanged();
    });

  }
}
