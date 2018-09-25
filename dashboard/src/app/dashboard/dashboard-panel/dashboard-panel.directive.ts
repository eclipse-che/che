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
 * @ngdoc directive
 * @name dashboard.directive:DashboardPanel
 * @description This class is handling the directive of the panel for displaying dashboard entries.
 * @author Oleksii Kurinnyi
 */
export class DashboardPanel implements ng.IDirective {

  restrict = 'E';
  transclude = true;
  replace = true;

  /**
   * Template for the panel
   * @param $element
   * @param $attrs
   * @returns {string} the template
   */
  template($element: ng.IAugmentedJQuery, $attrs: ng.IAttributes): string {
    return ''
      + '<div class="dashboard-panel">'
        + '<div class="dashboard-panel-title" layout="row" layout-align="start center">'
          + '<span>' + ($attrs as any).panelTitle + '</span>'
        + '</div>'
        + '<div class="dashboard-panel-content">'
          + '<ng-transclude></ng-transclude>'
        + '</div>'
      + '</div>';
  }

}
