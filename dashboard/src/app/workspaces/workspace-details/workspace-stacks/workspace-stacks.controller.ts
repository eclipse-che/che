/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';
import {CheWorkspace} from '../../../../components/api/che-workspace.factory';
import {ComposeEnvironmentManager} from '../../../../components/api/environment/compose-environment-manager';
import {CheEnvironmentRegistry} from '../../../../components/api/environment/che-environment-registry.factory';

/**
 * @ngdoc controller
 * @name workspaces.workspace.stacks.controller:WorkspaceStacksController
 * @description This class is handling the controller for stacks selection
 * @author Oleksii Kurinnyi
 */

const DEFAULT_WORKSPACE_RAM: number = 2 * Math.pow(1024, 3);

export class WorkspaceStacksController {
  $scope: ng.IScope;
  cheWorkspace: CheWorkspace;
  composeEnvironmentManager: ComposeEnvironmentManager;

  recipeUrl: string;
  recipeScript: string;
  recipeFormat: string;

  stack: any = null;
  isCustomStack: boolean = false;
  selectSourceOption: string;

  tabName: string;
  environmentName: string;
  workspaceName: string;
  workspaceImportedRecipe: {
    type: string,
    content: string,
    location: string
  };
  workspaceStackOnChange: Function;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($scope: ng.IScope, cheWorkspace: CheWorkspace, cheEnvironmentRegistry: CheEnvironmentRegistry) {
    this.cheWorkspace = cheWorkspace;
    this.composeEnvironmentManager = cheEnvironmentRegistry.getEnvironmentManager('compose');

    $scope.$watch(() => { return this.recipeScript; }, () => {
      if (this.isCustomStack) {
        this.cheStackLibrarySelecter(null);
      }
    });
    $scope.$watch(() => { return this.recipeUrl; }, () => {
      if (this.isCustomStack) {
        this.cheStackLibrarySelecter(null);
      }
    });
    $scope.$watch(() => { return this.recipeFormat; }, () => {
      if (this.isCustomStack) {
        this.cheStackLibrarySelecter(null);
      }
    });

    $scope.$watch(() => { return this.workspaceImportedRecipe; }, () => {
      if (!this.workspaceImportedRecipe) {
        return;
      }
      this.initStackSelecter();
    }, true);
  }

  /**
   * Initialize stack selector widget.
   */
  initStackSelecter(): void {
    let type = this.workspaceImportedRecipe.type;
    if (this.workspaceImportedRecipe.location && type !== 'dockerimage') {
      this.recipeFormat = type;
      this.recipeUrl = this.workspaceImportedRecipe.location;
      this.tabName = 'stack-import';
      delete this.recipeScript;
    } else {
      if (type === 'dockerimage') {
        type = 'dockerfile';
        this.recipeScript = 'FROM ' + this.workspaceImportedRecipe.location;
      } else {
        this.recipeScript = this.workspaceImportedRecipe.content;
      }
      this.recipeFormat = type;
      this.tabName = 'stack-authoring';
      delete this.recipeUrl;
    }
  }

  /**
   * Callback when stack has been set.
   *
   * @param stack {object} the selected stack
   */
  cheStackLibrarySelecter(stack: any): void {
    if (stack) {
      this.isCustomStack = false;
      this.recipeUrl = null;
      this.recipeScript = null;
    } else {
      this.isCustomStack = true;
    }
    this.stack = stack;

    let source = this.getSource();
    let config = this.buildWorkspaceConfig(source);

    this.workspaceStackOnChange({config: config, stackId: this.stack ? this.stack.id : ''});
  }

  /**
   * Builds workspace config.
   *
   * @param source
   * @returns {config}
   */
  buildWorkspaceConfig(source: any): any {
    let stackWorkspaceConfig;
    if (this.stack) {
      stackWorkspaceConfig = this.stack.workspaceConfig;
    } else if (!this.stack && source && source.format === 'compose' && source.content) {
      let machines    = this.composeEnvironmentManager.getMachines({recipe: source}),
          environment = this.composeEnvironmentManager.getEnvironment({recipe: source}, machines);
      stackWorkspaceConfig = {
        defaultEnv: this.environmentName,
        environments: {
          [this.environmentName]: environment
        }
      };
    }


    return this.cheWorkspace.formWorkspaceConfig(stackWorkspaceConfig, this.workspaceName, source, DEFAULT_WORKSPACE_RAM);
  }

  /**
   * Returns stack source.
   *
   * @returns {object}
   */
  getSource(): any {
    let source: any = {};
    source.type = 'dockerfile';
    // user provides recipe URL or recipe's content:
    if (this.isCustomStack) {
      this.stack = null;
      source.type = 'environment';
      source.format = this.recipeFormat;
      if (this.recipeUrl && this.recipeUrl.length > 0) {
        source.location = this.recipeUrl;
      } else {
        source.content = this.recipeScript;
      }
    } else if (this.stack) {
      // check predefined recipe location
      if (this.stack && this.stack.source && this.stack.source.type === 'location') {
        this.recipeUrl = this.stack.source.origin;
        source.location = this.recipeUrl;
      } else {
        source = this.getSourceFromStack(this.stack);
      }
    }
    return source;
  }

  /**
   * Detects machine source from pointed stack.
   *
   * @param stack {object} to retrieve described source
   * @returns {source} machine source config
   */
  getSourceFromStack(stack: any): any {
    let source: any = {};
    source.type = 'dockerfile';

    switch (stack.source.type.toLowerCase()) {
      case 'image':
        source.content = 'FROM ' + stack.source.origin;
        break;
      case 'dockerfile':
        source.content = stack.source.origin;
        break;
      default:
        throw 'Not implemented';
    }

    return source;
  }
}
