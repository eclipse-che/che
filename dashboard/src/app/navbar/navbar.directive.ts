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
