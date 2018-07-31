/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

/**
 * @ngDoc directive
 * @name navbar.directive:NavbarDropdownMenu
 * @description This class is handling the directive to handle the container with notifications
 * @author Ann Shumilova
 */
export class NavbarNotification implements ng.IDirective {

  restrict = 'E';
  bindToController = true;
  templateUrl = 'app/navbar/notification/navbar-notification.html';
  controller = 'NavbarNotificationController';
  controllerAs = 'navbarNotificationController';

  transclude = true;

}
