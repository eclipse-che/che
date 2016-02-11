/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
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
 * This class is handling the controller for the simple selecter (only allowing to select the widget)
 * @author Florent Benoit
 */
export class CheSimpleSelecterCtrl {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($scope) {
    this.$scope = $scope;
  }


  /**
   * perform sharing state in an upper scope as it may be shared
   */
  select(globalSelecterName, name, value) {
    this.globalSelecterName = globalSelecterName;
    this.$scope.$parent.$parent[globalSelecterName + '.selecterSelected'] = name;
    this.callbackController.cheSimpleSelecter(name, value);
  }

  /**
   * Gets the selected widget among all widgets of this name
   * @returns {*}
   */
  getSelected() {
    var globalSelecterName = this.$scope.selectName;
    return this.$scope.$parent.$parent[globalSelecterName + '.selecterSelected'];
  }


  /**
   * when initializing with the first item, send this item to the callback controller
   */
  initValue() {
    if (this.isFirst) {
      this.callbackController.cheSimpleSelecterDefault(this.value);
    }
  }

}
