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
import {ISupportedListItem, KubernetesMachineRecipeParser, isSupportedItem} from './kubernetes-machine-recipe-parser';
import {IParser} from './parser';

export interface ISupportedItemList {
  kind: string;
  apiVersion: string;
  items: Array<ISupportedListItem>;
}

/**
 * Wrapper for jsyaml and simple validator for kubernetes environment recipe.
 *
 *  @author Oleksii Orel
 */
export class KubernetesEnvironmentRecipeParser implements IParser {
  private machineRecipeParser = new KubernetesMachineRecipeParser();
  private recipeByContent: Map<string, ISupportedItemList> = new Map();
  private recipeKeys: Array<string> = [];

  /**
   * Parses recipe content
   * @param content {string} recipe content
   * @returns {ISupportedItemList} recipe object
   */
  parse(content: string): ISupportedItemList {
    let recipe: ISupportedItemList;
    if (this.recipeByContent.has(content)) {
      recipe = angular.copy(this.recipeByContent.get(content));
      this.validate(recipe);
      return recipe;
    }
    recipe = jsyaml.safeLoad(content);
    // add to buffer
    this.recipeByContent.set(content, angular.copy(recipe));
    this.recipeKeys.push(content);
    if (this.recipeKeys.length > 3) {
      this.recipeByContent.delete(this.recipeKeys.shift());
    }
    this.validate(recipe);

    return recipe;
  }

  /**
   * Dumps recipe object.
   * @param recipe {ISupportedItemList} recipe object
   * @returns {string} recipe content
   */
  dump(recipe: ISupportedItemList): string {
    return jsyaml.safeDump(recipe, {'indent': 1});
  }

  /**
   * Helper method for retreiving items with in a parsed recipe. Useful for overriding
   * in subclasses mainly, as e.g. OpenShift recipes support templates as well as lists.
   *
   * Types are left as `any` to allow overriding
   *
   * @param recipe the parsed and validated recipe
   */
  getRecipeItems(recipe: any): Array<any> {
    return recipe.items;
  }

  /**
   * Simple validation of recipe.
   * @param recipe {ISupportedItemList}
   */
  private validate(recipe: ISupportedItemList): void {
    if (!recipe || !recipe.kind) {
      throw new TypeError(`Recipe should contain a 'kind' section.`);
    }
    if (recipe.kind.toLowerCase() !== 'list') {
      throw new TypeError(`Recipe 'kind' section should be equals 'list'.`);
    }
    const items = recipe.items;
    if (!items) {
      throw new TypeError(`Recipe kubernetes list should contain an 'items' section.`);
    }
    if (!angular.isArray(items) || items.length === 0) {
      throw new TypeError(`Recipe kubernetes list should contain at least one 'item'.`);
    } else {
      items.forEach((item: any) => {
        if (!item) {
          return;
        }
        // skip services
        if (item.kind && item.kind.toLowerCase() === 'service') {
          return;
        }
        if (!isSupportedItem(item)) {
          throw new TypeError(`Item of kind '${item.kind}' is not supported in Kubernetes recipes`);
        }
        this.machineRecipeParser.validate(item);
      });
    }
  }

}
