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
 * Defines a directive for checking if the workspace name is not already taken
 * @author Oleksii Kurinnyi
 */
export class UniqueWorkspaceNameValidator {

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
   * Check that the name of workspace is unique
   */
  link($scope, element, attributes, ngModel) {

    // validate only input element
    if ('input' === element[0].localName) {

      ngModel.$asyncValidators.uniqueWorkspaceName = (modelValue) => {

        // create promise
        var deferred = this.$q.defer();

        // parent scope ?
        var scopingTest = $scope.$parent;
        if (!scopingTest) {
          scopingTest = $scope;
        }

        let currentWorkspaceName = scopingTest.$eval(attributes.uniqueWorkspaceName),
          workspaces = this.cheAPI.getWorkspace().getWorkspaces();
        if (workspaces.length) {

          for (let i=0; i<workspaces.length; i++) {
            if (workspaces[i].config.name === currentWorkspaceName) {
              continue;
            }
            if (workspaces[i].config.name === modelValue) {
              deferred.reject(false);
            }
          }
          deferred.resolve(true);
        } else {
          // no workspaces so it's ok
          deferred.resolve(true);
        }

        // return promise
        return deferred.promise;
      };
    }
  }


}
