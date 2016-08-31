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
 * Defines a directive for creating project from git.
 * @author Florent Benoit
 */
export class CreateProjectGit {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {

    this.controller = 'CreateProjectGitController';
    this.controllerAs = 'createProjectGitCtrl';
    this.bindToController = true;

    this.restrict = 'E';
    this.templateUrl = 'app/projects/create-project/git/create-project-git.html';
  }


  /**
   * Watch the data and update project name if location is updated
   * @param $scope
   */
  link($scope) {

    // Watch data of the createProject controller and update project name
    $scope.$watch('createProjectCtrl.importProjectData', (newValue) => {


      if ('git' !== $scope.createProjectCtrl.currentTab) {
        return;
      }

      // no focus, abort
      if (!$scope.createProjectGitCtrl.focus) {
        return;
      }

      // get current url
      var uri = newValue.source.location;

      if (!uri || uri === '') {
        return;
      }

      // search if repository is ending with . (for example .git) or the last name
      var indexFinishProjectName = uri.lastIndexOf('.');
      var indexStartProjectName;
      if (uri.lastIndexOf('/') !== -1) {
        indexStartProjectName = uri.lastIndexOf('/') + 1;
      } else {
        indexStartProjectName = uri.lastIndexOf(':') + 1;
      }


      var name;

      // extract name with .../dummy.git
      if (indexStartProjectName !== 0 && indexStartProjectName < indexFinishProjectName) {
        name = uri.substring(indexStartProjectName, indexFinishProjectName);
      } else if (indexStartProjectName !== 0) {
        // extract ...../dummy
        name = uri.substring(indexStartProjectName);
      } else {
        // unable to do something
        name = '';
      }

      // able to extract something, change it
      if (name !== '') {
        $scope.createProjectCtrl.importProjectData.project.name = name;
      }

    }, true);


  }
}
