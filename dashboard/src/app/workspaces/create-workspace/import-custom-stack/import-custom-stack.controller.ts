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
import { CheWorkspace } from '../../../../components/api/workspace/che-workspace.factory';
import { CheDashboardConfigurationService } from '../../../../components/branding/che-dashboard-configuration.service';
import { TogglableFeature } from '../../../../components/branding/branding.constant';

/**
 * This class is handling the controller for stack importing directive.
 *
 * @author Oleksii Orel
 */
export class ImportStackController implements IImportStackScopeBindings {

  static $inject = [
    'cheDashboardConfigurationService',
    'cheKubernetesNamespace',
    'cheWorkspace',
    'createWorkspaceSvc',
    'namespaceSelectorSvc',
    'randomSvc',
  ];

  onChange: IImportStackScopeOnChange;
  infrastructureNamespaceHint: string;
  ephemeralMode: boolean;
  enabledKubernetesNamespaceSelector: boolean = false;

  /**
   * Kubernetes Namespace API interaction.
   */
  private cheKubernetesNamespace: CheKubernetesNamespace;
  /**
   * Workspace API interaction.
   */
  private cheWorkspace: CheWorkspace;
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
   * Dashboard configuration service.
   */
  private cheDashboardConfigurationService: CheDashboardConfigurationService;
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
    cheDashboardConfigurationService: CheDashboardConfigurationService,
    cheKubernetesNamespace: CheKubernetesNamespace,
    cheWorkspace: CheWorkspace,
    createWorkspaceSvc: CreateWorkspaceSvc,
    namespaceSelectorSvc: NamespaceSelectorSvc,
    randomSvc: RandomSvc,
  ) {
    this.cheDashboardConfigurationService = cheDashboardConfigurationService;
    this.cheKubernetesNamespace = cheKubernetesNamespace;
    this.cheWorkspace = cheWorkspace;
    this.createWorkspaceSvc = createWorkspaceSvc;
    this.namespaceSelectorSvc = namespaceSelectorSvc;
    this.randomSvc = randomSvc;
  }

  $onInit(): void {
    this.cheKubernetesNamespace.fetchKubernetesNamespace().then(() => this.setInfrastructureNamespaceHint());
    this.cheWorkspace.fetchWorkspaceSettings().then((settings: che.IWorkspaceSettings) => {
      this.ephemeralMode = settings['che.workspace.persist_volumes.default'] === 'false';
    });
    this.enabledKubernetesNamespaceSelector = this.cheDashboardConfigurationService.enabledFeature(TogglableFeature.KUBERNETES_NAMESPACE_SELECTOR);
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
    this.ephemeralMode = this.devfile && this.devfile.attributes && this.devfile.attributes.persistVolumes === 'false' ? true : false;
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

  onEphemeralModeChange(): void {
    this.propagateChanges();
  }

  private updatePersistVolumeAttribute(): void {
    if (!this.devfile) {
      return;
    }
    if (this.ephemeralMode) {
      if (!this.devfile.attributes) {
        this.devfile.attributes = {};
      }
      this.devfile.attributes.persistVolumes = 'false';
    } else {
      if (this.devfile.attributes) {
        delete this.devfile.attributes.persistVolumes;
      }
      if (this.devfile.attributes && Object.keys(this.devfile.attributes).length === 0) {
        delete this.devfile.attributes;
      }
    }
  }

  private propagateChanges(): void {
    this.updatePersistVolumeAttribute();
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

  private setInfrastructureNamespaceHint(): void {
    this.infrastructureNamespaceHint = this.cheKubernetesNamespace.getHintDescription();
  }

}
