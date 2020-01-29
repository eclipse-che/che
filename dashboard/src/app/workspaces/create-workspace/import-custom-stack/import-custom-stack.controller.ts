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
import { IImportStackScopeBindings, IImportStackScopeOnChange } from './import-custom-stack.directive';
import {YAML, URL} from './devfile-source-selector/devfile-source-selector.directive';
import {CreateWorkspaceSvc} from '../create-workspace.service';
import {RandomSvc} from '../../../../components/utils/random.service';
import {NamespaceSelectorSvc} from '../ready-to-go-stacks/namespace-selector/namespace-selector.service';
import { CheKubernetesNamespace } from '../../../../components/api/che-kubernetes-namespace.factory';

/**
 * This class is handling the controller for stack importing directive.
 *
 * @author Oleksii Orel
 */
export class ImportStackController implements IImportStackScopeBindings {

  static $inject = [
    'cheKubernetesNamespace',
    'createWorkspaceSvc',
    'namespaceSelectorSvc',
    'randomSvc'
  ];

  onChange: IImportStackScopeOnChange;

  infrastructureNamespaceHint: string;

  /**
   * Kubernetes Namespace API interaction.
   */
  private cheKubernetesNamespace: CheKubernetesNamespace;
  /**
   * Namespace selector service.
   */
  private namespaceSelectorSvc: NamespaceSelectorSvc;
  /**
   * Generator for random strings.
   */
  private randomSvc: RandomSvc;
  /**
   * Workspace creation service.
   */
  private createWorkspaceSvc: CreateWorkspaceSvc;
  /**
   * The selected source for devfile importing(URL or YAML).
   */
  private selectedSource: string;
  /**
   * The imported devfile location(URL) to show preview.
   */
  private devfileLocation: string;
  /**
   * The imported YAML content to show in editor.
   */
  private devfileYaml: che.IWorkspaceDevfile;
  /**
   * The devfile to create the workspace.
   */
  private devfile: che.IWorkspaceDevfile;
  /**
   * The selected infrastructure namespace ID.
   */
  private infrastructureNamespaceId: string;
  /**
   * Additional workspace attributes.
   */
  private attrs: {[key: string]: any};

  /**
   * Default constructor that is using resource injection
   */
  constructor(
    cheKubernetesNamespace: CheKubernetesNamespace,
    createWorkspaceSvc: CreateWorkspaceSvc,
    namespaceSelectorSvc: NamespaceSelectorSvc,
    randomSvc: RandomSvc
  ) {
    this.cheKubernetesNamespace = cheKubernetesNamespace;
    this.createWorkspaceSvc = createWorkspaceSvc;
    this.namespaceSelectorSvc = namespaceSelectorSvc;
    this.randomSvc = randomSvc;
  }

  $onInit(): void {
    this.cheKubernetesNamespace.fetchKubernetesNamespace().then(() => this.updateInfrastructureNamespaceHint());
  }

  updateDevfileFromRemote(devfile: che.IWorkspaceDevfile, attrs: { factoryurl?: string } | undefined): void {
    this.devfileLocation = attrs.factoryurl;
    this.devfile = devfile;
    this.attrs = attrs;
    this.propagateChanges();
  }

  updateDevfileFromYaml(devfile: che.IWorkspaceDevfile): void {
    this.devfileYaml = devfile;
    this.devfile = devfile;
    this.attrs = undefined;
    this.propagateChanges();
  }

  onSourceChange(source: string): void {
    if (source === YAML && !this.devfileYaml) {
      this.initializeMinDevfile();
    }
  }

  isUrlSelected(): boolean {
    return this.selectedSource === URL;
  }

  isYamlSelected(): boolean {
    return this.selectedSource === YAML;
  }

  onInfrastructureNamespaceChange(namespaceId: string): void {
    this.infrastructureNamespaceId = namespaceId;
    this.propagateChanges();
  }

  private propagateChanges(): void {
    const opts = {
      devfile: this.devfile,
      attrs: this.attrs,
      infrastructureNamespaceId: this.infrastructureNamespaceId
    };
    this.onChange(opts);
  }

  private initializeMinDevfile() {
    this.devfileYaml = {
      apiVersion: '1.0.0',
      components: [],
      projects: [],
      metadata: {
        name: 'wksp-custom'
      }
    };

    if (this.devfileYaml) {
      const prefix = `${this.devfileYaml.metadata.name}-`;
      const namespaceId = this.namespaceSelectorSvc.getNamespaceId();
      this.createWorkspaceSvc.buildListOfUsedNames(namespaceId).then((list: string[]) => {
        this.devfileYaml.metadata.name = this.randomSvc.getRandString({prefix, list});
      });
    }
  }

  private updateInfrastructureNamespaceHint(): void {
    this.infrastructureNamespaceHint = this.cheKubernetesNamespace.getHintDescription();
  }

}
