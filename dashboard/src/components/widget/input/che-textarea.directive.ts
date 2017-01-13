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

import { CheInput } from './che-input.directive';

/**
 * Defines a directive for creating textarea that are working either on desktop or on mobile devices.
 * It will change upon width of the screen
 * @author Oleksii Kurinnyi
 */
export class CheTextarea extends CheInput {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    super();
  }

  /**
   * Template for the current toolbar
   * @param element
   * @param attrs
   * @returns {string} the template
   */
  template(element, attrs) {

    var textareaName = attrs.cheName;
    var labelName = attrs.cheLabelName || '';
    var placeHolder = attrs.chePlaceHolder;
    var pattern = attrs.chePattern;

    var template = '<div class="che-input">'
      + '<md-input-container hide-gt-xs>'
      + '<label>' + labelName + '</label>'
      + '<textarea type="text" name="' + textareaName + '"';
    if (attrs.chePattern) {
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
    if (attrs.chePattern) {
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

}
