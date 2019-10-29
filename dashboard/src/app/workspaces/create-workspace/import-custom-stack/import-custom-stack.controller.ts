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
import {IImportStackScopeBindings} from './import-custom-stack.directive';
import {YAML, URL} from './devfile-source-selector/devfile-source-selector.directive';

/**
 * This class is handling the controller for the import stack directive
 *
 * @author Oleksii Orel
 */
export class ImportStackController implements IImportStackScopeBindings {

  static $inject = [];

  onChange: (eventData: { devfile: che.IWorkspaceDevfile, attrs?: { [key: string]: any } }) => void;

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
  constructor() {
  }

  $onInit(): void {
  }

  private initializeMinDevfile() {
    this.devfile = {
      apiVersion: '1.0.0',
      components: [],
      projects: [],
      metadata: {
        name: 'custom-wksp'
      }
    };
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
