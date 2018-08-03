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
 * @name components.directive:cheButtonSaveFlat
 * @restrict E
 * @function
 * @element
 *
 * @description
 * `<che-button-save-flat>` defines a flat save button.
 *
 * @param {string=} che-button-title the title of the button
 * @param {string=} che-button-icon the optional icon of the button
 *
 * @usage
 *   <che-button-save-flat che-button-title="save"></che-button-save-flat>
 *
 * @example
 * <example module="userDashboard">
 * <file name="index.html">
 * <che-button-save-flat che-button-title="Save"></che-button-save-flat>
 * <che-button-save-flat che-button-title="Save" che-button-icon="fa fa-floppy-o"></che-button-save-flat>
 * </file>
 * </example>
 * @author Oleksii Kurinnyi
 */
export class CheButtonSaveFlat extends CheButton {

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
    return '<md-button md-theme=\"chesave\" class=\"che-button md-accent md-raised md-hue-1\"';
  }

}
