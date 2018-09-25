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
import {ChePreferences} from '../../api/che-preferences.factory';
import {ICheLearmMoreAttributes} from './che-learn-more.directive';

/**
 * @ngdoc controller
 * @name components.controller:CheLearnMoreCtrl
 * @description This class is handling the controller of learnmore widget
 * @author Florent Benoit
 */
export class CheLearnMoreCtrl {

  static $inject = ['$scope', '$element', '$attrs', '$compile', 'chePreferences'];

  $scope: ng.IScope;
  $element: ng.IAugmentedJQuery;
  $attrs: ICheLearmMoreAttributes;
  $compile: ng.ICompileService;
  chePreferences: ChePreferences;

  items: any[];
  WIDGET_PREFERENCES_PREFIX: string;
  currentIndex: number;
  stateIcons: any[];

  /**
   * Default constructor that is using resource
   */
  constructor($scope: ng.IScope, $element: ng.IAugmentedJQuery, $attrs: ICheLearmMoreAttributes, $compile: ng.ICompileService,
     chePreferences: ChePreferences) {
    this.items = [];

    this.WIDGET_PREFERENCES_PREFIX = 'learn-widget-';

    this.chePreferences = chePreferences;

    // current index is first one
    this.currentIndex = 0;

    let preferences = this.chePreferences.getPreferences();
    let promise = preferences.$promise;

    promise.then(() => {
      let selectedIndexInPreferences = preferences[this.WIDGET_PREFERENCES_PREFIX + 'selected-index'];
      if (selectedIndexInPreferences) {
        this.currentIndex = parseInt(selectedIndexInPreferences, 10);
      }
    });

    // by default, all icons are disabled
    this.stateIcons = [];

    this.$scope = $scope;
    this.$element = $element;
    this.$attrs = $attrs;
    this.$compile = $compile;

    // listener on events
    this.$scope.$on('cheLearnMore:updateState', (event: ng.IAngularEvent, data: any) => {
      this.updateState(data);
    });

    this.compileTemplate();
  }

  /**
   * Compile the template and wrap it
   */
  compileTemplate(): void {
    const template = this.$attrs.$cheLearnMoreTemplate;

    const data  = this.$element[ 0 ].getElementsByTagName('che-learn-more-data')[ 0 ];
    const element  = angular.element(data);
    element.html(template);
    this.$compile(element.contents())(this.$scope.$parent);

    // delete it from attributes
    delete this.$attrs.$cheLearnMoreTemplate;
  }

  updateState(data: any): void {

    // check key of data
    let key = data.key;
    let value = data.value;

    // built-in key
    let checkKey = this.WIDGET_PREFERENCES_PREFIX + key;
    const properties = {};
    properties[checkKey] = value;
    this.chePreferences.updatePreferences(properties);

    // also update icon state
    this.stateIcons[key] = value;
  }

  insertItem(item: any): void {

    // check if item has been done or not (if there is a key)
    let key = item.key;
    // default is false
    this.stateIcons[key] = false;

    // check if key is stored in preferences
    // if there,
    if (key) {
      let preferences = this.chePreferences.getPreferences();
      let promise = preferences.$promise;

      promise.then(() => {

        // built-in key
        let checkKey = this.WIDGET_PREFERENCES_PREFIX + key;
        let value = preferences[checkKey];
        if (value && value === 'true') {
          this.stateIcons[key] = true;
        }

      });
    }

    // add current item
    this.items.push(item);

  }

  isSelectedItem(index: number): boolean {
    return index === this.currentIndex;
  }

  isItemCompleted(key: string): void {
    let val = this.stateIcons[key];
    return val;
  }

  setCurrentIndex(currentIndex: number): void {
    this.currentIndex = currentIndex;

    // update preferences
    let data =   {
      key: 'selected-index',
      value: currentIndex
    };
    this.updateState(data);

  }

}
