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

import {CheButton} from './che-button.directive';

/**
 * @ngdoc directive
 * @name components.directive:cheButtonPrimaryFlat
 * @restrict E
 * @function
 * @element
 *
 * @description
 * `<che-button-primary-flat>` defines a default button.
 *
 * @param {string=} che-button-title the title of the button
 * @param {string=} che-button-icon the optional icon of the button
 *
 * @usage
 *   <che-button-primary-flat che-button-title="hello"></che-button-primary-flat>
 *
 * @example
 <example module="userDashboard">
 <file name="index.html">
 <che-button-primary-flat che-button-title="Hello"></che-button-primary-flat>
 <che-button-primary-flat che-button-title="Hello" che-button-icon="fa fa-file-text-o"></che-button-primary-flat>
 </file>
 </example>
 * @author Oleksii Kurinnyi
 */
export class CheButtonPrimaryFlat extends CheButton {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    super();
  }

  /**
   * Template for the buttons
   */
  getTemplateStart() {
    return '<md-button md-theme=\"default\" class=\"che-button md-accent md-hue-2\"';
  }

}
