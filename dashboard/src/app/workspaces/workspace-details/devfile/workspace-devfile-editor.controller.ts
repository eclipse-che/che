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
    'cheBranding'
  ];
  private $log: ng.ILogService;
  private $scope: ng.IScope;
  private $timeout: ng.ITimeoutService;
  private cheBranding: CheBranding;

  private isActive: boolean;
  private workspaceDevfile: che.IWorkspaceDevfile;
  private workspaceDevfileOnChange: Function;
  private devfileDocsUrl: string;

  private validationErrors: string[] = [];
  private devfileYaml: string;
  private saveTimeoutPromise: ng.IPromise<any>;


  /**
   * Default constructor that is using resource
   */
  constructor(
    $log: ng.ILogService,
    $scope: ng.IScope,
    $timeout: ng.ITimeoutService,
    cheBranding: CheBranding
  ) {
    this.$log = $log;
    this.$scope = $scope;
    this.$timeout = $timeout;
    this.cheBranding = cheBranding;
    this.devfileYaml = jsyaml.dump(this.workspaceDevfile);

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
        angular.extend(devfile, this.workspaceDevfile);
        this.devfileYaml = jsyaml.safeDump(devfile);
        this.validate();
      }
    }, true);
  }

  $onInit(): void {
    this.devfileYaml = jsyaml.safeDump(this.workspaceDevfile);
    this.devfileDocsUrl = this.cheBranding.getDocs().devfile;
  }

  validate() {
    this.validationErrors = [];

    let devfile: che.IWorkspaceDevfile;
    try {
      devfile = jsyaml.safeLoad(this.devfileYaml);
    } catch (e) {
      if (e.name === 'YAMLException') {
        this.validationErrors = [e.message];
      }
      if (this.validationErrors.length === 0) {
        this.validationErrors = ['Devfile is invalid.'];
      }
      this.$log.error(e);
    }
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
      this.validate();
      if (this.validationErrors.length !== 0) {
        return;
      }

      angular.extend(this.workspaceDevfile, jsyaml.safeLoad(this.devfileYaml));
      this.workspaceDevfileOnChange({devfile: this.workspaceDevfile});
    }, 200);
  }

}
