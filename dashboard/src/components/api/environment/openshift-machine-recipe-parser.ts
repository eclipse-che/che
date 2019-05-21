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

import {KubernetesMachineRecipeParser, ISupportedListItem, IObjectMetadata, isSupportedItem} from './kubernetes-machine-recipe-parser';
import {IParser} from './parser';

/**
 * Item types supported in OpenShift recipes.
 */
export type ISupportedOpenShiftItem = ISupportedListItem | IRouteItem;

export function isSupportedOpenShiftItem(item: any): item is ISupportedOpenShiftItem {
  return isRouteItem(item) || isSupportedItem(item);
}

export function isRouteItem(item: ISupportedOpenShiftItem): item is IRouteItem {
  return (item.kind && item.kind.toLowerCase() === 'route');
}

export interface IRouteItem {
  apiVersion: string;
  kind: string;
  metadata: IObjectMetadata;
  spec: {
    host: string;
    port: {
      targetPort: string;
    }
    to: {
      kind: string;
      name: string;
    }
  }
}

/**
 * Wrapper for jsyaml and simple validator for OpenShift machine recipe.
 *
 * Uses KubernetesMachineRecipeParser to parse objects supported by Kubernetes,
 * and handles Routes in addition.
 *
 * @author Angel Misevski
 */
export class OpenShiftMachineRecipeParser implements IParser {
  private recipeByContent: Map<string, ISupportedListItem> = new Map();
  private recipeKeys: Array<string> = [];
  private kubernetesMachineRecipeParser: KubernetesMachineRecipeParser = new KubernetesMachineRecipeParser();


  parse(content: string): ISupportedOpenShiftItem {
    let recipe: ISupportedOpenShiftItem;
    if (this.recipeByContent.has(content)) {
      recipe = angular.copy(this.recipeByContent.get(content));
      this.validate(recipe);
      return recipe;
    }
    recipe = jsyaml.safeLoad(content);
    // add to buffer
    this.recipeByContent.set(content, angular.copy(recipe));
    this.recipeKeys.push(content);
    if (this.recipeKeys.length > 10) {
      this.recipeByContent.delete(this.recipeKeys.shift());
    }
    this.validate(recipe);

    return recipe;
  }

  dump(recipe: ISupportedListItem): string {
    return jsyaml.safeDump(recipe, {'indent': 1});
  }

  validate(recipe: ISupportedOpenShiftItem): void {
    if (isSupportedItem(recipe)) {
      this.kubernetesMachineRecipeParser.validate(recipe);
    } else if (isRouteItem(recipe)) {
      this.validateRoute(<IRouteItem>recipe);
    }
  }

  validateRoute(route: IRouteItem): void {
    this.kubernetesMachineRecipeParser.validateMetadata(route.metadata);
    if (!route.spec) {
      throw new TypeError(`Recipe route item should contain a 'spec' section`);
    }
    if (!route.spec.to
        || !route.spec.to.kind
        || route.spec.to.kind.toLowerCase() !== 'service') {
      throw new TypeError(`Recipe route item should have .spec.to.kind='Service'`);
    }
  }
}
