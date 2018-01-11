/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
