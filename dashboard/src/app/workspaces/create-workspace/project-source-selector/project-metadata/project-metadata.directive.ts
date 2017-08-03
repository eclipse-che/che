/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

/**
 * Defines a directive for the project's metadata.
 *
 * @author Oleksii Kurinnyi
 */
export class ProjectMetadata implements ng.IDirective {
  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/create-workspace/project-source-selector/project-metadata/project-metadata.html';
  replace: boolean = true;

  controller: string = 'ProjectMetadataController';
  controllerAs: string = 'projectMetadataController';

  bindToController: boolean = true;

  scope: {
    [propName: string]: string;
  };

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.scope = {
      origTemplate: '=template',
      templateName: '@'
    };
  }

}
