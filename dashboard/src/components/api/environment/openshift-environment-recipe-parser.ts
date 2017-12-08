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
import {IPodItem, OpenshiftMachineRecipeParser} from './openshift-machine-recipe-parser';

export interface IPodList {
  kind: string;
  items: Array<IPodItem>;
}

/**
 * Wrapper for jsyaml and simple validator for openshift environment recipe.
 *
 *  @author Oleksii Orel
 */
export class OpenshiftEnvironmentRecipeParser {
  private machineRecipeParser = new OpenshiftMachineRecipeParser();

  /**
   * Parses recipe content
   * @param content {string} recipe content
   * @returns {IPodList} recipe object
   */
  parse(content: string): IPodList {
    const recipe = jsyaml.load(content);
    this.validate(recipe);
    return recipe;
  }

  /**
   * Dumps recipe object.
   * @param recipe {IPodList} recipe object
   * @returns {string} recipe content
   */
  dump(recipe: IPodList): string {
    return jsyaml.dump(recipe, {'indent': 1});
  }

  /**
   * Simple validation of recipe.
   * @param recipe {IPodList}
   */
  private validate(recipe: IPodList): void {
    if (!recipe || !recipe.kind) {
      throw new TypeError(`Recipe should contain a 'kind' section.`);
    }
    if (recipe.kind.toLowerCase() !== 'list') {
      throw new TypeError(`Recipe 'kind' section should be equals 'list'.`);
    }
    const podItems = (<IPodList>recipe).items;
    if (!podItems) {
      throw new TypeError(`Recipe pod list should contain an 'items' section.`);
    }
    if (!angular.isArray(podItems) || podItems.length === 0) {
      throw new TypeError(`Recipe pod list should contain at least one 'item'.`);
    } else {
      podItems.forEach((podItem: IPodItem) => {
        // skip services
        if (podItem.kind && podItem.kind.toLowerCase() === 'service') {
          return;
        }
        this.machineRecipeParser.validate(podItem);
      });
    }
  }

}
