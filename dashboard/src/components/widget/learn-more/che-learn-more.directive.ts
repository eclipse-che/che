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

export interface ICheLearmMoreAttributes extends ng.IAttributes {
  $cheLearnMoreTemplate: any;
}

/**
 * @ngdoc directive
 * @name components.directive:cheLearnMore
 * @restrict E
 * @function
 * @element
 *
 * @description
 * `<che-learn-more>` defines a learn more component.
 *
 * @author Florent Benoit
 */
export class CheLearnMore implements ng.IDirective {

  restrict = 'E';
  bindToController = true;

  controller = 'CheLearnMoreCtrl';
  controllerAs = 'cheLearnMoreCtrl';

  scope = {
    title: '@cheTitle'
  };

  /**
   * Template for the current toolbar
   * @param $element
   * @param $attrs
   * @returns {string} the template
   */
  template($element: ng.IAugmentedJQuery, $attrs: ICheLearmMoreAttributes) {

    // keep the current value into the attributes
    $attrs.$cheLearnMoreTemplate = $element.html();

    var template = '<md-card class="che-learn-more-panel" md-theme="default">'
      + '<div layout="row" class="che-learn-more-titlebox" layout-align="start center">'
      + '<div class="che-learn-more-title" layout="row" layout-align="start center">' + '{{cheLearnMoreCtrl.title}}</div>'
      + '<span flex></span></div>'
      + '<md-card-content><che-learn-more-wrapper layout-sm="column" layout-md="column" layout-gt-md="row">'
      + '<che-learn-more-data ng-hide="true"></che-learn-more-data>'
      + '<che-learn-more-titles layout="column">'
      + '<che-learn-more-title-container ng-class="cheLearnMoreCtrl.isSelectedItem({{item.index}}) ? '
      + '\'che-learn-more-title-container-selected\' : \'che-learn-more-title-container-unselected\'"'
      + ' ng-repeat="item in cheLearnMoreCtrl.items"><che-learn-more-title ng-click="cheLearnMoreCtrl.setCurrentIndex(item.index)">'
      + '<md-icon md-svg-src="assets/images/completed.svg" class="che-learn-more-item-completeted-box" ng-show="cheLearnMoreCtrl.isItemCompleted(item.key)"></md-icon>'
      + '<md-icon md-svg-src="assets/images/to-complete.svg" class="che-learn-more-item-to-complete-box" ng-hide="cheLearnMoreCtrl.isItemCompleted(item.key)">'
      + '</md-icon>{{item.title}}</che-learn-more-title></<che-learn-more-title-container>'
      + '</che-learn-more-titles>'
      + '<che-learn-more-contents flex><che-learn-more-content ng-show="cheLearnMoreCtrl.isSelectedItem({{item.index}})" ng-repeat="item in cheLearnMoreCtrl.items">'
      + '<div layout="column" layout-align="center center" che-learn-more-template="::item.content" che-scope="::item.parent" ></div></che-learn-more-content></che-learn-more-contents>'
      + '</che-learn-more-wrapper></md-card-content></md-card>';
    return template;
  }

}
