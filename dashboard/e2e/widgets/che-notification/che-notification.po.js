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

const EC = protractor.ExpectedConditions;
const asyncTimeout = 10000;

const CheInfoNotification = function () {
  this.notificationElements = $$('.che-notification-info');

  this.getNotificationElements = () => {
    return this.notificationElements;
  };

  this.getNotificationElementByText = (text) => {
    return this.notificationElements.filter((elem, i) => {
      return elem.$('.che-notification-message').getText().then((messageText) => {
        return messageText === text;
      });
    });
  };

  this.getCloseIconElement = (notificationElement) => {
    return notificationElement.$('.che-notification-close-icon');
  };

  this.waitFotNotificationElementWithText = (text) => {
    const notificationMessageElement = $('.che-notification-info .che-notification-message');
    browser.wait(EC.textToBePresentInElement(notificationMessageElement, text, asyncTimeout));
  };

};

const CheErrorNotification = function () {
  this.notificationElements = $$('.che-notification-error');

  this.getNotificationElements = () => {
    return this.notificationElements;
  };

  this.getNotificationElementByText = (text) => {
    return this.notificationElements.filter((elem, i) => {
      return elem.$('.che-notification-message').getText().then((messageText) => {
        return messageText === text;
      });
    });
  };

  this.getCloseIconElement = (notificationElement) => {
    return notificationElement.$('.che-notification-close-icon');
  };

  this.waitFotNotificationElementWithText = (text) => {
    const notificationMessageElement = $('.che-notification-error .che-notification-message');
    browser.wait(EC.textToBePresentInElement(notificationMessageElement, text, asyncTimeout));
  };

};

module.exports.info = new CheInfoNotification();
module.exports.error = new CheErrorNotification();
