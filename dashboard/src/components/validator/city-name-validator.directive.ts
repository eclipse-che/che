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

/**
 * Defines a directive for a city name validation.
 *
 * @author Oleksii Kurinnyi
 */
export class CityNameValidator implements ng.IDirective {
  restrict: string = 'A';
  require: string = 'ngModel';

  cityNameRE: RegExp = new RegExp(`^(?:[a-zA-Z\u00A0-\u024F]+(?:\. |-| |'))*[a-zA-Z\u00A0-\u024F]+$`);

  link($scope: ng.IRootScopeService, $element: ng.IAugmentedJQuery, $attrs: ng.IAttributes, $ctrl: ng.INgModelController) {
    // validate only input element
    if ('input' === $element[0].localName) {
      ($ctrl.$validators as any).cityNameValidator = (modelValue: string) => {
        return this.cityNameRE.test(modelValue);
      };
    }
  }
}

