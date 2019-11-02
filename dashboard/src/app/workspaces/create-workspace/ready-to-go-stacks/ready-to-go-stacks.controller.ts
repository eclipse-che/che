/*
 * Copyright (c) 2015-2019 Red Hat, Inc.
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
import { IReadyToGoStacksScopeBindings } from './ready-to-go-stacks.directive';
import { ProjectSourceSelectorService } from './project-source-selector/project-source-selector.service';

const WORKSPACE_NAME_FORM = 'workspaceName';

/**
 * This class is handling the controller for predefined stacks.
 *
 * @author Oleksii Kurinnyi
 */
export class ReadyToGoStacksController implements IReadyToGoStacksScopeBindings {

  static $inject = [
    '$timeout',
    'createWorkspaceSvc',
    'namespaceSelectorSvc',
    'projectSourceSelectorService',
    'randomSvc'
  ];

  /**
   * Directive scope bindings.
   */
  onChange: (eventData: { devfile: che.IWorkspaceDevfile, attrs: { [key: string]: any } }) => void;
  /**
   * The selected devfile.
   */
  selectedDevfile: che.IWorkspaceDevfile;
  /**
   * The workspace name model.
   */
  workspaceName: string;

  /**
   * Injected dependencies.
   */
  private $timeout: ng.ITimeoutService;
  private createWorkspaceSvc: CreateWorkspaceSvc;
  private namespaceSelectorSvc: NamespaceSelectorSvc;
  private projectSourceSelectorService: ProjectSourceSelectorService;
  private randomSvc: RandomSvc;

  /**
   * The selected namespace ID.
   */
  private namespaceId: string;
  /**
   * The map of forms.
   */
  private forms: Map<string, ng.IFormController>;
  /**
   * The list of names of existing workspaces.
   */
  private usedNamesList: string[];
  /**
   * Hide progress loader if <code>true</code>.
   */
  private stackName: string;
  /**
   * Progress loader is hidden if it's `true`.
   */
  private hideLoader: boolean;

  /**
   * Default constructor that is using resource injection
   */
  constructor(
    $timeout: ng.ITimeoutService,
    createWorkspaceSvc: CreateWorkspaceSvc,
    namespaceSelectorSvc: NamespaceSelectorSvc,
    projectSourceSelectorService: ProjectSourceSelectorService,
    randomSvc: RandomSvc
  ) {
    this.$timeout = $timeout;
    this.createWorkspaceSvc = createWorkspaceSvc;
    this.namespaceSelectorSvc = namespaceSelectorSvc;
    this.projectSourceSelectorService = projectSourceSelectorService;
    this.randomSvc = randomSvc;

    this.usedNamesList = [];
    this.forms = new Map();

    this.hideLoader = false;
  }

  $onInit(): void {
    this.namespaceId = this.namespaceSelectorSvc.getNamespaceId();
    this.createWorkspaceSvc.buildListOfUsedNames(this.namespaceId).then((namesList: string[]) => {
      this.usedNamesList = namesList;
      this.workspaceName = this.randomSvc.getRandString({ prefix: 'wksp-', list: this.usedNamesList });
      this.reValidateName();
    });
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
    // tiny timeout for templates selector to be rendered
    this.$timeout(() => {
      this.hideLoader = true;
    }, 10);
    this.selectedDevfile = devfile;
    this.onChange({
      devfile: this.updateDevfileProjects(),
      attrs: { stackName: this.stackName }
    });
  }

  /**
   * Callback which is called when a project template is added, updated or removed.
   */
  onProjectSelectorChange(): void {
    this.onChange({
      devfile: this.updateDevfileProjects(),
      attrs: { stackName: this.stackName }
    });
  }

  /**
   * Populates initial devfile with chosen projects
   */
  updateDevfileProjects(): che.IWorkspaceDevfile {
    // projects to add to current devfile
    const projectTemplates = this.projectSourceSelectorService.getProjectTemplates();

    const devfile = angular.copy(this.selectedDevfile);
    devfile.projects = projectTemplates;

    // check if some of added projects are defined in initial devfile
    const projectDefinedInDevfile = projectTemplates.some((template: che.IProjectTemplate) =>
      devfile.projects.some((devfileProject: any) => devfileProject.name === template.name)
    );

    // if no projects defined in devfile were added - remove the commands from devfile as well:
    if (projectDefinedInDevfile === false) {
      devfile.commands = [];
    }

    return devfile;
  }

  /**
   * Callback which is called when namespace is selected.
   */
  onNamespaceChanged(namespaceId: string) {
    this.namespaceId = namespaceId;

    this.createWorkspaceSvc.buildListOfUsedNames(namespaceId).then((namesList: string[]) => {
      this.usedNamesList = namesList;
      this.reValidateName();
    });
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

}
