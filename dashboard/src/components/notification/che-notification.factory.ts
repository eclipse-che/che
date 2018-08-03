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
import {CheUIElementsInjectorService} from '../service/injector/che-ui-elements-injector.service';

const NOTIFICATION_CONTAINER_ELEMENT_ID = 'che-notification-container';
const ERROR_NOTIFICATION_DISPLAY_TIME = 10000;
const INFO_NOTIFICATION_DISPLAY_TIME = 5000;
const MAX_NOTIFICATION_COUNT = 10;

/**
 * Provides custom notifications
 * @author Oleksii Orel
 */
export class CheNotification {

  static $inject = ['$timeout', '$document', 'cheUIElementsInjectorService'];

  private $timeout: ng.ITimeoutService;
  private $document: ng.IDocumentService;
  private cheUIElementsInjectorService: CheUIElementsInjectorService;

  private currentNotificationNumber: number;
  private timeoutPromiseMap: Map<string, ng.IPromise<any>>;

  /**
   * Default constructor that is using resource injection
   */
  constructor($timeout: ng.ITimeoutService, $document: ng.IDocumentService, cheUIElementsInjectorService: CheUIElementsInjectorService) {
    this.$timeout = $timeout;
    this.cheUIElementsInjectorService = cheUIElementsInjectorService;
    this.$document = $document;

    this.timeoutPromiseMap = new Map();
  }

  _getNextNotificationId(): string {
    this._removeHiddenNotification();

    const notificationCount = this.timeoutPromiseMap.size;
    if (notificationCount === 0) {
      // initialise counter of notifications
      this.currentNotificationNumber = 0;
    } else if (notificationCount >= MAX_NOTIFICATION_COUNT) {
      this._removeNotification(this._getNotificationContainer().children().first());
    }

    this.currentNotificationNumber++;
    if (this.currentNotificationNumber > 10000) {
      this.currentNotificationNumber = 1;
    }

    return 'che-notification-' + this.currentNotificationNumber;
  }

  _getNotificationContainer(): ng.IAugmentedJQuery {
    const notificationContainerElement = this.$document.find(`#${NOTIFICATION_CONTAINER_ELEMENT_ID}`);
    if (notificationContainerElement[0]) {
      return notificationContainerElement;
    }

    const jqAdditionalElement = angular.element(`<div></div>`);
    jqAdditionalElement.attr('id', NOTIFICATION_CONTAINER_ELEMENT_ID);
    this.cheUIElementsInjectorService.injectAdditionalElement(this.$document.find('body'), jqAdditionalElement);

    return jqAdditionalElement;
  }

  _removeNotificationContainer(): boolean {
    if (this.timeoutPromiseMap.size !== 0) {
      return false;
    }
    return this.cheUIElementsInjectorService.deleteElementById(NOTIFICATION_CONTAINER_ELEMENT_ID);
  }

  _addNotification(jqNotificationElement: ng.IRootElementService): void {
    if (!jqNotificationElement[0] || !jqNotificationElement[0].id) {
      return;
    }

    let oldNotificationElement = this.$document.find(`#${jqNotificationElement[0].id}`);
    if (oldNotificationElement[0]) {
      const jqOldNotificationElement = angular.element(oldNotificationElement);
      jqOldNotificationElement.addClass('hide-notification');
      this.$timeout(() => {
        this.cheUIElementsInjectorService.injectAdditionalElement(this._getNotificationContainer(), jqNotificationElement);
      }, 300);
    } else {
      this.cheUIElementsInjectorService.injectAdditionalElement(this._getNotificationContainer(), jqNotificationElement);
    }
  }

  _removeNotification(jqNotificationElement: JQuery): void {
    const elementId = jqNotificationElement[0].id;

    jqNotificationElement.addClass('hide-notification');

    const timeoutPromise = this.timeoutPromiseMap.get(elementId);
    if (timeoutPromise) {
      this.$timeout.cancel(timeoutPromise);
    }
    this.timeoutPromiseMap.delete(elementId);

    this.$timeout(() => {
      jqNotificationElement.remove();
      this._removeNotificationContainer();
    }, 300);
  }

  _removeHiddenNotification(): void {
    const jqNotificationContainerElement = this._getNotificationContainer();
    const hideNotificationElements = jqNotificationContainerElement[0].getElementsByClassName('hide-notification');

    if (hideNotificationElements.length === 0) {
      return;
    }
    for (let pos = 0; pos < hideNotificationElements.length; pos++) {
      let hideNotificationElement = hideNotificationElements[pos];
      if (!hideNotificationElement) {
        continue;
      }
      let elementId = hideNotificationElement.id;
      let timeoutPromise = this.timeoutPromiseMap.get(elementId);
      if (timeoutPromise) {
        this.$timeout.cancel(timeoutPromise);
        this.timeoutPromiseMap.delete(elementId);
      }
      hideNotificationElement.remove();
    }
    this._removeNotificationContainer();
  }

  showInfo(text: string): void {
    const notificationId = this._getNextNotificationId();
    const jqInfoNotificationElement = angular.element('<che-info-notification/>');

    jqInfoNotificationElement.attr('che-info-text', text);
    jqInfoNotificationElement.attr('id', notificationId);

    this._addNotification(jqInfoNotificationElement);

    const timeoutPromise = this.$timeout(() => {
      this._removeNotification(jqInfoNotificationElement);
    }, INFO_NOTIFICATION_DISPLAY_TIME);
    this.timeoutPromiseMap.set(notificationId, timeoutPromise);
  }

  showError(text: string, error?: {data?: { message?: string}}): void {
    let errorMessage: string;
    if (error && error.data && angular.isString(error.data.message) && error.data.message.length > 0) {
      errorMessage = error.data.message;
    } else {
      errorMessage = text;
    }
    const notificationId = this._getNextNotificationId();
    const jqErrorNotificationElement = angular.element('<che-error-notification/>');

    jqErrorNotificationElement.attr('che-error-text', errorMessage);
    jqErrorNotificationElement.attr('id', notificationId);

    this._addNotification(jqErrorNotificationElement);

    const timeoutPromise = this.$timeout(() => {
      this._removeNotification(jqErrorNotificationElement);
    }, ERROR_NOTIFICATION_DISPLAY_TIME);
    this.timeoutPromiseMap.set(notificationId, timeoutPromise);
  }
}
