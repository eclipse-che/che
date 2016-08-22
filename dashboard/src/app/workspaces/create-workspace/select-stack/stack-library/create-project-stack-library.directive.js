/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
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
 * @ngdoc directive
 * @name projects.create.directive:createProjectStackLibrary
 * @restrict E
 * @element
 *
 * @description
 * `<create-project-stack-library></create-project-stack-library>` for creating new projects from stack library.
 *
 * @author Florent Benoit
 */
export class CreateProjectStackLibrary {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.restrict = 'E';
    this.templateUrl = 'app/workspaces/create-workspace/select-stack/stack-library/create-project-stack-library.html';

    this.controller = 'CreateProjectStackLibraryController';
    this.controllerAs = 'createProjectStackLibraryCtrl';
    this.bindToController = true;

    // scope values
    this.scope = {};

  }

}
