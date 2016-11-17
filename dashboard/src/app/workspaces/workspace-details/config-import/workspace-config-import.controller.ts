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

/**
 * @ngdoc controller
 * @name workspaces.config-import.controller:WorkspaceConfigImportController
 * @description This class is handling the controller for the workspace config import widget
 * @author Oleksii Kurinnyi
 */
export class WorkspaceConfigImportController {
  $log: ng.ILogService;

  editorOptions: {
    lineWrapping: boolean,
    lineNumbers: boolean,
    matchBrackets: boolean,
    mode: string,
    onLoad: Function
  };

  validationError: string;
  importWorkspaceJson: string;
  copyImportWorkspaceJson: string;
  workspaceConfig: any;
  newWorkspaceConfig: any;
  workspaceConfigOnChange: Function;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($log: ng.ILogService, $scope: ng.IScope, $timeout: ng.ITimeoutService) {
    this.$log = $log;

    this.editorOptions = {
      lineWrapping: true,
      lineNumbers: true,
      matchBrackets: true,
      mode: 'application/json',
      onLoad: (editor: any) => {
        $timeout(() => {
          editor.refresh();
        }, 1000);
      }
    };

    $scope.$watch(() => { return this.workspaceConfig; }, () => {
      try {
        this.importWorkspaceJson = angular.toJson(this.workspaceConfig, true);
        this.copyImportWorkspaceJson = this.importWorkspaceJson;
      } catch (e) {
        this.$log.error(e);
      }
    }, true);
  }

  configValid(): boolean {
    return !this.validationError;
  }

  /**
   * Callback when editor content is changed.
   */
  onChange(): void {
    this.validationError = '';

    if (!this.importWorkspaceJson) {
      this.validationError = 'The config is required.';
      return;
    }

    try {
      let config = angular.fromJson(this.importWorkspaceJson);
      this.validationError = this.validateConfig(config);
      if (!this.validationError) {
        this.newWorkspaceConfig = angular.copy(config);
      }
    } catch (e) {
      this.validationError = 'JSON is invalid.';
      this.$log.error(e);
    }
  }

  /**
   * Performs checks for workspace config. Returns error message if config doesn't contain a mandatory property or those property has incorrect type.
   *
   * @param config {Object} workspace config provided by user
   * @returns {string} validation error message
   */
  validateConfig(config: any): string {
    if (!config.name) {
      return 'Config should contain property "name" which is a string.';
    }

    if (!angular.isObject(config.environments)) {
      return 'Config should contain property "environments" which is an Object.';
    }

    if (!config.defaultEnv) {
      return 'Config should contain property "defaultEnv" which is a string.';
    }

    if (!config.environments[config.defaultEnv]) {
      return 'Section "environments" should contain default environment.';
    }

    let envNames = Object.keys(config.environments);
    for (let i = 0; i < envNames.length; i++) {
      let envName = envNames[i];
      let machines = config.environments[envName].machines;
      if (!angular.isObject(machines)) {
        return `Environment "${envName}" should contain property "machines" which is an Object.`;
      }

      let recipe = config.environments[config.defaultEnv].recipe;
      if (!angular.isObject(recipe)) {
        return `Environment "${envName}" should contain property "recipe" which is an Object.`;
      } else if (!recipe.type) {
        return `Environment "${envName}": recipe should contain property "type" which is a string.`;
      } else if (!recipe.location && !recipe.content) {
        return `Environment "${envName}": recipe should have either "location" or "content" property which is a string.`;
      }
    }
  }

  /**
   * Returns true if config has been changed.
   *
   * @returns {boolean}
   */
  configChanged(): boolean {
    return this.copyImportWorkspaceJson !== this.importWorkspaceJson;
  }

  /**
   * Callback when user applies new config.
   */
  applyChanges(): void {
    this.workspaceConfigOnChange({config: this.newWorkspaceConfig});
  }
}
