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

/**
 * @ngdoc directive
 * @name navbar.selected.controller:NavBarSelected
 * @description This class is controller of NavBarSelected
 * @author Florent Benoit
 */
export class NavBarSelectedCtrl {

  static $inject = ['$mdSidenav'];

  $mdSidenav: ng.material.ISidenavService;

  /**
   * Default constructor that is using resource
   */
  constructor ($mdSidenav: ng.material.ISidenavService) {
    this.$mdSidenav = $mdSidenav;
  }

  /**
   * Close left navbar
   */
  close() {
    this.$mdSidenav('left').close();
  }


}
