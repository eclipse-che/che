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
interface ICheInputScope extends ng.IScope {
  isChanged: Function;
  inputName: string;
  valueModel: string;
}
interface ICheInputAttrs extends ng.IAttributes {
  cheName: string;
  cheLabelName: string;
  chePlaceHolder: string;
  chePattern: string;
  cheReadonly: string;
  cheDisabled: string;
  cheWidth: string;
  ngChange: string;
}

/**
 * Defines a directive for creating input that are working either on desktop or on mobile devices.
 * It will change upon width of the screen
 * @author Florent Benoit
 */
export class CheInput implements ng.IDirective {
  restrict = 'E';
  replace = true;
  transclude = true;

  // we require ngModel as we want to use it inside our directive
  require = ['ngModel'];

  // scope values
  scope: { [propName: string]: string; };

  /**
   * Default constructor that is using resource
   */
  constructor() {
    // scope values
    this.scope = {
      valueModel: '=ngModel',
      inputName: '@cheName',
      labelName: '@?cheLabelName',
      placeHolder: '@chePlaceHolder',
      pattern: '@chePattern',
      myForm: '=cheForm',
      isChanged: '&?ngChange',
      readonly: '=cheReadonly',
      disabled: '=cheDisabled'
    };
  }

  /**
   * Template for the current toolbar
   * @param element {ng.IAugmentedJQuery}
   * @param attrs {ICheInputAttrs}
   * @returns {string} the template
   */
  template(element: ng.IAugmentedJQuery, attrs: ICheInputAttrs): string {
    const inputName = attrs.cheName;
    const labelName = attrs.cheLabelName || '';
    const placeHolder = attrs.chePlaceHolder;
    const pattern = attrs.chePattern;

    let template = '<div class="che-input">'
      + '<md-input-container hide-gt-xs ng-class="{\'che-input-mobile-no-label\': !labelName}">'
      + '<label ng-if="labelName">' + labelName + '</label>'
      + '<input aria-label="input {{inputName}}" type="text" name="' + inputName + '"';
    if (attrs.chePattern) {
      template = template + ' pattern="' + pattern + '"';
    }
    if (attrs.cheReadonly) {
      template = template + ' ng-readonly="readonly"';
    }
    if (attrs.cheDisabled) {
      template = template + ' ng-disabled="disabled"';
    }
    if (attrs.ngChange) {
      template = template + ' ng-change="isChanged({$value: valueModel})" ';
    }
    template = template + ' ng-trim="false" ng-model="valueModel" >'
      + '<md-icon class="fa fa-pencil che-input-icon che-input-icon-xs"></md-icon>'
      + '<!-- display error messages for the form -->'
      + '<div ng-messages="myForm.' + inputName + '.$error"></div>'
      + '</md-input-container>'
      + '<div class="che-input-desktop" hide-xs layout="column">'
      + '<div layout="row" layout-align="start start">'
      + '<label flex="15" class="che-input-desktop-label" ng-if="labelName">' + labelName + ': </label>'
      + '<div layout="column" class="che-input-desktop-value-column" flex="{{labelName ? 85 : \'none\'}}">'
      + '<input type="text" data-ng-model="valueModel" placeholder="' + placeHolder + '" ng-trim="false" name="desk' + inputName + '"';
    if (!labelName) {
      template = template + ' style="width: 100%"';
    }
    if (attrs.chePattern) {
      template = template + ' pattern="' + pattern + '"';
    }
    if (attrs.cheReadonly) {
      template = template + ' ng-readonly="readonly"';
    }
    if (attrs.cheDisabled) {
      template = template + ' ng-disabled="disabled"';
    }
    if (attrs.ngChange) {
      template = template + ' ng-change="isChanged({$value: valueModel})" ';
    }
    template = template + '><md-icon class="fa fa-pencil che-input-icon"></md-icon>';
    if (attrs.cheWidth === 'auto') {
      template = template + '<div class="che-input-desktop-hidden-text">{{valueModel ? valueModel : placeHolder}}</div>';
    }
    template = template + '<!-- display error messages for the form -->'
      + '<div ng-messages="myForm.desk' + inputName + '.$error" ng-transclude></div>'
      + '</div>'
      + '</div>'
      + '</div>'
      + '</div>';

    return template;
  }

  compile(element: ng.IAugmentedJQuery, attrs: ng.IAttributes): ng.IDirectiveCompileFn {
    const tabindex = 'tabindex';
    const avoidAttrs = ['ng-model', 'ng-change'];
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
      if (avoidAttrs.indexOf(attr) !== -1) {
        return;
      }
      const avoidAttr = avoidStartWithAttrs.find((avoidStartWithAttr: string) => {
        return attr.indexOf(avoidStartWithAttr) === 0;
      });
      if (angular.isDefined(avoidAttr)) {
        return;
      }
      const value = attrs[key];
      // remember tabindex
      if (attr === tabindex) {
        tabIndex = value;
      }
      // set the value of the attribute
      inputJqEl.attr(attr, value);
      // add also the material version of max length (only one the first input which is the md-input)
      element.removeAttr(attr);
    });

    // the focusable element is the input, remove tabIndex from top-level element
    element.attr(tabindex, -1);
    // the default value for tabindex on the input is 0 (meaning: set 0 if no value was set)
    if (!tabIndex) {
      inputJqEl.attr(tabindex, 0);
    }

    return;
  }

  /**
   * Keep reference to the model controller.
   * @param $scope {ICheInputScope}
   * @param element {ng.IAugmentedJQuery}
   * @param attrs {ng.IAttributes}
   */
  link($scope: ICheInputScope, element: ng.IAugmentedJQuery, attrs: ng.IAttributes): void {
    $scope.$watch(() => {
      return element.is(':visible');
    }, () => {
      // since there are two inputs (for mobile and desktop versions) - add id attr only for visible one:
      const id = 'id';
      if (attrs.$attr[id]) {
        element.find('input:hidden').removeAttr(id);
        element.find('input:visible').attr(id, attrs[id]);
      }
    });

    $scope.$watch('myForm.desk' + $scope.inputName + '.$pristine', (isPristine: boolean) => {
      const desktopPristineClass = 'desktop-pristine';
      if (isPristine) {
        element.addClass(desktopPristineClass);
      } else {
        element.removeClass(desktopPristineClass);
      }
    });
  }
}
