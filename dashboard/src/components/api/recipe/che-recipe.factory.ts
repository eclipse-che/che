/*
 * Copyright (c) 2015-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

interface IRecipeResource<T> extends ng.resource.IResourceClass<T> {
  create(recipe: che.IRecipe): ng.resource.IResource<T>;
  getRecipes(): ng.resource.IResource<T>;
}

/**
 * This class is handling the recipes retrieval
 * It sets to the array of recipes
 * @author Florent Benoit
 */
export class CheRecipe {
  private $resource: ng.resource.IResourceService;
  private $q: ng.IQService;
  private remoteRecipesAPI: IRecipeResource<any>;
  private recipesById: Map<string, che.IRecipe>;
  private recipes: Array<che.IRecipe>;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($resource: ng.resource.IResourceService, $q: ng.IQService) {
    this.$resource = $resource;
    this.$q = $q;

    // recipes per id
    this.recipesById = new Map();
    // recipes
    this.recipes = [];
    // remote call
    this.remoteRecipesAPI = <IRecipeResource<any>> this.$resource('/api/recipe', {}, {
      create: {method: 'POST', url: '/api/recipe'},
      getRecipes: {method: 'GET', url: '/api/recipe/list', isArray: true}
    });
  }

  /**
   * Fetch the recipes.
   * @returns {IPromise<Array<che.IRecipe>>}
   */
  fetchRecipes(): ng.IPromise<Array<che.IRecipe>> {
    const defer = this.$q.defer();
    const promise = this.remoteRecipesAPI.getRecipes().$promise;

    promise.then((recipes: Array<che.IRecipe>) => {
      // clear global list
      this.recipes.length = 0;
      // clear global map
      this.recipesById.clear();
      // update globals
      recipes.forEach((recipe: che.IRecipe) => {
        this.recipes.push(recipe);
        this.recipesById.set((<any>recipe).id, recipe);
      });
      defer.resolve(recipes);
    }, (error: any) => {
      if (error.status !== 304) {
        defer.reject(error);
      } else {
        defer.resolve(this.recipes);
      }
    });

    return defer.promise;
  }

  /**
   * Create a recipe.
   * @param recipe {che.IRecipe}
   * @returns {angular.IPromise<any>}
   */
  create(recipe: che.IRecipe): ng.IPromise<any> {
    return this.remoteRecipesAPI.create(recipe).$promise;
  }

  /**
   * Gets all recipes.
   * @returns {Array<che.IRecipe>}
   */
  getRecipes(): Array<che.IRecipe> {
    return this.recipes;
  }

  /**
   * The recipes per id.
   * @param recipeId {string}
   * @returns {undefined|che.IRecipe}
   */
  getRecipeById(recipeId: string): che.IRecipe {
    return this.recipesById.get(recipeId);
  }
}
