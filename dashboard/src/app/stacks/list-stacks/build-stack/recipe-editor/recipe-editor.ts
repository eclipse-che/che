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

import {EnvironmentManager} from '../../../../../components/api/environment/environment-manager';

export class RecipeEditor {

  environmentManager: EnvironmentManager;

  /**
   * Editor tab title.
   */
  title: string;
  /**
   * The recipe for a new stack.
   */
  recipe: che.IRecipe;

  constructor(environmentManager: EnvironmentManager) {
    this.environmentManager = environmentManager;

    this.title = this.environmentManager.type;

    this.recipe = {
      contentType: this.environmentManager.editorMode,
      type: this.environmentManager.type
    };
  }

  /**
   * Callback which is called when recipe content changes.
   * Saves recipe content.
   *
   * @param {string} content
   */
  changeRecipe(content: string): void {
    this.recipe.content = content;
  }

  /**
   * Callback which is called when recipe content changes.
   * Validates recipe content and returns error message if any.
   *
   * @param {string} content
   * @returns {string}
   */
  validateRecipe(content: string): string {
    return this.environmentManager.validateRecipe(content);
  }

}
