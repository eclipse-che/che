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
 * @ngdoc controller
 * @name stacks.list.controller:StackItemController
 * @description This class is handling the controller for item of stack list
 * @author Ann Shumilova
 */
export class StackItemController {

  static $inject = ['$location', 'lodash'];

  $location: ng.ILocationService;
  lodash: any;

  stack: che.IStack;

  /**
   * Default constructor that is using resource
   */
  constructor($location: ng.ILocationService,
              lodash: any) {
    this.$location = $location;
    this.lodash = lodash;
  }

  /**
   * Redirects to stack details.
   */
  redirectToStackDetails() {
    this.$location.path('/stack/' + this.stack.id);
  }

  /**
   * Get string of stack's components.
   * @param stack stack with components
   * @returns {*}
   */
  getComponents(stack: che.IStack) {
    return this.lodash.map(stack.components, (component: any) => {
      return component.name;
    }).join(', ');
  }
}
