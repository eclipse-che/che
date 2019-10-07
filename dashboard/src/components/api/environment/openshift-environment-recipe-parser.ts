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
import {OpenShiftMachineRecipeParser, ISupportedOpenShiftItem, isSupportedOpenShiftItem} from './openshift-machine-recipe-parser';
import {IParser} from './parser';
import {KubernetesMachineRecipeParser, IObjectMetadata} from './kubernetes-machine-recipe-parser';

export interface IOpenShiftList {
  kind: string;
  apiVersion: string;
  items: Array<ISupportedOpenShiftItem>;
}

export interface IOpenShiftTemplate {
  kind: string;
  metadata: IObjectMetadata;
  objects: Array<ISupportedOpenShiftItem>;
  parameters: Array<IOpenShiftTemplateParameter>;
}

export interface IOpenShiftTemplateParameter {
  name: string;
  value?: string;
  generate?: string;
  from?: string;
  [propName: string]: string | Object;
}

export type IOpenShiftRecipe = IOpenShiftList | IOpenShiftTemplate;

export function isOpenShiftList(item: IOpenShiftRecipe): item is IOpenShiftList {
  return (item.kind && item.kind.toLowerCase() === 'list');
}

export function isOpenShiftTemplate(item: IOpenShiftRecipe): item is IOpenShiftTemplate {
  return (item.kind && item.kind.toLowerCase() === 'template');
}

/**
 * Wrapper for jsyaml and simple validator for openshift environment recipe.
 * Can parse and validate either list or templated-based recipes
 *
 * @author Angel Misevski
 */
export class OpenShiftEnvironmentRecipeParser implements IParser {
  private openShiftMachineRecipeParser = new OpenShiftMachineRecipeParser();
  private recipeByContent: Map<string, IOpenShiftList | IOpenShiftTemplate> = new Map();
  private recipeKeys: Array<string> = [];

  /**
   * Parses recipe content
   * @param content {string} recipe content
   * @returns {IOpenShiftRecipe} recipe object
   */
  parse(content: string): IOpenShiftRecipe {
    let recipe: IOpenShiftRecipe;
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
   * @param recipe {IOpenShiftRecipe} recipe object
   * @returns {string} recipe content
   */
  dump(recipe: IOpenShiftRecipe): string {
    return jsyaml.safeDump(recipe, {'indent': 1});
  }

  /**
   * If recipe is a list, return list.items. If recipe is a template, return
   * recipe.objects instead.
   *
   * If recipe is neither, return an empty array.
   *
   * @param recipe the parsed and validated recipe
   */
  getRecipeItems(recipe: any): Array<any> {
    if (isOpenShiftList(recipe)) {
      return recipe.items;
    } else if (isOpenShiftTemplate) {
      return recipe.objects;
    } else {
      return new Array<ISupportedOpenShiftItem>();
    }
  }

  private validate(recipe: IOpenShiftRecipe): void {
    if (!recipe || !recipe.kind) {
      throw new TypeError(`Recipe should contain a 'kind' section.`);
    }
    if (isOpenShiftList(recipe)) {
      this.validateList(recipe);
    } else if (isOpenShiftTemplate(recipe)) {
      this.validateTemplate(recipe);
    } else {
      throw new TypeError(`OpenShift recipe must be either 'List' or 'Template'`);
    }
  }

  private validateList(recipe: IOpenShiftList): void {
    if (!recipe.items) {
      throw new TypeError(`Recipe 'list' must contain an 'items' section`);
    }
    if (!angular.isArray(recipe.items) || recipe.items.length === 0) {
      throw new TypeError(`Recipe should contain at least one OpenShift object`);
    }
    this.validateItems(recipe.items);
  }

  private validateTemplate(recipe: IOpenShiftTemplate): void {
    if (!recipe.objects) {
      throw new TypeError(`Recipe 'Template' must contain an 'objects' section`);
    }
    if (!angular.isArray(recipe.objects) || recipe.objects.length === 0) {
      throw new TypeError(`Recipe should contain at least one OpenShift object`);
    }
    this.validateItems(recipe.objects);

    if (recipe.parameters
        && angular.isArray(recipe.parameters)
        && recipe.parameters.length > 0) {
      this.validateTemplateParameters(recipe.parameters);
    }
  }

  private validateItems(items: Array<any>): void {
    items.forEach((item: any) => {
      if (!item) {
        return;
      }
      // skip services
      if (item.kind && item.kind.toLowerCase() === 'service') {
        return;
      }
      if (!isSupportedOpenShiftItem(item)) {
        throw new TypeError(`Item of kind '${item.kind}' is not supported in OpenShift recipes`);
      }
      this.openShiftMachineRecipeParser.validate(item);
    });
  }

  private validateTemplateParameters(params: Array<IOpenShiftTemplateParameter>): void {
    params.forEach((param: IOpenShiftTemplateParameter) => {
      if (!param) {
        return;
      }
      if (!param.name) {
        throw new TypeError(`Template parameters require a 'name' field`);
      }
      // make sure parameter has a default value since it cannot be specified later
      // note: generated parameters are not supported due to https://github.com/fabric8io/kubernetes-client/issues/1340
      if (param.value) {
        return;
      } else {
        throw new TypeError(`Template parameters must have default value.`);
      }
    });
  }
}
