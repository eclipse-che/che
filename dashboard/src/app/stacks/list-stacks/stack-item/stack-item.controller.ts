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
 * @name stacks.list.controller:StackItemController
 * @description This class is handling the controller for item of stack list
 * @author Ann Shumilova
 */
export class StackItemController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($location, lodash) {
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
  getComponents(stack) {
    return this.lodash.map(stack.components, (component) => {
      return component.name;
    }).join(', ');
  }
}
