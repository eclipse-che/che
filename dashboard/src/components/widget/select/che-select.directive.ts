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

interface ICheSelectAttributes extends ng.IAttributes {
  cheName: string;
}

/**
 * Defines a directive for creating select that are working either on desktop or on mobile devices.
 * It will change upon width of the screen
 * @author Oleksii Orel
 */
export class CheSelect {
  restrict: string = 'E';
  replace: boolean = true;
  transclude: boolean = true;

  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   */
  constructor() {
    // scope values
    this.scope = {
      value: '=cheValue',
      labelName: '@?cheLabelName',
      placeHolder: '@chePlaceHolder',
      optionValues: '=cheOptionValues',
      myName: '=cheName',
      myForm: '=cheForm'
    };
  }

  template($element: ng.IAugmentedJQuery, $attrs: ICheSelectAttributes): string {
    return `<div class="che-select desktop-untouched">
      <!-- Mobile version -->
      <md-input-container class="che-select-mobile" hide-gt-xs>
        <label ng-if="labelName">{{value ? labelName : placeHolder}}</label>
        <md-select ng-model="value"
                   name="${$attrs.cheName}"
                   md-container-class="che-custom-dropdown"
                   placeholder="{{placeHolder}}">
          <md-option ng-value="optionValue.id ? optionValue.id : optionValue.name"
                     ng-repeat="optionValue in optionValues">{{optionValue.name}}
          </md-option>
        </md-select>
        <!-- display error messages for the form -->
        <div ng-messages="myForm.${$attrs.cheName}.$error"></div>
      </md-input-container>

      <!-- Desktop version -->
      <div class="che-select-desktop" hide-xs layout="column" flex>
        <div layout="row" flex layout-align="start start">
          <label flex="15" ng-if="labelName">{{labelName}}: </label>

          <div layout="column" class="che-select-container" flex="{{labelName ? 85 : 'none'}}">
            <md-select ng-model="value"
                       name="desk${$attrs.cheName}"
                       md-container-class="che-custom-dropdown"
                       placeholder="{{placeHolder}}">
              <md-option ng-value="optionValue.id ? optionValue.id : optionValue.name"
                         ng-repeat="optionValue in optionValues">{{optionValue.name}}
              </md-option>
            </md-select>
            <!-- display error messages for the form -->
            <div ng-messages="myForm.desk${$attrs.cheName}.$error" ng-transclude></div>
          </div>
        </div>
      </div>
    </div>`;
  }

  compile($element: ng.IAugmentedJQuery, $attrs: ICheSelectAttributes): void {
    let keys = Object.keys($attrs);

    // search the select field
    let selectElements = $element.find('md-select');

    keys.forEach((key: string) => {
      // don't reapply internal properties
      if (key.indexOf('$') === 0) {
        return;
      }
      // don't reapply internal element properties
      if (key.indexOf('che') === 0) {
        return;
      }
      // don't reapply class
      if (key.indexOf('class') === 0) {
        return;
      }
      // don't reapply model
      if (key.indexOf('ngModel') !== -1) {
        return;
      }

      // set the value of the attribute
      selectElements.attr($attrs.$attr[key], $attrs[key] !== '' ? $attrs[key] : 'true');
      $element.removeAttr($attrs.$attr[key]);
    });
  }

  link($scope: ng.IScope, $element: ng.IAugmentedJQuery, $attrs: ICheSelectAttributes) {
    let deregistrationFn = $scope.$watch('myForm.desk' + $attrs.cheName + '.$touched', (isTouched: boolean) => {
      if (isTouched) {
        $element.removeClass('desktop-untouched');
        $element.addClass('desktop-touched');
        deregistrationFn();
      }
    });
  }

}
