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
 * Wrapper for jsyaml and simple validator for openshift machine recipe.
 *
 *  @author Oleksii Orel
 */
export class OpenshiftMachineRecipeParser {

  /**
   * Parses recipe content
   *
   * @param content {string} recipe content
   * @returns {IPodItem} recipe object
   */
  parse(content: string): IPodItem {
    const recipe = jsyaml.load(content);
    this.validate(recipe);
    return recipe;
  }

  /**
   * Dumps recipe object.
   *
   * @param recipe {IPodItem} recipe object
   * @returns {string} recipe content
   */
  dump(recipe: IPodItem): string {
    return jsyaml.dump(recipe, {'indent': 1});
  }

  /**
   * Simple validation of machine recipe.
   *
   * @param recipe {IPodItem}
   */
  validate(recipe: IPodItem): void {
    if (!recipe || !recipe.kind) {
      throw new TypeError(`Recipe should contain a 'kind' section.`);
    }
    if (recipe.kind.toLowerCase() !== 'pod') {
      throw new TypeError(`Recipe 'kind' section should be equals 'pod'.`);
    }
    if (!recipe.apiVersion) {
      throw new TypeError(`Recipe pod item should contain 'apiVersion' section.`);
    }
    if (!recipe.metadata) {
      throw new TypeError(`Recipe pod item should contain 'metadata' section.`);
    }
    if (!recipe.metadata.name && !recipe.metadata.generateName) {
      throw new TypeError(`Recipe pod item metadata should contain 'name' section.`);
    }
    if (recipe.metadata.name && !this.testName(recipe.metadata.name)) {
      throw new TypeError(`Recipe pod item container name should not contain special characters like dollar, etc.`);
    }
    if (!recipe.spec) {
      throw new TypeError(`Recipe pod item should contain 'spec' section.`);
    }
    if (!recipe.spec.containers) {
      throw new TypeError(`Recipe pod item spec should contain 'containers' section.`);
    }
    if (!angular.isArray(recipe.spec.containers) || recipe.spec.containers.length === 0) {
      throw new TypeError(`Recipe pod item spec containers should contain at least one 'container'.`);
    }
    recipe.spec.containers.forEach((podItemContainer: IPodItemContainer) => {
      if (!podItemContainer.name) {
        throw new TypeError(`Recipe pod item container should contain 'name' section.`);
      }
      if (!this.testName(podItemContainer.name)) {
        throw new TypeError(`Recipe pod item container name should not contain special characters like dollar, etc.`);
      }
      if (!podItemContainer.image) {
        throw new TypeError(`Recipe pod item container should contain 'image' section.`);
      }
    });

  }

  /**
   * Returns true if the name is valid.
   * @param name {string}
   * @returns {boolean}
   */
  private testName(name: string): boolean {
    return /^[a-z0-9]([-a-z0-9]*[a-z0-9])?(\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*$/.test(name);
  }
}
