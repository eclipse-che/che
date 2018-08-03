/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {CheAPI} from '../api/che-api.factory';

interface IUniqueProjectNameValidatorAttributes extends ng.IAttributes {
  uniqueProjectName: Array<che.IProject>;
}

/**
 * Defines a directive for checking if the project name is not already taken
 * @author Florent Benoit
 */
export class UniqueProjectNameValidator implements ng.IDirective {

  static $inject = ['cheAPI', '$q'];

  restrict = 'A';
  require = 'ngModel';

  cheAPI: CheAPI;
  $q: ng.IQService;

  /**
   * Default constructor that is using resource
   */
  constructor (cheAPI: CheAPI, $q: ng.IQService) {
    this.cheAPI = cheAPI;
    this.$q = $q;
  }

  /**
   * Check that the GIT URL is compliant
   */
  link($scope: ng.IScope, $element: ng.IAugmentedJQuery, $attributes: ng.IAttributes, $ngModelCtrl: ng.INgModelController) {

    // validate only input element
    if ('input' === $element[0].localName) {

      ($ngModelCtrl.$asyncValidators as any).uniqueProjectName = (modelValue: string) => {

        // create promise
        const deferred = this.$q.defer();

        // parent scope ?
        let scopingTest = $scope.$parent;
        if (!scopingTest) {
          scopingTest = $scope;
        }

        const workspaceProjects = scopingTest.$eval(($attributes as any).uniqueProjectName);

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
