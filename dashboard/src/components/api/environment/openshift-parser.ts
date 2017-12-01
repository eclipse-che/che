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

export interface IPodList {
  kind: string;
  items: Array<IPodItem>;
}

export interface IPodItem {
  apiVersion: string;
  kind: string;
  metadata: {
    name?: string;
    generateName?: string;
    [propName: string]: string | Object;
  };
  spec: { containers: Array<IPodItemContainer> };
  [propName: string]: string | Object;
}

export interface IPodItemContainer {
  name: string;
  image: string;
  resources ?: {
    limits?: {
      memory?: string;
    }
  };
  [propName: string]: string | Object;
}

/**
 * Wrapper for jsyaml and simple openshift validator.
 *
 *  @author Oleksii Orel
 */
export class OpenshiftParser {

  /**
   * Parses recipe content
   *
   * @param content {string} recipe content
   * @returns {{ items: Array<IPodItem> } | IPodItem} recipe object
   */
  parse(content: string): IPodList | IPodItem {
    const recipe = jsyaml.load(content);
    this.validate(recipe);
    return recipe;
  }

  /**
   * Dumps recipe object.
   *
   * @param recipe {{ items: Array<IPodItem> } | IPodItem} recipe object
   * @returns {string} recipe content
   */
  dump(recipe: IPodList | IPodItem): string {
    return jsyaml.dump(recipe, {'indent': 1});
  }

  /**
   * Simple validation of recipe.
   *
   * @param recipe {{ items: Array<IPodItem> } | IPodItem}
   */
  private validate(recipe: IPodList | IPodItem): void {
    if (!recipe || !recipe.kind) {
      throw new TypeError(`Recipe should contain a "kind" section.`);
    }
    if (recipe.kind.toLowerCase() === 'list') {
      const podItems = (<IPodList>recipe).items;
      if (!podItems) {
        throw new TypeError(`Recipe pod list should contain an "items" section.`);
      }
      if (!angular.isArray(podItems) || podItems.length === 0) {
        throw new TypeError(`Recipe pod list should contain at least one "item".`);
      } else {
        podItems.forEach((podItem: IPodItem) => {
          if (podItem.kind.toLowerCase() === 'pod') {
            this.podItemValidate(podItem);
          }
        });
      }
    } else if (recipe.kind.toLowerCase() === 'pod') {
      this.podItemValidate(<IPodItem>recipe);
    }
  }

  /**
   * Simple validation of recipe podItem.
   *
   * @param podItem {IPodItem}
   */
  private podItemValidate(podItem: IPodItem): void {
    if (!podItem.apiVersion) {
      throw new TypeError(`Recipe pod item should contain "apiVersion" section.`);
    }
    if (!podItem.metadata) {
      throw new TypeError(`Recipe pod item should contain "metadata" section.`);
    }
    if (!podItem.metadata.name && !podItem.metadata.generateName) {
      throw new TypeError(`Recipe pod item metadata should contain "name" section.`);
    }
    if (!podItem.spec) {
      throw new TypeError(`Recipe pod item should contain "spec" section.`);
    }
    if (!podItem.spec.containers) {
      throw new TypeError(`Recipe pod item spec should contain "containers" section.`);
    }
    if (!angular.isArray(podItem.spec.containers) || podItem.spec.containers.length === 0) {
      throw new TypeError(`Recipe pod item spec containers should contain at least one "container".`);
    } else {
      podItem.spec.containers.forEach((podItemContainer: IPodItemContainer) => {
        this.podItemContainerValidate(podItemContainer);
      });
    }
  }

  /**
   * Simple validation of recipe podItemContainer.
   *
   * @param podItemContainer {IPodItemContainer}
   */
  private podItemContainerValidate(podItemContainer: IPodItemContainer): void {
    if (!podItemContainer.name) {
      throw new TypeError(`Recipe pod item container should contain "name" section.`);
    }
    if (!podItemContainer.image) {
      throw new TypeError(`Recipe pod item container should contain "image" section.`);
    }
  }

}
