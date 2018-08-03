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

import {CheButton} from './che-button.directive';

/**
 * @ngdoc directive
 * @name components.directive:cheButtonDefault
 * @restrict E
 * @function
 * @element
 *
 * @description
 * `<che-button-default>` defines a default button.
 *
 * @param {string=} che-button-title the title of the button
 * @param {string=} che-button-icon the optional icon of the button
 *
 * @usage
 *   <che-button-default che-button-title="hello"></che-button-default>
 *
 * @example
 * <example module="userDashboard">
 * <file name="index.html">
 * <che-button-default che-button-title="Hello"></che-button-default>
 * <che-button-default che-button-title="Hello" che-button-icon="fa fa-check-square"></che-button-default>
 * </file>
 * </example>
 * @author Florent Benoit
 */
export class CheButtonDefault extends CheButton {

  /**
   * Default constructor that is using resource
   */
  constructor () {
    super();
  }


  /**
   * Template for the buttons
   */
  getTemplateStart(): string {
    return '<md-button md-theme=\"chedefault\" class=\"che-button md-accent md-raised md-hue-2\"';
  }

}
