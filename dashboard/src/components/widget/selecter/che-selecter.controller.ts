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
 * This class is handling the controller for the selecter
 * @author Florent Benoit
 */
export class CheSelecterCtrl {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($scope) {
    this.$scope = $scope;
    this.globalSelecterName = 'unknown';
    this.selectedValuesByKey = new Map();
  }


  /**
   * perform sharing state in an upper scope as it may be shared
   */
  select(globalSelecterName, name) {
    this.globalSelecterName = globalSelecterName;
    this.$scope.$parent.$parent[globalSelecterName + '.selecterSelected'] = name;

    this.$scope.valueModel = this.selectedValuesByKey.get(name);
    this.$scope.callbackController.cheSelecter(name, this.selectedValuesByKey.get(name));
  }

  /**
   * Gets the selected widget among all widgets of this name
   * @returns {*}
   */
  getSelected() {
    return this.$scope.$parent.$parent[this.globalSelecterName + '.selecterSelected'];
  }


  /**
   * Sets the default type for the given category
   * @param category the category to use
   * @param types the available types
   */
  initType(key, values) {
    // set with first value
    this.selectedValuesByKey.set(key, values[0].id);

  }

  /**
   * Event when select operation is called
   * @param category the key of t
   * @param values
   */
  onChangeType(key) {

    // look at the model and define the value
    if (this.$scope.valueModel) {
      // update the selected value
      this.selectedValuesByKey.set(key, this.$scope.valueModel);

      // notify callbacks
      this.$scope.callbackController.cheSelecter(key, this.selectedValuesByKey.get(key));
    }

  }

}
