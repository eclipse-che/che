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
 * This class is handling the stacks retrieval
 * It sets to the array of stacks
 * @author Florent Benoit
 * @author Ann Shumilova
 */
export class CheStack {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($resource) {

    // keep resource
    this.$resource = $resource;

    // stacks per id
    this.stacksById = {};

    // stacks
    this.stacks = [];

    // remote call
    this.remoteStackAPI = this.$resource('/api/stack', {}, {
      getStacks: {method: 'GET', url: '/api/stack?maxItems=50', isArray: true}, //TODO 50 items is temp solution while paging is not added
      getStack: {method: 'GET', url: '/api/stack/:stackId'},
      updateStack: {method: 'PUT', url: '/api/stack/:stackId'},
      createStack: {method: 'POST', url: '/api/stack'},
      deleteStack: {method: 'DELETE', url: '/api/stack/:stackId'}
    });
  }


  /**
   * Fetch the stacks
   */
  fetchStacks() {
    let promise = this.remoteStackAPI.getStacks().$promise;
    let updatedPromise = promise.then((stacks) => {
      // reset global list
      this.stacks.length = 0;
      for (var member in this.stacksById) {
        delete this.stacksById[member];
      }

      stacks.forEach((stack) => {
        // get attributes
        var stackId = stack.id;

        // add element on the list
        this.stacksById[stackId] = stack;
        this.stacks.push(stack);
      });
    });

    return updatedPromise;
  }

  /**
   * Gets all stacks
   * @returns {Array}
   */
  getStacks() {
    return this.stacks;
  }

  /**
   * The stacks per id
   * @returns {*}
   */
  getStackById(id) {
    return this.stacksById[id];
  }

  /**
   * Creates new stack.
   * @param stack data for new stack
   * @returns {$promise|*|T.$promise}
   */
  createStack(stack) {
    return this.remoteStackAPI.createStack({}, stack).$promise;
  }

  /**
   * Fetch pointed stack.
   * @param stackId stack's id
   * @returns {$promise|*|T.$promise}
   */
  fetchStack(stackId) {
    return this.remoteStackAPI.getStack({stackId: stackId}).$promise;
  }

  /**
   * Update pointed stack.
   * @param stackId stack's id
   * @returns {$promise|*|T.$promise}
   */
  updateStack(stackId, stack) {
    return this.remoteStackAPI.updateStack({stackId: stackId}, stack).$promise;
  }

  /**
   * Delete pointed stack.
   * @param stackId stack's id
   * @returns {$promise|*|T.$promise}
   */
  deleteStack(stackId) {
    return this.remoteStackAPI.deleteStack({stackId: stackId}).$promise;
  }
}


