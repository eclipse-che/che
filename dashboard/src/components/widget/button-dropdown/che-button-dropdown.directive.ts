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
 * Defines the super class for for all buttons
 * @author Florent Benoit
 */
export class CheButtonDropdown {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.restrict = 'E';
    this.bindToController = true;
    this.templateUrl = 'components/widget/button-dropdown/che-button-dropdown.html';
    this.controller = 'CheButtonDropdownCtrl';
    this.controllerAs = 'cheButtonDropdownCtrl';

    // scope values
    this.scope = {
      labelText: '@cheButtonDropdownLabel',
      href: '@cheButtonDropdownHref',
      ctrl: '=cheButtonDropdownController',
      isDisabled: '=cheDisabled'
    };
  }

}
