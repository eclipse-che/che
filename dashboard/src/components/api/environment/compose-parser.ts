/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

export interface IComposeRecipe {
  services: {
    [machineName: string]: any
  };
}

/**
 * Wrapper for jsyaml and simple validator.
 *
 * @author Oleksii Kurinnyi
 */
export class ComposeParser {

  /**
   * Parses recipe content
   *
   * @param content {string} recipe content
   * @returns {IComposeRecipe} recipe object
   */
  parse(content: string): IComposeRecipe {
    const recipe = jsyaml.load(content);
    this.validate(recipe);
    return recipe;
  }

  /**
   * Dumps recipe object.
   *
   * @param recipe {IComposeRecipe} recipe object
   * @returns {string} recipe content
   */

  dump(recipe: IComposeRecipe): string {
    return jsyaml.dump(recipe, {'indent': 1});
  }

  /**
   * Simple validation of recipe.
   *
   * @param recipe {IComposeRecipe}
   */
  private validate(recipe: IComposeRecipe): void {
    if (!recipe.services) {
      throw new TypeError(`Recipe should contain "services" section.`);
    }

    const services = Object.keys(recipe.services);
    services.forEach((serviceName: string) => {
      let serviceFields: string[] = Object.keys(recipe.services[serviceName] || {});
      if (!serviceFields || (serviceFields.indexOf('build') === -1 && serviceFields.indexOf('image') === -1)) {
        throw new TypeError(`Service "${serviceName}" should contain "build" or "image" section.`);
      }
    });
  }

}
