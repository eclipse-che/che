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
 * @ngdoc controller
 * @name projects.list.controller:ProjectItemCtrl
 * @description This class is handling the controller for item of project list
 * @author Florent Benoit
 */
export class ProjectItemCtrl {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($location, cheWorkspace) {
    this.$location = $location;
    this.cheWorkspace = cheWorkspace;
  }

  redirectToProjectDetails() {
    this.$location.path('/project/' + this.workspace.namespace + '/' + this.workspace.config.name + '/' + this.project.name);
  }

}
