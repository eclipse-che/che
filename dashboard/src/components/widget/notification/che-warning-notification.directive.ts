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

interface ICheWarningNotificationAttributes extends ng.IAttributes {
  cheWarningText: string;
}

interface ICheWarningNotificationScope extends ng.IScope {
  hideNotification: () => void;
}

/**
 * Defines a directive for warning notification.
 * @author Ann Shumilova
 */
export class CheWarningNotification implements ng.IDirective {

  restrict = 'E';
  replace = true;

  scope = {};

  /**
   * Template for the warning notification.
   * @param $element
   * @param $attrs
   * @returns {string} the template
   */
  template($element: ng.IAugmentedJQuery, $attrs: ICheWarningNotificationAttributes): string {
    let warningText = $attrs.cheWarningText || '';
    return '<md-toast  class="che-notification-warning" layout="row" layout-align="start start">' +
      '<i class="che-notification-warning-icon fa fa-exclamation-triangle fa-2x"></i>' +
      '<div flex="90" layout="column" layout-align="start start">' +
      '<span flex class="che-notification-warning-title"><b>Warning</b></span>' +
      '<span flex class="che-notification-message">' + warningText + '</span>' +
      '</div>' +
      '<i class="che-notification-close-icon fa fa-times" ng-click="hideNotification()"/>' +
      '</md-toast>';
  }

  link($scope: ICheWarningNotificationScope, $element: ng.IAugmentedJQuery): void {
    $scope.hideNotification = () => {
      $element.addClass('hide-notification');
    };
  }
}
