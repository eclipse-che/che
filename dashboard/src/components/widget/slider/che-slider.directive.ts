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

/**
 * Defines a directive for creating slider that are working either on desktop or on mobile devices.
 * It will change upon width of the screen
 * @author Oleksii Kurinnyi
 */
export class CheSlider {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    this.restrict = 'E';

    this.replace = true;
    this.transclude = true;
    this.templateUrl = 'components/widget/slider/che-slider.html';

    // scope values
    this.scope = {
      sliderValue : '=cheValue',
      labelName: '@cheLabelName',
      captionUnits: '@cheCaptionUnits',
      captionValueDivider: '@cheCaptionValueDivider',
      captionMaxValue: '@cheCaptionMaxValue'
    };
  }

  compile (element, attrs) {
    var keys = Object.keys(attrs);

    // search the selected field
    var sliderElement = element.find('md-slider');

    keys.forEach((key) => {

      // don't reapply internal properties
      if (key.indexOf('$') === 0) {
        return;
      }
      // don't reapply internal element properties
      if (key.indexOf('che') === 0) {
        return;
      }
      // avoid model
      if (key === 'ngModel') {
        return;
      }
      var value = attrs[key];

      // handle empty values as boolean
      if (value === '') {
        value = 'true';
      }

      // set the value of the attribute
      sliderElement.attr(attrs.$attr[key], value);

      element.removeAttr(attrs.$attr[key]);

    });

  }

  link () { }
}
