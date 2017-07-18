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
 * Defines a directive for the blank project import.
 *
 * @author Oleksii Kurinnyi
 */
export class ImportBlankProject implements ng.IDirective {
  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/create-workspace/project-source-selector/import-blank-project/import-blank-project.html';
  replace: boolean = true;

  controller: string = 'ImportBlankProjectController';
  controllerAs: string = 'importBlankProjectController';

  bindToController: boolean = true;

  scope: {
    [propName: string]: string;
  };

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.scope = {};
  }

}
