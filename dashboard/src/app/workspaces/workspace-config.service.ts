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

import {CheWorkspace} from '../../components/api/workspace/che-workspace.factory';
import {NamespaceSelectorSvc} from './create-workspace/namespace-selector/namespace-selector.service';
import {CreateWorkspaceSvc} from './create-workspace/create-workspace.service';
import {StackSelectorSvc} from './create-workspace/stack-selector/stack-selector.service';
import {TemplateSelectorSvc} from './create-workspace/project-source-selector/add-import-project/template-selector/template-selector.service';
import {ImportGithubProjectService} from './create-workspace/project-source-selector/add-import-project/import-github-project/import-github-project.service';

/**
 * This class is handling the service for routes resolving.
 *
 * @author Oleksii Kurinnyi
 */
export class WorkspaceConfigService {

  static $inject = ['$log', '$q', 'cheWorkspace', 'namespaceSelectorSvc', 'createWorkspaceSvc', 'stackSelectorSvc', 'templateSelectorSvc', 'importGithubProjectService'];

  /**
   * Log service.
   */
  private $log: ng.ILogService;
  /**
   * Promises service.
   */
  private $q: ng.IQService;
  /**
   * Workspace API interaction.
   */
  private cheWorkspace: CheWorkspace;
  /**
   * Namespace selector service.
   */
  private namespaceSelectorSvc: NamespaceSelectorSvc;
  /**
   * Workspace creating service.
   */
  private createWorkspaceSvc: CreateWorkspaceSvc;
  /**
   * Stack selector service.
   */
  private stackSelectorSvc: StackSelectorSvc;
  /**
   * Template selector service.
   */
  private templateSelectorSvc: TemplateSelectorSvc;
  /**
   * Import GitHub project service.
   */
  private importGithubProjectService: ImportGithubProjectService;

  /** Default constructor that is using resource injection
   */
  constructor($log: ng.ILogService, $q: ng.IQService, cheWorkspace: CheWorkspace, namespaceSelectorSvc: NamespaceSelectorSvc, createWorkspaceSvc: CreateWorkspaceSvc,
     stackSelectorSvc: StackSelectorSvc, templateSelectorSvc: TemplateSelectorSvc, importGithubProjectService: ImportGithubProjectService) {
    this.$log = $log;
    this.$q = $q;
    this.cheWorkspace = cheWorkspace;
    this.namespaceSelectorSvc = namespaceSelectorSvc;
    this.createWorkspaceSvc = createWorkspaceSvc;
    this.stackSelectorSvc = stackSelectorSvc;
    this.templateSelectorSvc = templateSelectorSvc;
    this.importGithubProjectService = importGithubProjectService;
  }

  /**
   * Returns promise to resolve route for workspace creation page.
   *
   * @return {ng.IPromise<any>}
   */
  resolveWorkspaceRoute(): ng.IPromise<any> {
    const namespaceIdDefer = this.$q.defer(),
          workspacesDefer = this.$q.defer();

    // resolve namespace ID, workspaces in namespace
    this.namespaceSelectorSvc.fetchNamespaces().then((namespaceId: string) => {
      namespaceIdDefer.resolve(namespaceId);

      if (!namespaceId) {
        return this.$q.reject();
      }

      return this.createWorkspaceSvc.fetchWorkspacesByNamespace(namespaceId);
    }).then((workspaces: Array<che.IWorkspace>) => {
      workspacesDefer.resolve(workspaces);
    }, (error: any) => {
      this.logError(error);
      workspacesDefer.resolve([]);
    });

    // resolve GitHub repositories
    const githubRepositoriesPromise = this.$q.all([this.importGithubProjectService.getOrFetchUserId(), this.importGithubProjectService.getOrFetchOAuthProvider()]).then(() => {
      return this.importGithubProjectService.askLoad();
    });

    return this.$q.all([
      namespaceIdDefer.promise,
      workspacesDefer.promise,
      this.stackSelectorSvc.getOrFetchStacks(),
      this.templateSelectorSvc.getOrFetchTemplates(),
      githubRepositoriesPromise
    ]);
  }

  /**
   * Prints error message.
   *
   * @param {any} error error object or string
   */
  private logError(error: any): void {
    if (!error) {
      return;
    }
    const message = error.data && error.data.message ? error.data.message : error;
    this.$log.error(message);
  }
}
