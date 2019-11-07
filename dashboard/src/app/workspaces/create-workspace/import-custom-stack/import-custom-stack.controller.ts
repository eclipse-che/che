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
import {IImportStackScopeBindings} from './import-custom-stack.directive';
import {YAML, URL} from './devfile-source-selector/devfile-source-selector.directive';
import {CreateWorkspaceSvc} from '../create-workspace.service';
import {RandomSvc} from '../../../../components/utils/random.service';
import {NamespaceSelectorSvc} from '../ready-to-go-stacks/namespace-selector/namespace-selector.service';

/**
 * This class is handling the controller for stack importing directive.
 *
 * @author Oleksii Orel
 */
export class ImportStackController implements IImportStackScopeBindings {

  static $inject = ['namespaceSelectorSvc', 'createWorkspaceSvc', 'randomSvc'];

  onChange: (eventData: { devfile: che.IWorkspaceDevfile, attrs?: { [key: string]: any } }) => void;

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
   * The imported devfile location(URL).
   */
  private devfileLocation: string;
  /**
   * The imported devfile(YAML).
   */
  private devfile: che.IWorkspaceDevfile;

  /**
   * Default constructor that is using resource injection
   */
  constructor(namespaceSelectorSvc: NamespaceSelectorSvc, createWorkspaceSvc: CreateWorkspaceSvc, randomSvc: RandomSvc) {
    this.namespaceSelectorSvc = namespaceSelectorSvc;
    this.createWorkspaceSvc = createWorkspaceSvc;
    this.randomSvc = randomSvc;
  }

  $onInit(): void {
  }

  private initializeMinDevfile() {
    this.devfile = {
      apiVersion: '1.0.0',
      components: [],
      projects: [],
      metadata: {
        name: 'wksp-custom'
      }
    };

    if (this.devfile) {
      const prefix = `${this.devfile.metadata.name}-`;
      const namespaceId = this.namespaceSelectorSvc.getNamespaceId();
      this.createWorkspaceSvc.buildListOfUsedNames(namespaceId).then((list: string[]) => {
        this.devfile.metadata.name = this.randomSvc.getRandString({prefix, list});
      });
    }
  }

  updateDevfile(devfile: che.IWorkspaceDevfile, attrs: { factoryurl?: string } = {}): void {
    if (this.isYamlSelected()) {
      this.devfile = devfile;
    } else if (attrs.factoryurl) {
      this.devfileLocation = attrs.factoryurl;
    }
    if (angular.isFunction(this.onChange)) {
      this.onChange({devfile, attrs});
    }
  }

  onSourceChange(source: string): void {
    if (source === YAML && !this.devfile) {
      this.initializeMinDevfile();
    }
  }

  isUrlSelected(): boolean {
    return this.selectedSource === URL;
  }

  isYamlSelected(): boolean {
    return this.selectedSource === YAML;
  }
}
