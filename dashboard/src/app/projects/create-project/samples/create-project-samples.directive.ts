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
 * Defines a directive for creating project from samples.
 * @author Florent Benoit
 */
export class CreateProjectSamples {
  $timeout: ng.ITimeoutService;
  bindToController: boolean;
  restrict: string;
  controller: string;
  templateUrl: string;
  controllerAs: string;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($timeout: ng.ITimeoutService) {
    this.$timeout = $timeout;
    this.restrict = 'E';
    this.templateUrl = 'app/projects/create-project/samples/create-project-samples.html';


    this.controller = 'CreateProjectSamplesController';
    this.controllerAs = 'createProjectSamplesController';
    this.bindToController = true;
  }
}
