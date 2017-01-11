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
 * Defines a directive for creating project from github.
 * @author Florent Benoit
 */
export class CreateProjectGithub {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($http, $q, $window, $location, $browser, $modal, $filter, GitHub, githubPopup, gitHubTokenStore, githubOrganizationNameResolver) {
    this.$http = $http;
    this.$q = $q;
    this.$window = $window;
    this.$location = $location;
    this.$browser = $browser;
    this.$modal = $modal;
    this.$filter = $filter;
    this.GitHub = GitHub;
    this.gitHubTokenStore = gitHubTokenStore;
    this.githubPopup = githubPopup;
    this.githubOrganizationNameResolver = githubOrganizationNameResolver;

    this.controller = 'CreateProjectGithubController';
    this.controllerAs = 'createProjectGithubCtrl';
    this.bindToController = true;

    this.restrict = 'E';
    this.templateUrl = 'app/projects/create-project/github/create-project-github.html';

    // scope values
    this.scope = {
      createProjectGitHubForm: '=cheForm',
      selectedRepository: '=cheRepositoryModel',
      repositorySelectNotify: '&cheRepositorySelect',
      isCurrentTab: '=cheIsCurrentTab'
    };
  }

  link($scope) {
    // Watch data of the createProject controller and update content with these parameters
    $scope.$watch('createProjectCtrl.importProjectData.source.project.location', (newValue) => {
      var matchRepository = this.$filter('filter')($scope.createProjectGithubCtrl.gitHubRepositories, function (repository) {
        return repository.clone_url === newValue;
      });
      if (matchRepository) {
        $scope.createProjectGithubCtrl.selectedRepository = matchRepository[0];
      } else {
        $scope.createProjectGithubCtrl.selectedRepository = undefined;
      }
    });

  }
}
