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

/**
 * Defines a directive for creating slider that are working either on desktop or on mobile devices.
 * It will change upon width of the screen
 * @author Oleksii Kurinnyi
 */
export class CheSlider {

  restrict = 'E';

  replace = true;
  transclude = true;
  templateUrl = 'components/widget/slider/che-slider.html';

  // scope values
  scope = {
    sliderValue : '=cheValue',
    labelName: '@cheLabelName',
    captionUnits: '@cheCaptionUnits',
    captionValueDivider: '@cheCaptionValueDivider',
    captionMaxValue: '@cheCaptionMaxValue'
  };

  compile($element: ng.IAugmentedJQuery, $attrs: ng.IAttributes) {
    const keys = Object.keys($attrs);

    // search the selected field
    const sliderElement = $element.find('md-slider');

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
      if (key === 'ngModel') {
        return;
      }
      let value = $attrs[key];

      // handle empty values as boolean
      if (value === '') {
        value = 'true';
      }

      // set the value of the attribute
      sliderElement.attr($attrs.$attr[key], value);

      $element.removeAttr($attrs.$attr[key]);

    });

  }

}
