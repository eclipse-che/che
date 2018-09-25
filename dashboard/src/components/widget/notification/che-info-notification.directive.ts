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

interface ICheInfoNotificationAttributes extends ng.IAttributes {
  cheInfoText: string;
}

interface ICheInfoNotificationScope extends ng.IScope {
  hideNotification: () => void;
}

/**
 * Defines a directive for info notification.
 * @author Oleksii Orel
 */
export class CheInfoNotification implements ng.IDirective {

  restrict = 'E';
  replace = true;

  scope = {};

  /**
   * Template for the info notification.
   * @param $element
   * @param $attrs
   * @returns {string} the template
   */
  template($element: ng.IAugmentedJQuery, $attrs: ICheInfoNotificationAttributes): string {
    let infoText = $attrs.cheInfoText || '';
    return '<md-toast class="che-notification-info" layout="row" flex layout-align="start start">' +
      '<i class="che-notification-info-icon fa fa-check fa-2x"></i>' +
      '<div flex="90" layout="column" layout-align="start start">' +
      '<span flex class="che-notification-info-title"><b>Success</b></span>' +
      '<span flex class="che-notification-message">' + infoText + '</span>' +
      '</div>' +
      '<i class="che-notification-close-icon fa fa-times" ng-click="hideNotification()"/>' +
      '</md-toast>';
  }

  link($scope: ICheInfoNotificationScope, $element: ng.IAugmentedJQuery) {
    $scope.hideNotification = () => {
      $element.addClass('hide-notification');
    };
  }
}
