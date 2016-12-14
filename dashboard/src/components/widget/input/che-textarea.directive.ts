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
  template(element: ng.IAugmentedJQuery, attrs: any): string {

    let textareaName = attrs.cheName;
    let placeHolder = attrs.chePlaceHolder;

    let template = '<div class="che-input">'
      + '<div class="che-input-desktop" layout="column">'
      + '<div layout="column" class="che-input-desktop-value-column">'
      + '<textarea type="text" placeholder="' + placeHolder + '" ng-trim="false" name="' + textareaName + '" data-ng-model="valueModel"></textarea>'
      + '<!-- display error messages for the form -->'
      + '<div ng-messages="myForm.' + textareaName + '.$error" ng-transclude></div>'
      + '</div>'
      + '</div>'
      + '</div>';

    return template;
  }

}
