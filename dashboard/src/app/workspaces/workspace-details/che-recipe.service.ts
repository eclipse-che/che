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
import {CheRecipeTypes} from '../../../components/api/recipe/che-recipe-types';

export interface ICheRecipeService {
  /**
   * Returns true if the recipe type is scalable.
   *
   * @param {string} recipeType
   * @returns {boolean}
   */
  isScalable(recipeType: string): boolean;
  /**
   * Returns true if the recipe type is scalable.
   *
   * @param {che.IRecipe} recipe
   * @returns {boolean}
   */
  isScalable(recipe: che.IRecipe): boolean;
  /**
   * Returns true if the environment's recipe type is kubernetes.
   *
   * @param {string} recipeType
   * @returns {boolean}
   */
  isKubernetes(recipeType: string): boolean;
  /**
   * Returns true if the environment's recipe type is kubernetes.
   *
   * @param {che.IRecipe} recipe
   * @returns {boolean}
   */
  isKubernetes(recipe: che.IRecipe): boolean;
  /**
   * Returns true if the environment's recipe type is compose.
   *
   * @param {string} recipeType
   * @returns {boolean}
   */
  isCompose(recipeType: string): boolean;
  /**
   * Returns true if the environment's recipe type is compose.
   *
   * @param {che.IRecipe} recipe
   * @returns {boolean}
   */
  isCompose(recipe: che.IRecipe): boolean;
  /**
   * Returns true if the environment's recipe type is openshift.
   *
   * @param {string} recipeType
   * @returns {boolean}
   */
  isOpenshift(recipeType: string): boolean;
  /**
   * Returns true if the environment's recipe type is openshift.
   *
   * @param {che.IRecipe} recipe
   * @returns {boolean}
   */
  isOpenshift(recipe: che.IRecipe): boolean;
  /**
   * Returns true if the environment's recipe type is dockerfile.
   *
   * @param {string} recipeType
   * @returns {boolean}
   */
  isDockerfile(recipeType: string): boolean;
  /**
   * Returns true if the environment's recipe type is dockerfile.
   *
   * @param {che.IRecipe} recipe
   * @returns {boolean}
   */
  isDockerfile(recipe: che.IRecipe): boolean;
  /**
   * Returns true if the environment's recipe type is dockerimage.
   *
   * @param {string} recipeType
   * @returns {boolean}
   */
  isDockerimage(recipeType: string): boolean;
  /**
   * Returns true if the environment's recipe type is dockerimage.
   *
   * @param {che.IRecipe} recipe
   * @returns {boolean}
   */
  isDockerimage(recipe: che.IRecipe): boolean;
}

/**
 * This class is handling the data for environment's recipe.
 *
 * @author Oleksii Orel
 */
export class CheRecipeService implements ICheRecipeService {

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

  isScalable(recipeType: string): boolean;
  isScalable(recipe: che.IRecipe): boolean;
  isScalable(arg: any): boolean {
    return this.isCompose(arg) || this.isKubernetes(arg) || this.isOpenshift(arg);
  }

  isKubernetes(recipeType: string): boolean;
  isKubernetes(recipe: che.IRecipe): boolean;
  isKubernetes(arg: any): boolean {
    if (typeof arg === 'string') {
      return <string>arg === CheRecipeTypes.KUBERNETES;
    }
    return this.getRecipeType(<che.IRecipe>arg) === CheRecipeTypes.KUBERNETES;
  }

  isOpenshift(recipeType: string): boolean;
  isOpenshift(recipe: che.IRecipe): boolean;
  isOpenshift(arg: any): boolean {
    if (typeof arg === 'string') {
      return <string>arg === CheRecipeTypes.OPENSHIFT;
    }
    return this.getRecipeType(<che.IRecipe>arg) === CheRecipeTypes.OPENSHIFT;
  }

  isCompose(recipeType: string): boolean;
  isCompose(recipe: che.IRecipe): boolean;
  isCompose(arg: any): boolean {
    if (typeof arg === 'string') {
      return <string>arg === CheRecipeTypes.COMPOSE;
    }
    return this.getRecipeType(<che.IRecipe>arg) === CheRecipeTypes.COMPOSE;
  }

  isDockerfile(recipeType: string): boolean;
  isDockerfile(recipe: che.IRecipe): boolean;
  isDockerfile(arg: any): boolean {
    if (typeof arg === 'string') {
      return <string>arg === CheRecipeTypes.DOCKERFILE;
    }
    return this.getRecipeType(<che.IRecipe>arg) === CheRecipeTypes.DOCKERFILE;
  }

  isDockerimage(recipeType: string): boolean;
  isDockerimage(recipe: che.IRecipe): boolean;
  isDockerimage(arg: any): boolean {
    if (typeof arg === 'string') {
      return <string>arg === CheRecipeTypes.DOCKERIMAGE;
    }
    return this.getRecipeType(<che.IRecipe>arg) === CheRecipeTypes.DOCKERIMAGE;
  }

  /**
   * Gets recipe type.
   *
   * @param {che.IRecipe} recipe
   * @returns {string}
   */
  getRecipeType(recipe: che.IRecipe): string {
    if (!recipe || !recipe.type) {
      this.$log.error('Unable to find the recipe type');
      return null;
    }
    return recipe.type;
  }

}
