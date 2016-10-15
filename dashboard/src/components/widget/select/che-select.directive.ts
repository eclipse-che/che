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

/**
 * Defines a directive for creating select that are working either on desktop or on mobile devices.
 * It will change upon width of the screen
 * @author Oleksii Orel
 */
export class CheSelect {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.restrict = 'E';

    this.replace = true;
    this.transclude = true;
    this.templateUrl = 'components/widget/select/che-select.html';

    // scope values
    this.scope = {
      value: '=cheValue',
      labelName: '@?cheLabelName',
      placeHolder: '@chePlaceHolder',
      optionValues: '=cheOptionValues'
    };
  }

  compile(element, attrs) {
    let keys = Object.keys(attrs);

    // search the select field
    let selectElements = element.find('md-select');

    keys.forEach((key) => {
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
      selectElements.attr(attrs.$attr[key], attrs[key] !== '' ? attrs[key] : 'true');
      element.removeAttr(attrs.$attr[key]);
    });
  }

}
