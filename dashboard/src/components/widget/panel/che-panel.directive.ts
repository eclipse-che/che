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
import {ChePanelCtrl} from './che-panel.controller';

interface IChePanelAttributes extends ng.IAttributes {
  cheLockMode: boolean;
  cheCollapse: boolean;
  chePanelId: string;
  cheDisabled: boolean;
  cheToggle: boolean;
  cheTitleIcon: string;
  cheTitleSvgIcon: string;
  cheTooltip: string;
}

/**
 * @ngdoc directive
 * @name components.directive:chePanel
 * @restrict E
 * @function
 * @element
 *
 * @description
 * `<che-panel>` defines a panel used to insert data.
 *
 * @param {string=} che-title the title of the panel
 * @param {string=} che-title-icon icon font prefixing the panel's title
 * @param {string=} che-title-svg-icon path to SVG image used as panel's title
 * @param {boolean=} che-toggle boolean used to display or not the panel toggle
 * @param {boolean=} che-disabled boolean used to add a glass panel over the panel
 *
 * @usage
 *   <che-panel che-title="hello"></che-panel>
 *
 * @example
 * <example module="cheDashboard">
 *   <file name="index.html">
 *     <che-panel che-title-icon="fa fa-lock" che-title="hello">This is simple text</che-panel>
 *   </file>
 * </example>
 * @author Florent Benoit
 */
export class ChePanel {

  restrict = 'E';
  replace = true;
  transclude = true;
  bindToController = true;

  controller = 'ChePanelCtrl';
  controllerAs = 'chePanelCtrl';

  scope = {
    svgIcon: '@cheTitleSvgIcon',
    tooltip: '@?cheTooltip',
    title: '@cheTitle',
    disabled: '@cheDisabled'
  };

  /**
   * Defines id of the controller and apply some initial settings
   */
  link($scope: ng.IScope, $element: ng.IAugmentedJQuery, $attributes: IChePanelAttributes, controller: ChePanelCtrl): void {

    // special mode
    if ($attributes.cheLockMode) {
      controller.lock();
    }

    // special mode
    if ($attributes.cheCollapse) {
      controller.collapse = true;
    }

    // set id
    if ($attributes.chePanelId) {
      controller.setId($attributes.chePanelId);
    }

    // disabled
    if ($attributes.cheDisabled) {
      controller.disabled = true;
    }
  }

  /**
   * Template for the current toolbar
   * @param $element
   * @param $attrs
   * @returns {string} the template
   */
  template($element: ng.IAugmentedJQuery, $attrs: IChePanelAttributes): string {

    var template = '<md-card class="che-panel" md-theme="default">'
      + '<div layout="row" class="che-panel-titlebox" layout-align="start center">'
      + '<div class="che-panel-title" layout="row" layout-align="start center"'
      + ($attrs.cheToggle ? ' ng-click="chePanelCtrl.toggle()">' : '>');

    if ($attrs.cheTitleIcon) {
      template = template + '<span class="che-panel-title-icon ' + $attrs.cheTitleIcon + '"></span>';
    }
    if ($attrs.cheTitleSvgIcon) {
      template = template + '<md-icon md-svg-src="' + '{{chePanelCtrl.svgIcon}}' + '"></md-icon>';
    }

    template = template + '{{chePanelCtrl.title}}</div>';
    if ($attrs.cheTooltip) {
      template = template + '<div><i class="fa fa-info-circle che-panel-title-tooltip-icon" tooltip-placement="right" uib-tooltip="{{chePanelCtrl.tooltip}}"></i></div>';
    }

    template = template + '<div flex layout="column"><div class="che-panel-title-top">&nbsp;</div><div class="che-panel-title-bottom">&nbsp;</div></div>';


    if ($attrs.cheToggle) {
      template = template + '<i class="{{chePanelCtrl.getToggleIcon()}}" ng-click="chePanelCtrl.toggle()"></i>';
    }

    template = template
    + '</div>'
    + '<md-card-content class="che-panel-content" ng-hide="chePanelCtrl.isCollapsed()">'
    + '<ng-transclude></ng-transclude>'
    + '</md-card-content>'
    + '<div class="che-panel-glass" ng-show="chePanelCtrl.disabled"></div>'
    + '</md-card>';

    return template;
  }

}
