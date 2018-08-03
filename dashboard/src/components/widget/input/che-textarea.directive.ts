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

import { CheInput } from './che-input.directive';

interface ICheTextareaAttributes extends ng.IAttributes {
  cheName: string;
  cheLabelName: string;
  chePlaceHolder: string;
  chePattern: string;
}

/**
 * Defines a directive for creating textarea that are working either on desktop or on mobile devices.
 * It will change upon width of the screen
 * @author Oleksii Kurinnyi
 */
export class CheTextarea extends CheInput {

  /**
   * Default constructor that is using resource
   */
  constructor() {
    super();
  }

  /**
   * Template for the current toolbar
   * @param $element
   * @param $attrs
   * @returns {string} the template
   */
  template($element: ng.IAugmentedJQuery, $attrs: ICheTextareaAttributes) {

    const textareaName = $attrs.cheName;
    const labelName = $attrs.cheLabelName || '';
    const placeHolder = $attrs.chePlaceHolder;
    const pattern = $attrs.chePattern;

    let template = '<div class="che-input">'
      + '<md-input-container hide-gt-xs>'
      + '<label>' + labelName + '</label>'
      + '<textarea type="text" name="' + textareaName + '"';
    if ($attrs.chePattern) {
      template = template + ' pattern="' + pattern + '"';
    }

    template = template + ' ng-trim="false" data-ng-model="valueModel" ></textarea>'
      + '<md-icon class="fa fa-pencil che-input-icon che-input-icon-xs"></md-icon>'
      + '<!-- display error messages for the form -->'
      + '<div ng-messages="myForm.' + textareaName + '.$error"></div>'
      + '</md-input-container>'
      + ''
      + '<div class="che-input-desktop" hide-xs layout="column">'
      + '<div layout="row" layout-align="start start">'
      + '<label flex="15" class="che-input-desktop-label" ng-if="labelName">' + labelName + ': </label>'
      + ''
      + '<div layout="column" class="che-input-desktop-value-column" flex="{{labelName ? 85 : \'none\'}}">'
      + '<textarea type="text" placeholder="' + placeHolder + '" ng-trim="false" name="desk' + textareaName + '" style="{{labelName ? \'width: 100%\' : \'\'}}"';
    if ($attrs.chePattern) {
      template = template + ' pattern="' + pattern + '"';
    }

    template = template + ' data-ng-model="valueModel"></textarea>'
      + '<md-icon class="fa fa-pencil che-input-icon"></md-icon>'
      + '<!-- display error messages for the form -->'
      + '<div ng-messages="myForm.desk' + textareaName + '.$error" ng-transclude></div>'
      + '</div>'
      + '</div>'
      + '</div>'
      + '</div>';

    return template;
  }

  compile(element: ng.IRootElementService, attrs: ng.IAttributes): ng.IDirectiveCompileFn {
    const tabindex = 'tabindex';
    const avoidAttrs = ['ng-model', 'ng-change'];
    const avoidStartWithAttrs: Array<string> = ['$', 'che-'];

    const keys = Object.keys(attrs.$attr);
    // search the input field
    const inputJqEl = element.find('textarea');
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

}
