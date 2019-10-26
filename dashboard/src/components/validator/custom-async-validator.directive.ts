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

interface IModelValidators extends ng.IAsyncModelValidators {
  customAsyncValidator: (modelValue: string) => ng.IPromise<boolean>;
}

interface INgModelController extends ng.INgModelController {
  $asyncValidators: IModelValidators;
}

interface IAttributes extends ng.IAttributes {
  customAsyncValidator: string;
}

/**
 * Defines a directive for custom asynchronous validation
 * @author Oleksii Orel
 */
export class CustomAsyncValidator implements ng.IDirective {
  restrict = 'A';
  require = 'ngModel';

  /**
   * Check that the name of workspace is unique
   */
  link($scope: ng.IScope, element: ng.IAugmentedJQuery, attrs: IAttributes, ctrl: INgModelController) {
    const elementLocalName = element[0].localName;
    // validate only input or textarea elements
    if ('input' !== elementLocalName && 'textarea' !== elementLocalName) {
      return;
    }

    const $testScope = $scope.$parent ? $scope.$parent : $scope;

    ctrl.$asyncValidators.customAsyncValidator = modelValue => {
      return $testScope.$eval(attrs.customAsyncValidator, {$value: modelValue});
    };
  }

}
