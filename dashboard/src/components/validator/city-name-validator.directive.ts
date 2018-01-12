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

