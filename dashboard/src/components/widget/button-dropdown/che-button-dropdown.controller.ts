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
 * @author Florent Benoit
 */
export class CheButtonDropdownCtrl {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($timeout, $window) {
    this.$timeout = $timeout;
    this.showDropdown = false;
    this.$window = $window;
  }

  toggleDropDown() {
    if (this.isDisabled) {
      this.showDropdown = false;
      return;
    }

    this.showDropdown = !this.showDropdown;
  }

  disableDropDown() {
    this.$timeout(() => {
      this.showDropdown = false;
    }, 300);
  }

  redirect(newPath) {
    if (!newPath || this.isDisabled) {
      return;
    }
    this.$window.location.href = newPath;
    this.disableDropDown();
  }

}


