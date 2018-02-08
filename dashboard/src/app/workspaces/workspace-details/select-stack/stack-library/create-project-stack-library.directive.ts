/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
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
export class CreateProjectStackLibrary implements ng.IDirective {

  restrict = 'E';
  templateUrl = 'app/workspaces/workspace-details/select-stack/stack-library/create-project-stack-library.html';

  controller = 'CreateProjectStackLibraryController';
  controllerAs = 'createProjectStackLibraryCtrl';
  bindToController = true;

  // scope values
  scope = {
    tabName: '@cheTabName'
  };

}
