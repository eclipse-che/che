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

import { CreateWorkspaceSvc } from '../create-workspace.service';
import { NamespaceSelectorSvc } from './namespace-selector/namespace-selector.service';
import { RandomSvc } from '../../../../components/utils/random.service';
import { IReadyToGoStacksScopeBindings, IReadyToGoStacksScopeOnChange } from './ready-to-go-stacks.directive';
import { ProjectSourceSelectorService } from './project-source-selector/project-source-selector.service';
import { CheKubernetesNamespace } from '../../../../components/api/che-kubernetes-namespace.factory';
import { CheWorkspace } from '../../../../components/api/workspace/che-workspace.factory';
import { CheDashboardConfigurationService } from '../../../../components/branding/che-dashboard-configuration.service';
import { TogglableFeature } from '../../../../components/branding/branding.constant';

/**
 * This class is handling the controller for predefined stacks.
 *
 * @author Oleksii Kurinnyi
 */
export class ReadyToGoStacksController implements IReadyToGoStacksScopeBindings {

  static $inject = [
    'cheDashboardConfigurationService',
    'cheKubernetesNamespace',
    'cheWorkspace',
    'createWorkspaceSvc',
    'namespaceSelectorSvc',
    'projectSourceSelectorService',
    'randomSvc',
  ];

  /**
   * Directive scope bindings.
   */
  onChange: IReadyToGoStacksScopeOnChange;
  /**
   * The selected devfile.
   */
  selectedDevfile: che.IWorkspaceDevfile;
  /**
   * The workspace name model.
   */
  workspaceName: string;
  /**
   * Form name
   */
  WORKSPACE_NAME_FORM = 'workspaceName';
  infrastructureNamespaceHint: string = '';
  ephemeralMode: boolean;
  enabledKubernetesNamespaceSelector: boolean = false;

  /**
   * Injected dependencies.
   */
  private cheDashboardConfigurationService: CheDashboardConfigurationService;
  private cheKubernetesNamespace: CheKubernetesNamespace;
  private cheWorkspace: CheWorkspace;
  private createWorkspaceSvc: CreateWorkspaceSvc;
  private namespaceSelectorSvc: NamespaceSelectorSvc;
  private projectSourceSelectorService: ProjectSourceSelectorService;
  private randomSvc: RandomSvc;

  /**
   * The workspace devfile.
   */
  private devfile: che.IWorkspaceDevfile;
  /**
   * The workspace attributes.
   */
  private attrs: { [key: string]: any } = {};
  /**
   * The selected Che namespace ID.
   */
  private cheNamespaceId: string;
  /**
   * The selected Kubernetes namespace ID.
   */
  private infrastructureNamespaceId: string;
  /**
   * The map of forms.
   */
  private forms: Map<string, ng.IFormController>;
  /**
   * The list of names of existing workspaces.
   */
  private usedNamesList: string[];
  /**
   * Selected stack name.
   */
  private stackName: string;
  /**
   * Workspace name provided by user.
   */
  private providedWorkspaceName: string;

  /**
   * Default constructor that is using resource injection
   */
  constructor(
    cheDashboardConfigurationService: CheDashboardConfigurationService,
    cheKubernetesNamespace: CheKubernetesNamespace,
    cheWorkspace: CheWorkspace,
    createWorkspaceSvc: CreateWorkspaceSvc,
    namespaceSelectorSvc: NamespaceSelectorSvc,
    projectSourceSelectorService: ProjectSourceSelectorService,
    randomSvc: RandomSvc,
  ) {
    this.cheDashboardConfigurationService = cheDashboardConfigurationService;
    this.cheKubernetesNamespace = cheKubernetesNamespace;
    this.cheWorkspace = cheWorkspace;
    this.createWorkspaceSvc = createWorkspaceSvc;
    this.namespaceSelectorSvc = namespaceSelectorSvc;
    this.projectSourceSelectorService = projectSourceSelectorService;
    this.randomSvc = randomSvc;

    this.usedNamesList = [];
    this.forms = new Map();
  }

  $onInit(): void {
    this.cheNamespaceId = this.namespaceSelectorSvc.getNamespaceId();
    this.createWorkspaceSvc.buildListOfUsedNames(this.cheNamespaceId).then((namesList: string[]) => {
      this.usedNamesList = namesList;
      this.workspaceName = this.randomSvc.getRandString({ prefix: 'wksp-', list: this.usedNamesList });
      this.providedWorkspaceName = this.workspaceName;
      this.reValidateName();
    });
    this.cheKubernetesNamespace.fetchKubernetesNamespace().then(() => this.setInfrastructureNamespaceHint());
    this.cheWorkspace.fetchWorkspaceSettings().then((settings: che.IWorkspaceSettings) => {
      this.ephemeralMode = settings['che.workspace.persist_volumes.default'] === 'false';
    });
    this.enabledKubernetesNamespaceSelector = this.cheDashboardConfigurationService.enabledFeature(TogglableFeature.KUBERNETES_NAMESPACE_SELECTOR);
  }

