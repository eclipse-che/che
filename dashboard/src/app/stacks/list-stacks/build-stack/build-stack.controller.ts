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
import {CheEnvironmentRegistry} from '../../../../components/api/environment/che-environment-registry.factory';
import {ImportStackService} from '../../stack-details/import-stack.service';
import {CheStack} from '../../../../components/api/che-stack.factory';
import {IEnvironmentManagerMachine} from '../../../../components/api/environment/environment-manager-machine';
import {CheBranding} from '../../../../components/branding/che-branding.factory';
import {CheRecipeTypes} from '../../../../components/api/recipe/che-recipe-types';
import {RecipeEditor} from './recipe-editor/recipe-editor';
import {CheWorkspace} from '../../../../components/api/workspace/che-workspace.factory';

const DEFAULT_WORKSPACE_RAM: number = 2 * Math.pow(1024, 3);
/**
 * @ngdoc controller
 * @name stacks.list-stacks.build-stack.controller:BuildStackController
 * @description This class is handling the controller for a dialog box with recipe editors of all supported types.
 * @author Oleksii Orel
 * @author Oleksii Kurinnyi
 */
export class BuildStackController {

  static $inject = ['$mdDialog', '$location', 'cheStack', 'importStackService', 'cheEnvironmentRegistry', 'cheBranding', 'cheWorkspace'];

  importStackService: ImportStackService;
  cheEnvironmentRegistry: CheEnvironmentRegistry;
  stackDocsUrl: string;
  recipeEditors: Array<RecipeEditor> = [];

  private $mdDialog: ng.material.IDialogService;
  private $location: ng.ILocationService;
  private cheStack: CheStack;
  private cheWorkspace: CheWorkspace;

  /**
   * Default constructor that is using resource
   */
  constructor($mdDialog: ng.material.IDialogService,
              $location: ng.ILocationService,
              cheStack: CheStack,
              importStackService: ImportStackService,
              cheEnvironmentRegistry: CheEnvironmentRegistry,
              cheBranding: CheBranding,
              cheWorkspace: CheWorkspace) {
    this.$mdDialog = $mdDialog;
    this.$location = $location;
    this.cheStack = cheStack;
    this.importStackService = importStackService;
    this.cheEnvironmentRegistry = cheEnvironmentRegistry;
    this.cheWorkspace = cheWorkspace;

    this.stackDocsUrl = cheBranding.getDocs().stack;

    cheWorkspace.fetchWorkspaceSettings().then(() => {
      this.buildEditors();
    });
  }

  /**
   * It will hide the dialog box.
   */
  cancel(): void {
    this.importStackService.setStack({} as che.IStack);
    this.$mdDialog.cancel();
  }

  /**
   * Builds recipe editor instance for each supported environment type.
   */
  buildEditors(): void {
    const supportedTypes = this.cheWorkspace.getSupportedRecipeTypes();
    supportedTypes.forEach((recipeType: string) => {
      const envManager = this.cheEnvironmentRegistry.getEnvironmentManager(recipeType),
        recipeEditor = new RecipeEditor(envManager);
      this.recipeEditors.push(recipeEditor);
    });
  }

  /**
   * Builds new stack and closes the popup.
   *
   * @param {RecipeEditor} recipeEditor
   */
  importRecipe(recipeEditor: RecipeEditor): void {
    this.buildStack(recipeEditor);

    this.$mdDialog.hide();
    this.$location.path('/stack/import');
  }

  /**
   * Builds new stack from recipe.
   *
   * @param {RecipeEditor} recipeEditor
   */
  buildStack(recipeEditor: RecipeEditor): void {
    const stack = angular.copy(this.cheStack.getStackTemplate());
    const environments = stack.workspaceConfig.environments;
    const defaultEnv = angular.copy(environments[stack.workspaceConfig.defaultEnv]);

    // set recipe for default environment
    defaultEnv.recipe = angular.copy(recipeEditor.recipe);

    // create new-machine in case of dockerfile or dockerimage recipe
    defaultEnv.machines = (CheRecipeTypes.DOCKERFILE === recipeEditor.recipe.type || CheRecipeTypes.DOCKERIMAGE === recipeEditor.recipe.type) ? {
      'new-machine': {
        'installers': [],
        'attributes': {
          'memoryLimitBytes': 2147483648
        }
      }
    } : {};
    const machines = recipeEditor.environmentManager.getMachines(defaultEnv);

    // check each machine for RAM to be set
    machines.forEach((machine: IEnvironmentManagerMachine) => {
      const memoryLimit = recipeEditor.environmentManager.getMemoryLimit(machine);
      if (!memoryLimit || memoryLimit === -1) {
        recipeEditor.environmentManager.setMemoryLimit(machine, DEFAULT_WORKSPACE_RAM);
      }
    });

    environments[stack.workspaceConfig.defaultEnv] = recipeEditor.environmentManager.getEnvironment(defaultEnv, machines);
    this.importStackService.setStack(stack);
  }

}
