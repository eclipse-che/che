/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
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
 * Defines a directive for info notification.
 * @author Oleksii Orel
 */
export class CheInfoNotification {

    /**
     * Default constructor.
     */
    constructor() {
        this.restrict = 'E';
        this.replace = true;

        this.scope = {};
    }

    /**
     * Template for the info notification.
     * @param element
     * @param attrs
     * @returns {string} the template
     */
    template(element, attrs) {
        let infoText = attrs['cheInfoText'] || '';
        return '<md-toast class="che-notification-info" layout="row" flex layout-align="start start">' +
            '<i class="che-notification-info-icon fa fa-check fa-2x"></i>' +
            '<div flex="90" layout="column" layout-align="start start">' +
            '<span flex class="che-notification-info-title"><b>Success</b></span>' +
            '<span flex class="che-notification-message">' + infoText + '</span>' +
            '</div>' +
            '<i class="che-notification-close-icon fa fa-times" ng-click="hideNotification()"/>' +
            '</md-toast>';
    }

    link($scope, element) {
        $scope.hideNotification = ()=> {
            element.addClass('hide-notification');
        };
    }
}
