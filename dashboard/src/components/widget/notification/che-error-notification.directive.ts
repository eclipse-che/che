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

interface ICheErrorNotificationAttributes extends ng.IAttributes {
  cheErrorText: string;
}

interface ICheErrorNotificationScope extends ng.IScope {
  hideNotification: () => void;
}

/**
 * Defines a directive for error notification.
 * @author Oleksii Orel
 */
export class CheErrorNotification {

  restrict = 'E';
  replace = true;

  scope = {};

  /**
   * Template for the error notification.
   * @param $element
   * @param $attrs
   * @returns {string} the template
   */
  template($element: ng.IAugmentedJQuery, $attrs: ICheErrorNotificationAttributes): string {
    let errorText = $attrs.cheErrorText || '';
    return '<md-toast  class="che-notification-error" layout="row" layout-align="start start">' +
      '<i class="che-notification-error-icon fa fa-exclamation-triangle fa-2x"></i>' +
      '<div flex="90" layout="column" layout-align="start start">' +
      '<span flex class="che-notification-error-title"><b>Failed</b></span>' +
      '<span flex class="che-notification-message">' + errorText + '</span>' +
      '</div>' +
      '<i class="che-notification-close-icon fa fa-times" ng-click="hideNotification()"/>' +
      '</md-toast>';
  }

  link($scope: ICheErrorNotificationScope, $element: ng.IAugmentedJQuery): void {
    $scope.hideNotification = () => {
      $element.addClass('hide-notification');
    };
  }
}
