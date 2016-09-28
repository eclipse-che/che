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
 * This class is handling all application notifications.
 *
 * @author Ann Shumilova
 */
export class ApplicationNotifications {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($http) {
    this.$http = $http;

    this.notifications = [];
  }

  /**
   * Returns the list of application notification.
   * Notification has title, content and type (info, error, warning).
   *
   * @returns {*} the list of application notifications
   */
  getNotifications() {
    return this.notifications;
  }

  /**
   * Add new notification.
   *
   * @param notification notification yo be added
   */
  addNotification(notification) {
    this.notifications.push(notification);
  }

  /**
   * Add error notification.
   *
   * @param title notification's title
   * @param content notification's content
   * @returns {{notification}} notification
   */
  addErrorNotification(title, content) {
    return this._addNotification('error', title, content);
  }

  /**
   * Add warning notification.
   *
   * @param title notification's title
   * @param content notification's content
   * @returns {{notification}} notification
   */
  addWarningNotification(title, content) {
    return this._addNotification('warning', title, content);
  }

  /**
   * Add information notification.
   *
   * @param title notification's title
   * @param content notification's content
   * @returns {{notification}} notification
   */
  addInfoNotification(title, content) {
    return this._addNotification('info', title, content);
  }

  /**
   * Add notification with pointed type.
   *
   * @param type notification type (error, info, warning)
   * @param title notification title
   * @param content notification content
   * @returns {{}}
   * @private
   */
  _addNotification(type, title, content) {
    let notification = {};
    notification.title = title;
    notification.content = content;
    notification.type = type;
    this.notifications.push(notification);
    return notification;
  }

  /**
   * Remove the pointed notification
   *
   * @param notification notification to be removed
   */
  removeNotification(notification) {
    let index = this.notifications.indexOf(notification);
    if (index >= 0) {
      this.notifications.splice(index, 1);
    }
  }
}
