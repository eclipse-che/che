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
 * @author Oleksii Kurinnyi
 */
export class NavbarDropdownMenu {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($timeout) {
    this.$timeout = $timeout;

    this.restrict = 'E';
    this.bindToController = true;
    this.templateUrl = 'app/navbar/navbar-dropdown-menu/navbar-dropdown-menu.html';
    this.controller = 'NavbarDropdownMenuCtrl';
    this.controllerAs = 'navbarDropdownMenuCtrl';

    this.transclude = true;

    // scope values
    this.scope = {
      dropdownItems: '=navbarDropdownItems',
      bindToRightClick: '@?navbarDropdownRightClick',
      isDisabled: '=?navbarDropdownDisabled',
      externalCssClass: '@?navbarDropdownExternalClass',
      offset: '@?navbarDropdownOffset'
    };
  }

  compile($element, attrs) {
    let jqButton = $element.find('[ng-transclude]');
    if (!angular.isUndefined(attrs['navbarDropdownRightClick'])) {
      jqButton.attr('ng-click', '');
      jqButton.attr('che-on-right-click', '$mdOpenMenu($event)');
    } else {
      jqButton.attr('ng-click', '$mdOpenMenu($event)');
    }
  }
}
