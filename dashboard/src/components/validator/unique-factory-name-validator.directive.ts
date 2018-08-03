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

interface IFactoryNameValidatorAsyncModelValidators extends ng.IAsyncModelValidators {
  uniqueFactoryName: (modelValue: any, viewValue?: any) => ng.IPromise<any>;
}

interface IFactoryNameValidatorAttributes extends ng.IAttributes {
  uniqueFactoryName: string;
}

/**
 * Defines a directive for checking if the factory name is not already taken
 * @author Oleksii Kurinnyi
 */
export class UniqueFactoryNameValidator implements ng.IDirective {

  static $inject = ['cheAPI', '$q'];

  $q: ng.IQService;
  cheAPI: CheAPI;

  restrict: string = 'A';
  require: string = 'ngModel';

  user: che.IUser;

  /**
   * Default constructor that is using resource
   */
  constructor (cheAPI: CheAPI, $q: ng.IQService) {
    this.cheAPI = cheAPI;
    this.$q = $q;

    this.user = this.cheAPI.getUser().getUser();
  }

  /**
   * Check that the name of workspace is unique
   */
  link($scope: ng.IScope, element: ng.IAugmentedJQuery, attributes: IFactoryNameValidatorAttributes, ngModel: ng.INgModelController) {

    const asyncValidators = ngModel.$asyncValidators as IFactoryNameValidatorAsyncModelValidators;

    // validate only input element
    if ('input' === element[0].localName) {

      asyncValidators.uniqueFactoryName = (modelValue: any, viewValue: any) => {

        // create promise
        const deferred = this.$q.defer();

        if (!this.user) {
          deferred.reject(false);
          return deferred.promise;
        }

        // parent scope ?
        let scopingTest = $scope.$parent;
        if (!scopingTest) {
          scopingTest = $scope;
        }

        const currentFactoryName = scopingTest.$eval(attributes.uniqueFactoryName);

        if (!modelValue || modelValue === currentFactoryName) {
          deferred.resolve(true);
        } else if (this.cheAPI.getFactory().getFactoryByName(modelValue, this.user.id)) {
          deferred.reject(false);
        } else {
          this.cheAPI.getFactory().fetchFactoryByName(modelValue, this.user.id).finally(() => {
            if (this.cheAPI.getFactory().getFactoryByName(modelValue, this.user.id)) {
              deferred.reject(false);
            } else {
              deferred.resolve(true);
            }
          });
        }

        // return promise
        return deferred.promise;
      };
    }
  }

}
