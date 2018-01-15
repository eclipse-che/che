/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
