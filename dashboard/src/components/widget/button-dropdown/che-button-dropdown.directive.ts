/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

/**
 * Defines the super class for for all buttons
 * @author Florent Benoit
 */
export class CheButtonDropdown implements ng.IDirective {

  restrict = 'E';
  bindToController = true;
  templateUrl = 'components/widget/button-dropdown/che-button-dropdown.html';
  controller = 'CheButtonDropdownCtrl';
  controllerAs = 'cheButtonDropdownCtrl';

  // scope values
  scope = {
    labelText: '@cheButtonDropdownLabel',
    href: '@cheButtonDropdownHref',
    ctrl: '=cheButtonDropdownController',
    isDisabled: '=cheDisabled'
  };

}
