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
 * Defines the super class for for all buttons
 * @author Oleksii Kurinnyi
 */
export class NavbarDropdown {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($timeout) {
    this.$timeout = $timeout;

    this.restrict = 'E';
    this.bindToController = true;
    this.templateUrl = 'app/navbar/navbar-dropdown/navbar-dropdown.html';
    this.controller = 'NavbarDropdownCtrl';
    this.controllerAs = 'navbarDropdownCtrl';

    this.transclude = true;
    this.replace = true;

    // scope values
    this.scope = {
      dropdownItems: '=navbarDropdownItems',
      moveDropdownAbove: '@?navbarDropdownAbove'
    };
  }

  link($scope, $element, $attrs, ctrl) {
    let elemHeight = $element.height();
    let $dropdownList = $element.find('.navbar-dropdown-elements');
    if ($dropdownList.length) {
      if (ctrl.moveupDropdown) {
        $dropdownList.css('bottom', elemHeight+'px');
      } else {
        $dropdownList.css('top', elemHeight+'px');
      }
    }

    // set focus to $element
    // if $element lose focus then dropdown will be closed
    $scope.$watch(() => {return ctrl.showDropdown;}, (doShow) => {
      if (doShow){
        $element.focus();
      }
    });
    $element.bind('blur', () => {
      ctrl.closeDropdown();
    })

  }
}
