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

export class CustomAsyncValidator implements ng.IDirective {
  restrict = 'A';
  require = 'ngModel';

  /**
   * Check that the name of workspace is unique
   */
  link($scope: ng.IScope, element: ng.IAugmentedJQuery, attributes: ng.IAttributes, ngModel: any) {

    // validate only input element
    if ('input' === element[0].localName) {

      ngModel.$asyncValidators.customAsyncValidator = (modelValue: string) => {
        // parent scope ?
        let scopingTest = $scope.$parent;
        if (!scopingTest) {
          scopingTest = $scope;
        }

        return scopingTest.$eval((<any>attributes).customAsyncValidator, {$value: modelValue});
      };
    }
  }

}
