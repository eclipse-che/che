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
 * TODO
 */
export class ImportStackController implements IImportStackScopeBindings {

  static $inject = [];

  onChange: Function;

  /**
   * The imported devfile.
   */
  private draftDevfile: che.IWorkspaceDevfile;

  private devfile: che.IWorkspaceDevfile;

  private selectedSource: string;

  private attributes: { factoryurl?: string };

  /**
   * Default constructor that is using resource injection
   */
  constructor() {
    this.draftDevfile = {
      apiVersion: '1.0.0',
      components: [],
      projects: [],
      metadata: {
        name: 'custom-wksp'
      }
    };
  }

  $onInit(): void { }

  updateDevfile(devfile: che.IWorkspaceDevfile, attributes: { factoryurl?: string } = {}): void {
    this.devfile = devfile;
    if (angular.isFunction(this.onChange)) {
      this.onChange({devfile, attributes});
    }
  }

  onSourceChange(source: string): void {
     if(source === YAML && this.devfile) {
       this.draftDevfile = angular.copy(this.devfile);
       console.log('>>>>>>>>>>> ImportStackController source', source, this.draftDevfile);
     }
  }

  isUrlSelected(): boolean {
    return this.selectedSource === URL;
  }

  isYamlSelected(): boolean {
    return this.selectedSource === YAML;
  }
}
