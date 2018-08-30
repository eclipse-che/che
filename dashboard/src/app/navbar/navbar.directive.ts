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
 * Defines a directive for creating navbar.
 * @author Florent Benoit
 */
export class CheNavBar implements ng.IDirective {
  replace: boolean;
  restrict: string;
  templateUrl: string;
  controller: string;
  controllerAs: string;

  /**
   * Default constructor that is using resource
   */
  constructor () {
    this.restrict = 'E';
    this.replace = false;
    this.templateUrl = 'app/navbar/navbar.html';
    this.controller = 'CheNavBarController';
    this.controllerAs = 'navbarController';
  }

}
