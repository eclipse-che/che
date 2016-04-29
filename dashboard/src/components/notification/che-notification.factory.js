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
 * Provides custom notifications
 * @author Oleksii Orel
 */
export class CheNotification {

    /**
     * Default constructor that is using resource injection
     * @ngInject for Dependency injection
     */
    constructor($timeout, $document, cheUIElementsInjectorService) {
        this.$timeout = $timeout;
        this.cheUIElementsInjectorService = cheUIElementsInjectorService;
        this.$document = $document;

        this.timeoutPromiseMap = new Map();
        this.notificationContainerElementId = 'che-notification-container';

        //alignment element id
        this.mainContentElementId = 'main-content';

        //max number of notifications on the page
        this.maxNotificationCount = 10;

        //time in milliseconds
        this.infoNotificationDisplayTime = 5000;
        this.errorNotificationDisplayTime = 10000;
    }

    _getNextNotificationId() {
        this._removeHiddenNotification();

        let notificationCount = this.timeoutPromiseMap.size;
        if (notificationCount === 0) {
            //initialise counter of notifications
            this.currentNotificationNumber = 0;
        } else if (notificationCount >= this.maxNotificationCount) {
            this._removeNotification(this._getNotificationContainer().children().first());
        }

        this.currentNotificationNumber++;
        if (this.currentNotificationNumber > 10000) {
            this.currentNotificationNumber = 1;
        }

        return 'che-notification-' + this.currentNotificationNumber;
    }

    _getNotificationContainer() {
        let notificationContainerElement = this.$document[0].getElementById(this.notificationContainerElementId);

        if (notificationContainerElement) {
            return angular.element(notificationContainerElement);
        }

        let offsetRight = 0;

        let bodyElement = this.$document[0].getElementsByTagName('body')[0];
        let parentElement = this.$document[0].getElementById(this.mainContentElementId);

        if (parentElement) {
            offsetRight = bodyElement.offsetWidth - (parentElement.offsetLeft + parentElement.offsetWidth);
        }

        let jqAdditionalElement = angular.element('<div></div>');
        // set attributes into the additional element
        jqAdditionalElement.attr('id', this.notificationContainerElementId);
        jqAdditionalElement.attr('style', 'right:' + offsetRight + 'px;');

        this.cheUIElementsInjectorService.injectAdditionalElement(bodyElement, jqAdditionalElement);

        return jqAdditionalElement;
    }

    _removeNotificationContainer() {
        if (this.timeoutPromiseMap.size !== 0) {
            return false;
        }
        return this.cheUIElementsInjectorService.deleteElementById(this.notificationContainerElementId);
    }

    _addNotification(jqNotificationElement) {
        if (!jqNotificationElement[0] || !jqNotificationElement[0].id) {
            return;
        }

        let oldNotificationElement = this.$document[0].getElementById(jqNotificationElement[0].id);
        if (oldNotificationElement) {
            let jqOldNotificationElement = angular.element(oldNotificationElement);
            jqOldNotificationElement.addClass('hide-notification');
            this.$timeout(() => {

                this.cheUIElementsInjectorService.injectAdditionalElement(this._getNotificationContainer(), jqNotificationElement);
            }, 300);
        } else {

            this.cheUIElementsInjectorService.injectAdditionalElement(this._getNotificationContainer(), jqNotificationElement);
        }
    }

    _removeNotification(jqNotificationElement) {
        let elementId = jqNotificationElement[0].id;

        jqNotificationElement.addClass('hide-notification');

        let timeoutPromise = this.timeoutPromiseMap.get(elementId);
        if (timeoutPromise) {
            this.$timeout.cancel(timeoutPromise);
        }
        this.timeoutPromiseMap.delete(elementId);

        this.$timeout(() => {
            jqNotificationElement.remove();
            this._removeNotificationContainer();
        }, 300);
    }

    _removeHiddenNotification() {
        let jqNotificationContainerElement = this._getNotificationContainer();

        let hideNotificationElements = jqNotificationContainerElement[0].getElementsByClassName('hide-notification');

        let elementsLength = hideNotificationElements.length;

        if (elementsLength === 0) {
            return;
        }

        for (let pos = 0; pos < elementsLength; pos++) {
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

    showInfo(text) {
        let notificationId = this._getNextNotificationId();
        let jqInfoNotificationElement = angular.element('<che-info-notification/>');

        jqInfoNotificationElement.attr('che-info-text', text);
        jqInfoNotificationElement.attr('id', notificationId);

        this._addNotification(jqInfoNotificationElement);

        let timeoutPromise = this.$timeout(() => {
            this._removeNotification(jqInfoNotificationElement);
        }, this.infoNotificationDisplayTime);
        this.timeoutPromiseMap.set(notificationId, timeoutPromise);
    }

    showError(text) {
        let notificationId = this._getNextNotificationId();
        let jqErrorNotificationElement = angular.element('<che-error-notification/>');

        jqErrorNotificationElement.attr('che-error-text', text);
        jqErrorNotificationElement.attr('id', notificationId);

        this._addNotification(jqErrorNotificationElement);

        let timeoutPromise = this.$timeout(() => {
            this._removeNotification(jqErrorNotificationElement);
        }, this.errorNotificationDisplayTime);
        this.timeoutPromiseMap.set(notificationId, timeoutPromise);
    }
}
