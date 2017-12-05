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
import {CheErrorMessagesService} from '../../../../components/error-messages/che-error-messages.service';
import {StackValidationService} from '../../../stacks/stack-details/stack-validation.service';

/**
 * @ngdoc controller
 * @name workspaces.config-import.controller:WorkspaceConfigImportController
 * @description This class is handling the controller for the workspace config import widget
 * @author Oleksii Kurinnyi
 */
export class WorkspaceConfigImportController {
  $log: ng.ILogService;
  $scope: ng.IScope;
  $timeout: ng.ITimeoutService;
  errorMessagesService: CheErrorMessagesService;
  validationService: StackValidationService;

  editorOptions: {
    lineWrapping: boolean,
    lineNumbers: boolean,
    matchBrackets: boolean,
    mode: string,
    onLoad: Function
  };

  configValidationMessages: string[] = [];
  configErrorsNumber: number = 0;
  otherValidationMessages: {
    [errorsScope: string]: string[]
  } = {};
  errorsScopeSettings: string = 'workspace-details-settings';
  errorsScopeEnvironment: string = 'workspace-details-environment';
  importWorkspaceJson: string;
  workspaceConfig: any;
  newWorkspaceConfig: any;
  workspaceConfigOnChange: Function;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($log: ng.ILogService, $scope: ng.IScope, $timeout: ng.ITimeoutService, cheErrorMessagesService: CheErrorMessagesService, stackValidationService: StackValidationService) {
    this.$log = $log;
    this.$scope = $scope;
    this.$timeout = $timeout;
    this.errorMessagesService = cheErrorMessagesService;
    this.validationService = stackValidationService;

    this.editorOptions = {
      lineWrapping: true,
      lineNumbers: true,
      matchBrackets: true,
      mode: 'application/json',
      onLoad: (editor: any) => {
        $timeout(() => {
          editor.refresh();
        }, 500);
      }
    };

    this.importWorkspaceJson = angular.toJson(this.workspaceConfig);

    $scope.$watch(() => { return this.workspaceConfig; }, () => {
      try {
        let editedWorkspaceConfig = angular.fromJson(this.importWorkspaceJson) || {};
        angular.extend(editedWorkspaceConfig, this.workspaceConfig);

        this.importWorkspaceJson = angular.toJson(editedWorkspaceConfig, true);

        let validateOnly = true;
        this.onChange(validateOnly);
      } catch (e) {
        this.$log.error(e);
      }
    }, true);

    this.errorMessagesService.registerCallback(this.errorsScopeSettings, this.updateErrorsList.bind(this, this.errorsScopeSettings));
    this.errorMessagesService.registerCallback(this.errorsScopeEnvironment, this.updateErrorsList.bind(this, this.errorsScopeEnvironment));
  }

  updateErrorsList(errorsScope: string, otherErrors: string[]) {
    this.otherValidationMessages[errorsScope] = angular.copy(otherErrors);
  }

  workspaceConfigValidation(): che.IValidation {
     return this.validationService.getWorkspaceConfigValidation(angular.fromJson(this.importWorkspaceJson));
  }

  /**
   * Callback when editor content is changed.
   */
  onChange(validateOnly?: boolean): void {
    if (!this.importWorkspaceJson) {
      this.configValidationMessages = ['The config is required.'];
      return;
    }

    try {
      let config = angular.fromJson(this.importWorkspaceJson);
      let validationResult = this.validationService.getWorkspaceConfigValidation(config);

      this.configValidationMessages = angular.copy(validationResult.errors);
      this.configErrorsNumber = this.configValidationMessages.length;

      if (validateOnly) {
        return;
      }

      // immediately apply config on IU
      this.newWorkspaceConfig = angular.copy(config);
      this.applyChanges();

    } catch (e) {
      if (this.configValidationMessages.length === 0) {
        this.configValidationMessages = ['JSON is invalid.'];
      }
      this.$log.error(e);
    }

    this.configErrorsNumber = this.configValidationMessages.length;
  }

  /**
   * Callback when user applies new config.
   */
  applyChanges(): void {
    this.workspaceConfigOnChange({config: this.newWorkspaceConfig});
  }
}
