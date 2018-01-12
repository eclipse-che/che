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

interface IModelValidators extends ng.IModelValidators {
  customValidator: (modelValue: any) => boolean;
}

interface INgModelController extends ng.INgModelController {
  $validators: IModelValidators;
}

interface IAttributes extends ng.IAttributes {
  customValidator: string;
}

/**
 * Defines a directive for custom validation
 * @author Oleksii Kurinnyi
 */
export class CustomValidator implements ng.IDirective {
  restrict: string = 'A';
  require: string = 'ngModel';

  link($scope: ng.IScope, element: ng.IAugmentedJQuery, attrs: IAttributes, ctrl: INgModelController) {
    const elementLocalName = element[0].localName;
    // validate only input or textarea elements
    if ('input' !== elementLocalName && 'textarea' !== elementLocalName) {
      return;
    }

    const $testScope = $scope.$parent ? $scope.$parent : $scope;

    ctrl.$validators.customValidator = (modelValue: any) => {
      return $testScope.$eval(attrs.customValidator, {$value: modelValue});
    };
  }
}

