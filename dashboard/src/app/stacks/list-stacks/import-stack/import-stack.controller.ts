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
import {CheEnvironmentRegistry} from '../../../../components/api/environment/che-environment-registry.factory';
import {ImportStackService} from '../../stack-details/import-stack.service';
import {CheStack} from '../../../../components/api/che-stack.factory';
import {StackValidationService} from '../../stack-details/stack-validation.service';
import {IEnvironmentManagerMachine} from '../../../../components/api/environment/environment-manager-machine';
import {CheBranding} from '../../../../components/branding/che-branding.factory';
import {ComposeParser} from '../../../../components/api/environment/compose-parser';
import {DockerfileParser} from '../../../../components/api/environment/docker-file-parser';

const DEFAULT_WORKSPACE_RAM: number = 2 * Math.pow(1024, 3);
const DOCKERFILE = 'dockerfile';
const COMPOSE = 'compose';
/**
 * @ngdoc controller
 * @name stacks.list-stacks.import-stack.controller:ImportStackController
 * @description This class is handling the controller for a dialog box with different types of recipe for target stack.
 * @author Oleksii Orel
 */
export class ImportStackController {
  private $timeout: ng.ITimeoutService;
  private $mdDialog: ng.material.IDialogService;
  private $location: ng.ILocationService;
  private cheStack: CheStack;
  private importStackService: ImportStackService;
  private stackValidationService: StackValidationService;
  private cheEnvironmentRegistry: CheEnvironmentRegistry;
  private recipeValidation: che.IValidation;

  private composeParser: ComposeParser;
  private dockerfileParser: DockerfileParser;
  private recipeValidationError: string;

  private editingTimeoutPromise: ng.IPromise<any>;
  private recipeScript: string;
  private recipeFormat: string;
  private stackDocsUrl: string;

  private editorOptions: {
    lineWrapping: boolean,
    lineNumbers: boolean,
    matchBrackets: boolean,
    mode: string,
    onLoad: Function
  };

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($mdDialog: ng.material.IDialogService, $location: ng.ILocationService, stackValidationService: StackValidationService,
              cheStack: CheStack, importStackService: ImportStackService, cheEnvironmentRegistry: CheEnvironmentRegistry,
              $timeout: ng.ITimeoutService, cheBranding: CheBranding) {
    this.$timeout = $timeout;
    this.$mdDialog = $mdDialog;
    this.$location = $location;
    this.cheStack = cheStack;
    this.importStackService = importStackService;
    this.stackValidationService = stackValidationService;
    this.cheEnvironmentRegistry = cheEnvironmentRegistry;

    this.composeParser = new ComposeParser();
    this.dockerfileParser = new DockerfileParser();
    this.stackDocsUrl = cheBranding.getDocs().stack;

    this.editorOptions = {
      lineWrapping: true,
      lineNumbers: true,
      matchBrackets: true,
      mode: undefined,
      onLoad: (editor: any) => {
        this.setEditor(editor);
        editor.focus();
      }
    };

    this.onRecipeChange();
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
    this.updateImportedStack();
    this.$mdDialog.hide();
    this.$location.path('/stack/import');
  }

  setEditor(editor: any): void {
    editor.on('paste', () => {
      let content = editor.getValue();
      this.detectFormat(content);
    });
    editor.on('change', () => {
      let content = editor.getValue();
      this.trackChangesInProgress(content);
    });
  }

  trackChangesInProgress(content: string): void {
    if (this.editingTimeoutPromise) {
      this.$timeout.cancel(this.editingTimeoutPromise);
    }

    this.editingTimeoutPromise = this.$timeout(() => {
      this.detectFormat(content);
      this.validateRecipe(content);
    }, 100);
  }

  detectFormat(content: string): void {
    if (!content || content.trim().length === 0) {
      return;
    }
    // compose format detection:
    if (content.match(/^services:\n/m)) {
      this.recipeFormat = COMPOSE;
      this.editorOptions.mode = 'text/x-yaml';
    }

    // docker file format detection
    if (!this.recipeFormat || content.match(/^FROM\s+\w+/m)) {
      this.recipeFormat = DOCKERFILE;
      this.editorOptions.mode = 'text/x-dockerfile';
    }
  }

  validateRecipe(content: string): void {
    this.recipeValidationError = '';

    if (!content) {
      return;
    }

    try {
      if (this.recipeFormat === DOCKERFILE) {
        this.dockerfileParser.parse(content);
      } else if (this.recipeFormat === COMPOSE) {
        this.composeParser.parse(content);
      }
    } catch (e) {
      this.recipeValidationError = e.message;
    }
  }

  /**
   * Returns validation state of the recipe.
   * @returns {boolean}
   */
  isRecipeValid(): boolean {
    return angular.isUndefined(this.recipeValidationError) || this.recipeValidationError.length === 0;
  }

  onRecipeChange(): void {
    this.$timeout(() => {
      this.detectFormat(this.recipeScript);
    }, 10);
  }

  /**
   * Update imported stack if it is valid.
   */
  updateImportedStack(): void {
    let stack = angular.copy(this.cheStack.getStackTemplate());
    let environments = stack.workspaceConfig.environments;
    let defaultEnv = angular.copy(environments[stack.workspaceConfig.defaultEnv]);
    defaultEnv.recipe = {
      content: this.recipeScript,
      contentType: COMPOSE === this.recipeFormat ? 'application/x-yaml' : 'text/x-dockerfile',
      type: this.recipeFormat
    };
    let environmentManager = this.cheEnvironmentRegistry.getEnvironmentManager(defaultEnv.recipe.type);
    // add ws-agent to default dev-machine for dockerfile recipe format
    defaultEnv.machines = DOCKERFILE === this.recipeFormat ? {
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

    // check each machine for RAM to be set
    machines.forEach((machine: IEnvironmentManagerMachine) => {
      let memoryLimit = environmentManager.getMemoryLimit(machine);
      if (!memoryLimit || memoryLimit === -1) {
        environmentManager.setMemoryLimit(machine, DEFAULT_WORKSPACE_RAM);
      }
    });

    let environment = environmentManager.getEnvironment(defaultEnv, machines);
    // check recipe correctness
    this.recipeValidation = this.stackValidationService.getRecipeValidation(environment.recipe);
    if (!this.recipeValidation.isValid) {
      return;
    }
    environments[stack.workspaceConfig.defaultEnv] = environment;
    this.importStackService.setStack(stack);
  }
}