  /**
   * Stores forms in list.
   *
   * @param name a name to register form controller.
   * @param form a form controller.
   */
  registerForm(name: string, form: ng.IFormController) {
    this.forms.set(name, form);
  }

  onDevfileNameChange(newName: string): void {
    this.providedWorkspaceName = newName;
    this.propagateChanges();
  }

  /**
   * Returns `false` if workspace name is not unique in the namespace.
   * Only member with 'manageWorkspaces' permission can definitely know whether
   * name is unique or not.
   *
   * @param name workspace name
   */
  isNameUnique(name: string): boolean {
    return this.usedNamesList.indexOf(name) === -1;
  }

  /**
   * Returns a warning message in case if namespace is missed.
   */
  getNamespaceEmptyMessage(): string {
    return this.namespaceSelectorSvc.getNamespaceEmptyMessage();
  }

  /**
   * Returns list of namespaces.
   */
  getNamespaces(): Array<che.INamespace> {
    return this.namespaceSelectorSvc.getNamespaces();
  }

  /**
   * Returns namespaces caption.
   */
  getNamespaceCaption(): string {
    return this.namespaceSelectorSvc.getNamespaceCaption();
  }

  /**
   * Callback which is called when stack is selected.
   */
  onDevfileSelected(devfile: che.IWorkspaceDevfile): void {
    this.selectedDevfile = devfile;
    this.propagateChanges();
  }

  /**
   * Callback which is called when a project template is added, updated or removed.
   */
  onProjectSelectorChange(): void {
    this.propagateChanges();
  }

  /**
   * Callback which is called when Che namespace is selected.
   */
  onCheNamespaceChanged(namespaceId: string) {
    this.cheNamespaceId = namespaceId;

    this.createWorkspaceSvc.buildListOfUsedNames(namespaceId).then((namesList: string[]) => {
      this.usedNamesList = namesList;
      this.reValidateName();
    });
  }

  onInfrastructureNamespaceChanged(namespaceId: string): void {
    this.infrastructureNamespaceId = namespaceId;
    this.propagateChanges();
  }

  onEphemeralModeChange(): void {
    this.propagateChanges();
  }

  private setInfrastructureNamespaceHint(): void {
    this.infrastructureNamespaceHint = this.cheKubernetesNamespace.getHintDescription();
  }

  /**
   * Triggers form validation on Settings tab.
   */
  private reValidateName(): void {
    const form: ng.IFormController = this.forms.get('name');

    if (!form) {
      return;
    }

    ['name', 'deskname'].forEach((inputName: string) => {
      const model = form[inputName] as ng.INgModelController;
      if (model) {
        model.$validate();
      }
    });
  }

  private updateDevfile(): void {
    this.devfile = angular.copy(this.selectedDevfile);
    this.updateDevfileProjects();
    this.updateDevfileMetadataName();
    this.updatePersistVolumesAttribute();
  }

  private updatePersistVolumesAttribute(): void {
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

  private updateDevfileMetadataName(): void {
    if (!this.devfile) {
      return;
    }
    this.devfile.metadata.name = this.providedWorkspaceName;
  }

  /**
   * Populates a devfile with chosen projects
   */
  private updateDevfileProjects() {
    if (!this.devfile) {
      return;
    }

    // projects to add to current devfile
    const projectTemplates = this.projectSourceSelectorService.getProjectTemplates();

    this.devfile.projects = projectTemplates;

    // check if some of added projects are defined in initial devfile
    const projectDefinedInDevfile = projectTemplates.some((template: che.IProjectTemplate) =>
      this.devfile.projects.some((devfileProject: any) => devfileProject.name === template.name)
    );

    // if no projects defined in devfile were added - remove the commands from devfile as well:
    if (projectDefinedInDevfile === false) {
      this.devfile.commands = [];
    }
  }

  private propagateChanges(): void {
    this.updateDevfile();
    this.onChange({
      devfile: this.devfile,
      attrs: {
        stackName: this.stackName,
      },
      infrastructureNamespaceId: this.infrastructureNamespaceId,
    });
  }

}
