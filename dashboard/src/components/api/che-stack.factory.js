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
 */
export class CheStack {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($resource, $http) {

    // keep resource
    this.$resource = $resource;

    // FIXME : remove when server side will implement stack API
    this.$http = $http;

    // stacks per id
    this.stacksById = {};

    // stacks
    this.stacks = [];

    // remote call
    this.remoteStackAPI = this.$resource('/api/stack',{}, {
      getStacks: {method: 'GET', url: '/api/stack/list', isArray: true}});


  }


  /**
   * Fetch the stacks
   */
  fetchStacks() {
    // FIXME : remove comments and the client's side json file when remote stack api will be implemented
    /*
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



     }, (error) => {*/
    let stackContent = 'assets/html/che-stack.json';
    let promise = this.$http.get(stackContent);
    let updatedPromise = promise.then((data) => {
      let stacks = data.data;
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


}
