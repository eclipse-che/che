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
import {CheEnvironmentRegistry} from '../../../../components/api/environment/che-environment-registry.factory';
import {ImportStackService} from '../../stack-details/import-stack.service';
import {CheStack} from '../../../../components/api/che-stack.factory';
import {StackValidationService} from '../../stack-details/stack-validation.service';


/**
 * @ngdoc controller
 * @name stacks.list-stacks.import-stack.controller:ImportStackController
 * @description This class is handling the controller for a dialog box with different types of recipe for target stack.
 * @author Oleksii Orel
 */
export class ImportStackController {
  private $mdDialog: ng.material.IDialogService;
  private $location: ng.ILocationService;
  private cheStack: CheStack;
  private importStackService: ImportStackService;
  private stackValidationService: StackValidationService;
  private cheEnvironmentRegistry: CheEnvironmentRegistry;
  private recipeScript: string;
  private recipeFormat: string;
  private COMPOSE: string = 'compose';
  private DOCKERFILE: string = 'dockerfile';
  private environmentValidation: che.IValidation;
  private editorOptions: che.IEditorOptions = {
    lineNumbers: true,
    lineWrapping: true,
    matchBrackets: true,
    mode: 'text/x-dockerfile'
  };

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($mdDialog: ng.material.IDialogService, $location: ng.ILocationService, stackValidationService: StackValidationService, cheStack: CheStack, importStackService: ImportStackService, cheEnvironmentRegistry: CheEnvironmentRegistry) {
    this.$mdDialog = $mdDialog;
    this.$location = $location;
    this.cheStack = cheStack;
    this.importStackService = importStackService;
    this.stackValidationService = stackValidationService;
    this.cheEnvironmentRegistry = cheEnvironmentRegistry;

    // set default values
    this.environmentValidation = {isValid: false, errors: []};
    this.recipeFormat = this.DOCKERFILE;
    this.updateType();
  }

  /**
   * It will hide the dialog box.
   */
  cancel(): void {
    this.importStackService.setStack({});
    this.$mdDialog.cancel();
  }

  /**
   * It will hide the dialog box and redirect to '/stack/import'.
   */
  onImport(): void {
    this.$mdDialog.hide();
    this.$location.path('/stack/import');
  }

  /**
   * Update editor options for needed recipe type.
   */
  updateType(): void {
    if (this.DOCKERFILE === this.recipeFormat) {
      this.editorOptions.mode = 'text/x-dockerfile';
    } else if (this.COMPOSE === this.recipeFormat) {
      this.editorOptions.mode = 'text/x-yaml';
    }
    this.recipeScript = '';
    this.updateImportedStack();
  }

  /**
   * Update imported stack if it is valid.
   */
  updateImportedStack(): void {
    let stack = angular.copy(this.cheStack.getStackTemplate());
    let environments: che.IWorkspaceEnvironments = stack.workspaceConfig.environments;
    let defaultEnv: che.IWorkspaceEnvironment = angular.copy(environments[stack.workspaceConfig.defaultEnv]);
    let recipe: che.IRecipe = {content: this.recipeScript, type: this.recipeFormat};

    if (this.COMPOSE === this.recipeFormat) {
      recipe.contentType = 'application/x-yaml';
    }
    defaultEnv.recipe = recipe;
    let environmentManager = this.cheEnvironmentRegistry.getEnvironmentManager(defaultEnv.recipe.type);
    // add ws-agent to default dev-machine for dockerfile recipe format
    defaultEnv.machines = this.DOCKERFILE === this.recipeFormat ? {
      'dev-machine': {
        'agents': [],
        'attributes': {
          'memoryLimitBytes': 2147483648
        }
      }
    } : {};
    let machines = environmentManager.getMachines(defaultEnv);
    // add ws-agent to a machine if it one
    if (machines.length === 1) {
      environmentManager.setDev(machines[0], true);
    }
    let environment = environmentManager.getEnvironment(defaultEnv, machines);
    // check environment correctness
    this.environmentValidation = this.stackValidationService.getEnvironmentValidation(environment);
    if (!this.environmentValidation.isValid) {
      return;
    }
    environments[stack.workspaceConfig.defaultEnv] = environment;
    this.importStackService.setStack(stack);
  }
}
