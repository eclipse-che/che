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

        this.mainContentElementId = 'main-content';
        this.notificationContainerElementId = 'che-notification-container';

        this.maxNotificationCount = 10;
        //time in milliseconds
        this.infoNotificationDisplayTime = 3000;
        this.errorNotificationDisplayTime = 20000;

        //initialise counter of notifications
        this.currentNotificationNumber = 0;
    }

    _getNextNotificationId() {
        this.currentNotificationNumber++;

        if (this.currentNotificationNumber > this.maxNotificationCount) {
            this.currentNotificationNumber = 1;
        }

        return 'che-notification-' + this.currentNotificationNumber;
    }

    _getNotificationContainer() {
        let notificationContainerElement = this.$document[0].getElementById(this.notificationContainerElementId);

        if (notificationContainerElement) {
            return notificationContainerElement;
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

    _removeNotification(jqNotificationElement) {
        jqNotificationElement.attr('style', 'max-height:0;padding:0;opacity:0;');
        this.$timeout(() => {
            jqNotificationElement.remove();
        }, 300);
    }

    showInfo(text) {
        let notificationId = this._getNextNotificationId();
        let jqInfoNotificationElement = angular.element('<che-info-notification/>');

        jqInfoNotificationElement.attr('che-info-text', text);
        jqInfoNotificationElement.attr('id', notificationId);

        this.cheUIElementsInjectorService.injectAdditionalElement(this._getNotificationContainer(), jqInfoNotificationElement);

        this.$timeout(() => {
            this._removeNotification(jqInfoNotificationElement);
        }, this.infoNotificationDisplayTime);
    }

    showError(text) {
        let notificationId = this._getNextNotificationId();
        let jqErrorNotificationElement = angular.element('<che-error-notification/>');

        jqErrorNotificationElement.attr('che-error-text', text);
        jqErrorNotificationElement.attr('id', notificationId);

        this.cheUIElementsInjectorService.injectAdditionalElement(this._getNotificationContainer(), jqErrorNotificationElement);

        this.$timeout(() => {
            this._removeNotification(jqErrorNotificationElement);
        }, this.errorNotificationDisplayTime);
    }
}
