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
 * Defines a directive for creating project from configuration file.
 * @author Florent Benoit
 */
export class CreateProjectConfFile {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($filter) {
    this.$filter = $filter;
    this.restrict='E';
    this.templateUrl = 'app/projects/create-project/config-file/create-project-conf-file.html';
  }


  /**
   * watch data and update json content each time the value is updated
   * @param $scope
   */
  link($scope) {

    // Watch data of the createProject controller and update content with these parameters
    $scope.$watch('createProjectCtrl.importProjectData', (newValue) => {
      $scope.createProjectCtrl.jsonConfig.content = this.$filter('json')(angular.fromJson(newValue), 2);
    }, true);

  }

}
