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
import {CheWorkspace} from '../../../../components/api/workspace/che-workspace.factory';
import {CheBranding} from '../../../../components/branding/che-branding.factory';
import {WorkspacesService} from '../../workspaces.service';


const BLUR_TIMEOUT = 5000;

/**
 * @ngdoc controller
 * @name workspaces.list.controller:WorkspaceItemCtrl
 * @description This class is handling the controller for item of workspace list
 * @author Ann Shumilova
 */
export class WorkspaceItemCtrl {

  static $inject = ['$location', 'lodash', 'cheWorkspace', 'workspacesService', '$timeout', '$document', 'cheBranding', '$sce'];

  $location: ng.ILocationService;
  lodash: any;
  cheWorkspace: CheWorkspace;
  workspacesService: WorkspacesService;
  $document: ng.IDocumentService;
  $timeout: ng.ITimeoutService;

  workspace: che.IWorkspace;
  workspaceName: string;
  workspaceSupportIssues: any;

  private supportedRecipeTypeIssue: any;
  private supportedVersionTypeIssue: any;
  private timeoutPromise: ng.IPromise<any>;

  /**
   * Default constructor that is using resource
   */
  constructor($location: ng.ILocationService,
              lodash: any,
              cheWorkspace: CheWorkspace,
              workspacesService: WorkspacesService,
              $timeout: ng.ITimeoutService,
              $document: ng.IDocumentService,
              cheBranding: CheBranding,
              $sce: ng.ISCEService) {
    this.$location = $location;
    this.lodash = lodash;
    this.cheWorkspace = cheWorkspace;
    this.workspacesService = workspacesService;
    this.$timeout = $timeout;
    this.$document = $document;
    this.workspaceName = this.cheWorkspace.getWorkspaceDataManager().getName(this.workspace);

    this.supportedRecipeTypeIssue = $sce.trustAsHtml('Current infrastructure doesn\'t support this workspace recipe type.');

    this.supportedVersionTypeIssue = $sce.trustAsHtml(`This workspace is using old definition format which is not compatible anymore. 
          Please follow the <a href="${cheBranding.getDocs().workspace}" target="_blank">documentation</a>
          to update the definition of the workspace and benefits from the latest capabilities.`);
  }

  /**
   * Returns `true` if supported.
   *
   * @returns {boolean}
   */
  get isSupported(): boolean {
    if (!this.workspacesService.isSupportedRecipeType(this.workspace)) {
      if (this.workspaceSupportIssues !== this.supportedRecipeTypeIssue) {
        this.workspaceSupportIssues = this.supportedRecipeTypeIssue;
      }

      return false;
    } else if (!this.workspacesService.isSupportedVersion(this.workspace)) {
      if (this.workspaceSupportIssues !== this.supportedVersionTypeIssue) {
        this.workspaceSupportIssues = this.supportedVersionTypeIssue;
      }

      return false;
    } else if (this.workspaceSupportIssues) {
      this.workspaceSupportIssues = undefined;
    }

    return true;
  }

  /**
   * Redirects to workspace details.
   * @param tab {string}
   */
  redirectToWorkspaceDetails(tab?: string): void {
    this.$location.path('/workspace/' + this.workspace.namespace + '/' + this.workspaceName).search({tab: tab ? tab : 'Overview'});
  }

  getDefaultEnvironment(workspace: che.IWorkspace): che.IWorkspaceEnvironment {
    let environments = workspace.config.environments;
    let envName = workspace.config.defaultEnv;
    let defaultEnvironment = environments[envName];
    return defaultEnvironment;
  }

  getMemoryLimit(workspace: che.IWorkspace): string {
    if (!workspace.config && workspace.devfile) {
      return '-';
    }

    let environment = this.getDefaultEnvironment(workspace);
    if (environment) {
      let limits = this.lodash.pluck(environment.machines, 'attributes.memoryLimitBytes');
      let total = 0;
      limits.forEach((limit: number) => {
        if (limit) {
          total += limit / (1024 * 1024);
        }
      });
      return (total > 0) ? Math.round(total) + ' MB' : '-';
    }

    return '-';
  }

  setTemporaryFocus(elementId?: string): void {
    const id = elementId ? elementId : `${this.workspace.id}-item-error`;
    const targetElement = this.$document.find(`#${id}`);
    if (!targetElement) {
      return;
    }
    targetElement.focus();

    this.resetBlurTimeout();
    this.timeoutPromise = this.$timeout(() => {
      targetElement.blur();
    }, BLUR_TIMEOUT);
  }

  resetBlurTimeout(): void {
    if (this.timeoutPromise) {
      this.$timeout.cancel(this.timeoutPromise);
    }
  }

  /**
   * Returns current status of workspace
   * @returns {String}
   */
  getWorkspaceStatus(): string {
    let workspace = this.cheWorkspace.getWorkspaceById(this.workspace.id);
    return workspace ? workspace.status : 'unknown';
  }
}
