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
 * @ngDoc directive
 * @name workspace.warnings.directive:WorkspaceWarnings
 * @description This class is handling the directive for the container with warnings
 * @author Ann Shumilova
 */
export class WorkspaceWarnings implements ng.IDirective {
  restrict: string;
  bindToController: boolean;
  templateUrl: string;
  controller: string;
  controllerAs: string;
  transclude: boolean;
  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   */
  constructor() {
    this.restrict = 'E';
    this.bindToController = true;
    this.templateUrl = 'app/workspaces/workspace-details/warnings/workspace-warnings.html';
    this.controller = 'WorkspaceWarningsController';
    this.controllerAs = 'workspaceWarningsController';

    this.transclude = true;
    this.scope = {
      workspace: '=workspace'
    };
  }

}
