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
 * This class is handling the recipes retrieval
 * It sets to the array of recipes
 * @author Florent Benoit
 */
export class CheRecipe {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($resource, $q) {
    // keep resource
    this.$resource = $resource;
    this.$q = $q;

    // recipes per id
    this.recipesByid = {};

    // recipes
    this.recipes = [];

    // remote call
    this.remoteRecipesAPI = this.$resource('/api/recipe',{}, {
      create: {method: 'POST', url: '/api/recipe'},
      getRecipes: {method: 'GET', url: '/api/recipe/list', isArray: true}});


  }


  /**
   * Fetch the recipes
   */
  fetchRecipes() {
    var defer = this.$q.defer();

    let promise = this.remoteRecipesAPI.getRecipes().$promise;
    promise.then((recipes) => {

      // reset global list
      this.recipes.length = 0;
      for (var member in this.recipesByid) {
        delete this.recipesByid[member];
      }

      recipes.forEach((recipe) => {

        // get attributes
        var recipeId = recipe.id;

        // add element on the list
        this.recipesByid[recipeId] = recipe;
        this.recipes.push(recipe);
      });
      defer.resolve();
    }, (error) => {
      if (error.status !== 304) {
        defer.reject(error);
      } else {
        defer.resolve();
      }
    });

    return defer.promise;
  }

  /**
   * Create a recipe
   * @param recipe the recipe to create
   * @returns {*}
   */
  create(recipe) {
    let promise = this.remoteRecipesAPI.create(recipe).$promise;
    return promise;
  }

  /**
   * Gets all recipes
   * @returns {Array}
   */
  getRecipes() {
    return this.recipes;
  }

  /**
   * The recipes per id
   * @returns {*}
   */
  getRecipeById(id) {
    return this.recipesByid[id];
  }


}
