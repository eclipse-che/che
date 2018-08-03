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

export interface IRecipeEditorControllerScope {
  allowInvalidRecipe: boolean;
  editorMode: string;
  changeRecipeFn(data: {content: string}): void;
  validateRecipeFn(data: {content: string}): string;
}

/**
 * This class is handling the directive of the recipe editor.
 * @author Oleksii Kurinnyi
 */
export class RecipeEditorDirective implements ng.IDirective {

  restrict = 'E';
  templateUrl = 'app/stacks/list-stacks/build-stack/recipe-editor/recipe-editor.html';

  controller = 'RecipeEditorController';
  controllerAs = 'recipeEditorController';
  bindToController = true;

  scope = {
    allowInvalidRecipe: '=',
    editorMode: '=',
    changeRecipeFn: '&',
    validateRecipeFn: '&'
  };

}
