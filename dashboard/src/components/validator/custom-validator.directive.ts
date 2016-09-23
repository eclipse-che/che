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
 * Defines a directive for custom validation
 * @author Oleksii Kurinnyi
 */
export class CustomValidator {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {

    this.restrict = 'A';
    this.require = 'ngModel';
  }

  link($scope, element, attrs, ctrl) {
    // validate only input element
    if ('input' === element[0].localName) {

      let $testScope = $scope.$parent ? $scope.$parent : $scope;

      ctrl.$validators.customValidator = (modelValue) => {
        return $testScope.$eval(attrs.customValidator, {$value: modelValue});
      }
    }
  }
}

