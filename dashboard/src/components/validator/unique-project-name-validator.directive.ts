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
 * Defines a directive for checking if the project name is not already taken
 * @author Florent Benoit
 */
export class UniqueProjectNameValidator {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor (cheAPI, $q) {
    this.cheAPI = cheAPI;
    this.$q = $q;
    this.restrict='A';
    this.require = 'ngModel';
  }

  /**
   * Check that the GIT URL is compliant
   */
  link($scope, element, attributes, ngModel) {

    // validate only input element
    if ('input' === element[0].localName) {

      ngModel.$asyncValidators.uniqueProjectName = (modelValue) => {

        // create promise
        var deferred = this.$q.defer();

        // parent scope ?
        var scopingTest = $scope.$parent;
        if (!scopingTest) {
          scopingTest = $scope;
        }

        var workspaceProjects = scopingTest.$eval(attributes.uniqueProjectName);

        // found a selected workspace ?
        if (workspaceProjects) {
          // check if project is there
          for (let i = 0; i < workspaceProjects.length; i++) {
            let project = workspaceProjects[i];
            if (modelValue === project.name) {
              // project there so already exists, return false
              deferred.reject(false);
            }

          }
          deferred.resolve(true);
        } else {
          // no workspace so it's ok
          deferred.resolve(true);
        }

        // return promise
        return deferred.promise;
      };
    }
  }


}
