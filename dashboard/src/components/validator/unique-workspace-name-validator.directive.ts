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

interface IUniqueWorkspaceNameValidatorAttributes extends ng.IAttributes {
  uniqueWorkspaceName: string;
}

/**
 * Defines a directive for checking if the workspace name is not already taken
 * @author Oleksii Kurinnyi
 */
export class UniqueWorkspaceNameValidator implements ng.IDirective {

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
   * Check that the name of workspace is unique
   */
  link($scope: ng.IScope, $element: ng.IAugmentedJQuery, $attributes: IUniqueWorkspaceNameValidatorAttributes, $ngModelCtrl: ng.INgModelController) {

    // validate only input element
    if ('input' === $element[0].localName) {

      ($ngModelCtrl.$asyncValidators as any).uniqueWorkspaceName = (modelValue: string) => {

        // create promise
        const deferred = this.$q.defer();

        // parent scope ?
        let scopingTest = $scope.$parent;
        if (!scopingTest) {
          scopingTest = $scope;
        }

        let currentWorkspaceName = scopingTest.$eval($attributes.uniqueWorkspaceName),
          workspaces = this.cheAPI.getWorkspace().getWorkspaces();
        if (workspaces.length) {

          for (let i = 0; i < workspaces.length; i++) {
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
