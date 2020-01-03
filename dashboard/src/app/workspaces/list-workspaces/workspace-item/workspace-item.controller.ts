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
import { WorkspaceDataManager } from '../../../../components/api/workspace/workspace-data-manager';

const BLUR_TIMEOUT = 5000;

/**
 * @ngdoc controller
 * @name workspaces.list.controller:WorkspaceItemCtrl
 * @description This class is handling the controller for item of workspace list
 * @author Ann Shumilova
 */
export class WorkspaceItemCtrl {

  static $inject = [
    '$document',
    '$location',
    '$sce',
    '$timeout',
    'cheBranding',
    'cheWorkspace',
    'lodash',
    'workspacesService',
  ];

  $document: ng.IDocumentService;
  $location: ng.ILocationService;
  $timeout: ng.ITimeoutService;
  cheWorkspace: CheWorkspace;
  lodash: any;
  workspacesService: WorkspacesService;

  workspaceDataManager: WorkspaceDataManager;
  workspace: che.IWorkspace;
  workspaceName: string;
  workspaceSupportIssues: any;

  private supportedVersionTypeIssue: any;
  private timeoutPromise: ng.IPromise<any>;

  /**
   * Default constructor that is using resource
   */
  constructor(
    $document: ng.IDocumentService,
    $location: ng.ILocationService,
    $sce: ng.ISCEService,
    $timeout: ng.ITimeoutService,
    cheBranding: CheBranding,
    cheWorkspace: CheWorkspace,
    lodash: any,
    workspacesService: WorkspacesService,
  ) {
    this.$document = $document;
    this.$location = $location;
    this.$timeout = $timeout;
    this.cheWorkspace = cheWorkspace;
    this.lodash = lodash;
    this.workspacesService = workspacesService;

    this.workspaceDataManager = new WorkspaceDataManager();

    this.supportedVersionTypeIssue = $sce.trustAsHtml(`This workspace is using old definition format which is not compatible anymore.
          Please follow the <a href="${cheBranding.getDocs().converting}" target="_blank">documentation</a>
          to update the definition of the workspace and benefits from the latest capabilities.`);
  }

  $onInit(): void {
    this.workspaceName = this.cheWorkspace.getWorkspaceDataManager().getName(this.workspace);
  }

  get stackDescription(): string {
    const attributes = this.workspace.attributes;
    let description = attributes.stackId ? attributes.stackId : attributes.stackName;
    if (!description) {
      description = attributes.factoryId ? attributes.factoryId : attributes.factoryurl;
    }
    return description;
  }

  /**
   * Returns workspace projects.
   *
   * @returns {Array<che.IProject>}
   */
  get projects(): Array<che.IProject> {
    return this.workspaceDataManager.getProjects(this.workspace);
  }

  /**
   * Returns `true` if supported.
   *
   * @returns {boolean}
   */
  get isSupported(): boolean {
    if (!this.workspacesService.isSupported(this.workspace)) {
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

  getMemoryLimit(workspace: che.IWorkspace): string {
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
