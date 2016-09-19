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
 * This class is handling the controller for the notifications container.
 * @author Ann Shumilova
 */
export class NavbarNotificationController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(applicationNotifications) {
    this.applicationNotifications = applicationNotifications;
  }

  getNotificationsCount() {
    return this.applicationNotifications.getNotifications().length;
  }

  getNotifications() {
    return this.applicationNotifications.getNotifications();
  }
}


