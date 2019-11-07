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

/**
 * Defines a directive for import devfile from a URL.
 *
 * @author Oleksii Orel
 */
export class DevfileByUrl implements ng.IDirective {
  restrict: string;
  templateUrl: string;
  controller: string;
  controllerAs: string;
  bindToController: boolean;

  scope: {
    [propName: string]: string;
  };

  /**
   * Default constructor that is using resource
   */
  constructor() {
    this.controller = 'DevfileByUrlController';
    this.controllerAs = 'devfileByUrlController';
    this.bindToController = true;

    this.restrict = 'E';
    this.templateUrl = 'app/workspaces/create-workspace/import-custom-stack/devfile-by-url/devfile-by-url.html';

    // scope values
    this.scope = {
      workspaceDevfileLocation: '=',
      workspaceDevfileOnChange: '&'
    };
  }

}
