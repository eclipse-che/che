/*
 * Copyright (c) 2015-2017 Red Hat, Inc.
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
 * This class is handling the controller for the dropdown
 * @author Florent Benoit
 */
export class CheButtonDropdownCtrl {
  $timeout: ng.ITimeoutService;
  $window: ng.IWindowService;

  showDropdown: boolean;
  isDisabled: boolean;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($timeout: ng.ITimeoutService, $window: ng.IWindowService) {
    this.$timeout = $timeout;
    this.showDropdown = false;
    this.$window = $window;
  }

  toggleDropDown(): void {
    if (this.isDisabled) {
      this.showDropdown = false;
      return;
    }

    this.showDropdown = !this.showDropdown;
  }

  disableDropDown(): void {
    this.$timeout(() => {
      this.showDropdown = false;
    }, 300);
  }

  redirect(newPath: string): void {
    if (!newPath || this.isDisabled) {
      return;
    }
    this.$window.location.href = newPath;
    this.disableDropDown();
  }

}


