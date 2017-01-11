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
 * @ngdoc controller
 * @name components.controller:CheLearnMoreCtrl
 * @description This class is handling the controller of learnmore widget
 * @author Florent Benoit
 */
export class CheLearnMoreCtrl {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($scope, $element, $attrs, $compile, chePreferences) {
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
        this.currentIndex = parseInt(selectedIndexInPreferences);
      }

    });

    // by default, all icons are disabled
    this.stateIcons = [];

    this.$scope = $scope;
    this.$element = $element;
    this.$attrs = $attrs;
    this.$compile = $compile;

    // listener on events
    this.$scope.$on('cheLearnMore:updateState', (event, data) => {
      this.updateState(data);
    });

    this.compileTemplate();

  }

  /**
   *Compile the template and wrap it
   */
  compileTemplate () {
    var template = this.$attrs.$cheLearnMoreTemplate;

    var data  = this.$element[ 0 ].getElementsByTagName('che-learn-more-data')[ 0 ];
    var element  = angular.element(data);
    element.html(template);
    this.$compile(element.contents())(this.$scope.$parent);

    // delete it from attributes
    delete this.$attrs.$cheLearnMoreTemplate;
  }

  updateState(data) {

    // check key of data
    let key = data.key;
    let value = data.value;

    // built-in key
    let checkKey = this.WIDGET_PREFERENCES_PREFIX + key;
    var properties = {};
    properties[checkKey] = value;
    this.chePreferences.updatePreferences(properties);

    // also update icon state
    this.stateIcons[key] = value;
  }


  insertItem(item) {

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


  isSelectedItem(index) {
    return index === this.currentIndex;
  }


  isItemCompleted(key) {
    let val = this.stateIcons[key];
    return val;
  }


  setCurrentIndex(currentIndex) {
    this.currentIndex = currentIndex;

    // update preferences
    let data =   {
      key: 'selected-index',
      value: currentIndex
    };
    this.updateState(data);

  }

}
