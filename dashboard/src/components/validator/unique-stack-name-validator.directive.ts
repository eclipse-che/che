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
 * Defines a directive for checking whether stack name already exists.
 *
 * @author Ann Shumilova
 */
export class UniqueStackNameValidator {

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
   * Check that the name of stack is unique
   */
  link($scope, element, attributes, ngModel) {

    // validate only input element
    if ('input' === element[0].localName) {

      ngModel.$asyncValidators.uniqueStackName = (modelValue) => {

        // create promise
        var deferred = this.$q.defer();

        // parent scope ?
        var scopingTest = $scope.$parent;
        if (!scopingTest) {
          scopingTest = $scope;
        }

        let currentStackName = scopingTest.$eval(attributes.uniqueStackName),
          stacks = this.cheAPI.getStack().getStacks();
        if (stacks.length) {
          for (let i = 0; i < stacks.length; i++) {
            if (stacks[i].name === currentStackName) {
              continue;
            }
            if (stacks[i].name === modelValue) {
              deferred.reject(false);
            }
          }
          deferred.resolve(true);
        } else {
          deferred.resolve(true);
        }
        return deferred.promise;
      };
    }
  }
}
