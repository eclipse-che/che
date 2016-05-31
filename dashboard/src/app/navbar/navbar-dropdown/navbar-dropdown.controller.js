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
 * This class is handling the controller for the dropdown
 * @author Oleksii Kurinnyi
 */
export class NavbarDropdownCtrl {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($timeout, $window, $scope) {
    this.$timeout = $timeout;
    this.showDropdown = false;
    this.$window = $window;
    this.$scope = $scope;

    this.moveupDropdown = !angular.isUndefined(this.moveDropdownAbove);
  }

  toggleDropdown() {
    if (this.isDisabled) {
      this.showDropdown = false;
      return;
    }

    this.showDropdown = !this.showDropdown;
  }

  closeDropdown() {
    this.$timeout(() => {
      this.showDropdown = false;
    }, 100);
  }

  process(item) {
    if (item.url) {
      this.redirect(item.url);
      return;
    }

    if (item.onclick) {
      item.onclick();
    }
  }

  redirect(newPath) {
    if (!newPath || this.isDisabled) {
      return;
    }
    this.$window.location.href = newPath;
  }

  getVisibility() {
    return this.showDropdown;
  }

}


