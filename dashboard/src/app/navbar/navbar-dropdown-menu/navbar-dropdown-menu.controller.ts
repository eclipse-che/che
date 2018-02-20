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
 * This class is handling the controller for the dropdown menu on navbar
 * @author Oleksii Kurinnyi
 */
export class NavbarDropdownMenuController {

  static $inject = ['$window'];

  /**
   * Reference to the browser's <code>window</code> object.
   */
  $window: ng.IWindowService;

  offset: string;
  isDisabled: boolean;

  /**
   * Default constructor that is using resource
   */
  constructor($window: ng.IWindowService) {
    this.$window = $window;

    this.offset = angular.isUndefined(this.offset) ? '0 0' : this.offset;
  }

  /**
   * Method process click on dropdown-menu item. If item contains
   * url property then application will follow this URL.
   * Otherwise, onclick callback will be called.
   *
   * @param item {Object} the dropdown-menu item which was clicked on
   */
  process(item: any): void {
    if (item.url) {
      this.redirect(item.url);
      return;
    }

    if (item.onclick) {
      item.onclick();
    }
  }

  redirect(newPath: string): void {
    if (!newPath || this.isDisabled) {
      return;
    }
    this.$window.location.href = newPath;
  }
}


