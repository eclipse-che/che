/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {CheRecipeTypes} from '../../../components/api/recipe/che-recipe-types';

/**
 * This class is handling the data for environment's recipe.
 *
 * @author Oleksii Orel
 */
export class CheRecipeService {

  static $inject = ['$log'];

  /**
   * Logging service.
   */
  private $log: ng.ILogService;

  /**
   * Default constructor that is using resource
   */
  constructor($log: ng.ILogService) {
    this.$log = $log;
  }

  /**
   * Returns true if the recipe type is scalable.
   *
   * @param {che.IRecipe} recipe
   * @returns {boolean}
   */
  isScalable(recipe: che.IRecipe): boolean {
    return this.isCompose(recipe) || this.isKubernetes(recipe) || this.isOpenshift(recipe);
  }

  /**
   * Returns true if the recipe type is a supported type.
   *
   * @param {che.IRecipe} recipe
   * @returns {boolean}
   */
  isSupportedType(recipe: che.IRecipe): boolean {
    const recipeType = this.getRecipeType(recipe);
    if (recipeType === null) {
      return false;
    }
    return CheRecipeTypes.getValues().indexOf(recipeType) !== -1;
  }

  /**
   * Returns true if the environment's recipe type is kubernetes.
   *
   * @param {che.IRecipe} recipe
   * @returns {boolean}
   */
  isKubernetes(recipe: che.IRecipe): boolean {
    return this.getRecipeType(recipe) === CheRecipeTypes.KUBERNETES;
  }

  /**
   * Returns true if the environment's recipe type is openshift.
   *
   * @param {che.IRecipe} recipe
   * @returns {boolean}
   */
  isOpenshift(recipe: che.IRecipe): boolean {
    return this.getRecipeType(recipe) === CheRecipeTypes.OPENSHIFT;
  }

  /**
   * Returns true if the environment's recipe type is compose.
   *
   * @param {che.IRecipe} recipe
   * @returns {boolean}
   */
  isCompose(recipe: che.IRecipe): boolean {
    return this.getRecipeType(recipe) === CheRecipeTypes.COMPOSE;
  }

  /**
   * Returns true if the environment's recipe type is dockerfile.
   *
   * @param {che.IRecipe} recipe
   * @returns {boolean}
   */
  isDockerfile(recipe: che.IRecipe): boolean {
    return this.getRecipeType(recipe) === CheRecipeTypes.DOCKERFILE;
  }

  /**
   * Returns true if the environment's recipe type is dockerimage.
   *
   * @param {che.IRecipe} recipe
   * @returns {boolean}
   */
  isDockerimage(recipe: che.IRecipe): boolean {
    return this.getRecipeType(recipe) === CheRecipeTypes.DOCKERIMAGE;
  }

  /**
   * Gets recipe type.
   *
   * @param {che.IRecipe} recipe
   * @returns {string|null}
   */
  getRecipeType(recipe: che.IRecipe): string {
    if (!recipe || !recipe.type || !angular.isString(recipe.type)) {
      this.$log.error('Unable to find the recipe type');
      return null;
    }
    return recipe.type.toLowerCase();
  }

}
