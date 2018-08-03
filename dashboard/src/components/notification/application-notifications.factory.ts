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
 * This class is handling all application notifications.
 *
 * @author Ann Shumilova
 */
export class ApplicationNotifications {
  private notifications: Array<any>;

  /**
   * Default constructor that is using resource
   */
  constructor () {
    this.notifications = [];
  }

  /**
   * Returns the list of application notification.
   * Notification has title, content and type (info, error, warning).
   *
   * @returns {*} the list of application notifications
   */
  getNotifications(): Array<any> {
    return this.notifications;
  }

  /**
   * Add new notification.
   *
   * @param notification notification yo be added
   */
  addNotification(notification: any): void {
    this.notifications.push(notification);
  }

  /**
   * Add error notification.
   *
   * @param title notification's title
   * @param content notification's content
   * @param removeOnRead if <code>true</code> - should be removed after has been shown to user
   * @returns {{notification}} notification
   */
  addErrorNotification(title: string, content: string, removeOnRead: boolean = true): any {
    return this._addNotification('error', title, content, removeOnRead);
  }

  /**
   * Add warning notification.
   *
   * @param title notification's title
   * @param content notification's content
   * @param removeOnRead if <code>true</code> - should be removed after has been shown to user
   * @returns {{notification}} notification
   */
  addWarningNotification(title: string, content: string, removeOnRead: boolean = true): any {
    return this._addNotification('warning', title, content, removeOnRead);
  }

  /**
   * Add information notification.
   *
   * @param title notification's title
   * @param content notification's content
   * @param removeOnRead if <code>true</code> - should be removed after has been shown to user
   * @returns {{notification}} notification
   */
  addInfoNotification(title: string, content: string, removeOnRead: boolean = true): any {
    return this._addNotification('info', title, content, removeOnRead);
  }

  /**
   * Add notification with pointed type.
   *
   * @param type notification type (error, info, warning)
   * @param title notification title
   * @param content notification content
   * @param removeOnRead if <code>true</code> - should be removed after has been shown to user
   * @returns {{}} added notification
   * @private
   */
  _addNotification(type: string, title: string, content: string, removeOnRead: boolean): any {
    let notification = {
      title: title,
      content: content,
      type: type,
      removeOnRead: removeOnRead
    };
    this.notifications.push(notification);
    return notification;
  }

  /**
   * Remove the pointed notification
   *
   * @param notification notification to be removed
   */
  removeNotification(notification: any): void {
    let index = this.notifications.indexOf(notification);
    if (index >= 0) {
      this.notifications.splice(index, 1);
    }
  }
}
