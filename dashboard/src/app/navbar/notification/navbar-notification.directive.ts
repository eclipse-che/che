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
