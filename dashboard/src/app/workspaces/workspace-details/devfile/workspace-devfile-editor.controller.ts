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
/**
 * @ngdoc controller
 * @name workspaces.devfile-editor.controller:WorkspaceDevfileEditorController
 * @description This class is handling the controller for the workspace devfile editor widget
 * @author Anna Shumilova
 */
export class WorkspaceDevfileEditorController {

  static $inject = ['$log', '$scope', '$timeout'];

  $log: ng.ILogService;
  $scope: ng.IScope;
  $timeout: ng.ITimeoutService;

  editorOptions: {
    lineWrapping: boolean,
    lineNumbers: boolean,
    matchBrackets: boolean,
    mode: string,
    onLoad: Function
  };
  devfileValidationMessages: string[] = [];
  isActive: boolean;
  workspaceDevfile: che.IWorkspaceDevfile;
  devfileYaml: string;
  newWorkspaceDevfile: che.IWorkspaceDevfile;
  workspaceDevfileOnChange: Function;
  private saveTimeoutPromise: ng.IPromise<any>;
  private isSaving: boolean;


  /**
   * Default constructor that is using resource
   */
  constructor($log: ng.ILogService, $scope: ng.IScope, $timeout: ng.ITimeoutService) {
    this.$log = $log;
    this.$scope = $scope;
    this.$timeout = $timeout;
    this.isSaving = false;
    this.devfileYaml = jsyaml.dump(this.workspaceDevfile);

    $scope.$watch(() => {
      return this.workspaceDevfile;
    }, () => {
      let editedWorkspaceDevfile;
      try {
        editedWorkspaceDevfile = jsyaml.load(this.devfileYaml);
        angular.extend(editedWorkspaceDevfile, this.workspaceDevfile);
      } catch (e) {
        editedWorkspaceDevfile = this.workspaceDevfile;
      }
      this.devfileYaml = jsyaml.dump(this.workspaceDevfile);
      const validateOnly = true;
      this.onChange(validateOnly);
    }, true);
  }

  $onInit(): void { }

  /**
   * Callback when editor content is changed.
   */
  onChange(validateOnly?: boolean): void {
    this.devfileValidationMessages = [];
    if (!this.devfileYaml) {
      return;
    }

    let devfile;
    try {
      devfile = jsyaml.load(this.devfileYaml);
    } catch (e) {
      if (e.name === 'YAMLException') {
        this.devfileValidationMessages = [e.message];
      }
      if (this.devfileValidationMessages.length === 0) {
        this.devfileValidationMessages = ['Devfile is invalid.'];
      }
      this.$log.error(e);
    }

    if (validateOnly || !this.isActive) {
      return;
    }
    this.isSaving = (this.devfileValidationMessages.length === 0) && !angular.equals(devfile, this.workspaceDevfile);

    if (this.saveTimeoutPromise) {
      this.$timeout.cancel(this.saveTimeoutPromise);
    }

    this.saveTimeoutPromise = this.$timeout(() => {
      // immediately apply config on IU
      this.newWorkspaceDevfile = angular.copy(devfile);
      this.isSaving = false;
      this.applyChanges();
    }, 2000);
  }

  /**
   * Callback when user applies new config.
   */
  applyChanges(): void {
    this.workspaceDevfileOnChange({devfile: this.newWorkspaceDevfile});
  }
}
