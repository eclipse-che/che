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

import {CheBranding} from '../../../../components/branding/che-branding.factory';
import { CheAPI } from '../../../../components/api/che-api.factory';

/**
 * @ngdoc controller
 * @name workspaces.devfile-editor.controller:WorkspaceDevfileEditorController
 * @description This class is handling the controller for the workspace devfile editor widget
 * @author Anna Shumilova
 */
export class WorkspaceDevfileEditorController {

  static $inject = [
    '$log',
    '$scope',
    '$timeout',
    'cheBranding',
    'cheAPI'
  ];
  private $log: ng.ILogService;
  private $scope: ng.IScope;
  private $timeout: ng.ITimeoutService;
  private cheBranding: CheBranding;

  private isActive: boolean;
  private workspaceDevfile: che.IWorkspaceDevfile;
  private workspaceDevfileOnChange: Function;
  private devfileDocsUrl: string;

  private editorOptions: {
    wordWrap: string,
    lineNumbers: string,
    matchBrackets: boolean,
    mode: string,
    onLoad: Function
  };
  private editorState: che.IValidation = {
    isValid: true,
    errors: []
  };
  private devfileYaml: string;
  private saveTimeoutPromise: ng.IPromise<any>;
  private cheAPI: CheAPI;


  /**
   * Default constructor that is using resource
   */
  constructor($log: ng.ILogService, $scope: ng.IScope, $timeout: ng.ITimeoutService, cheBranding: CheBranding, cheAPI: CheAPI) {
    this.$log = $log;
    this.$scope = $scope;
    this.$timeout = $timeout;
    this.cheBranding = cheBranding;
    this.cheAPI = cheAPI;

    this.$scope.$on('edit-workspace-details', (event: ng.IAngularEvent, attrs: { status: string }) => {
      if (attrs.status === 'cancelled') {
        this.$onInit();
      }
    });

    $scope.$watch(() => {
      return this.workspaceDevfile;
    }, () => {
      let devfile: che.IWorkspaceDevfile;
      try {
        devfile = jsyaml.safeLoad(this.devfileYaml);
      } catch (e) {
        return;
      }

      if (angular.equals(devfile, this.workspaceDevfile) === false) {
        Object.keys(devfile).forEach((key: string) => {
          if (!this.workspaceDevfile[key]) {
            delete devfile[key];
          }
        });
        angular.extend(devfile, this.workspaceDevfile);
        this.devfileYaml = jsyaml.safeDump(devfile);
      }
    }, true);
  }

  /**
   * Returns the workspace devfile validation.
   * @returns {che.IValidation}
   */
  workspaceDevfileValidation(): che.IValidation {
    const validation = {
      isValid: true,
      errors: []
    };

    try {
      jsyaml.safeLoad(this.devfileYaml);
    } catch (e) {
      if (e && e.name === 'YAMLException') {
        validation.errors = [e.message];
      } else {
        validation.errors = ['Unable to parse YAML.'];
      }
      validation.isValid = false;
      this.$log.error(e);
    }

    return validation;
  }

  $onInit(): void {
    this.devfileYaml = jsyaml.safeDump(this.workspaceDevfile);
    this.devfileDocsUrl = this.cheBranding.getDocs().devfile;
    const yamlService = (window as any).yamlService;
    this.cheAPI.getDevfile().fetchDevfileSchema().then(jsonSchema => {
      const schemas = [{
        uri: 'inmemory:yaml',
        fileMatch: ['*'],
        schema: jsonSchema
      }];
      yamlService.configure({
        validate: true,
        schemas,
        hover: true,
        completion: true,
      });
    });
  }

  /**
   * Callback when editor content is changed.
   */
  onChange(): void {
    if (!this.isActive) {
      return;
    }

    if (this.saveTimeoutPromise) {
      this.$timeout.cancel(this.saveTimeoutPromise);
    }

    this.saveTimeoutPromise = this.$timeout(() => {
      if (!this.editorState.isValid) {
        return;
      }

      const devfile =  jsyaml.safeLoad(this.devfileYaml);
      Object.keys(this.workspaceDevfile).forEach((key: string) => {
        if (!devfile[key]) {
          delete this.workspaceDevfile[key];
        }
      });
      angular.extend(this.workspaceDevfile, devfile);
      this.workspaceDevfileOnChange({devfile});
    }, 500);
  }

}
