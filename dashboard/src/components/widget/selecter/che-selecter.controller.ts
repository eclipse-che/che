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
import {ICheSelecterScope} from './che-selecter.directive';

/**
 * This class is handling the controller for the selecter
 * @author Florent Benoit
 */
export class CheSelecterCtrl {

  static $inject = ['$scope'];

  $scope: ICheSelecterScope;

  globalSelecterName: string;
  selectedValuesByKey: Map<string, any>;

  /**
   * Default constructor that is using resource
   */
  constructor($scope: ICheSelecterScope) {
    this.$scope = $scope;
    this.globalSelecterName = 'unknown';
    this.selectedValuesByKey = new Map();
  }

  /**
   * perform sharing state in an upper scope as it may be shared
   */
  select(globalSelecterName: string, name: string): void {
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
   * @param {string} key
   * @param values
   */
  initType(key: string, values: any): void {
    // set with first value
    this.selectedValuesByKey.set(key, values[0].id);
  }

  /**
   * Event when select operation is called
   * @param {string} key
   */
  onChangeType(key: string): void {

    // look at the model and define the value
    if (this.$scope.valueModel) {
      // update the selected value
      this.selectedValuesByKey.set(key, this.$scope.valueModel);

      // notify callbacks
      this.$scope.callbackController.cheSelecter(key, this.selectedValuesByKey.get(key));
    }

  }

}
