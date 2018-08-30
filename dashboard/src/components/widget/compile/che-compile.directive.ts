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
 * Defines a directive in html element, which value will be self compiled.
 *
 * @author Ann Shumilova
 */
export class CheCompile implements ng.IDirective {

  static $inject = ['$compile'];

  restrict = 'A';

  $compile: ng.ICompileService;

  /**
   * Default constructor that is using resource
   */
  constructor ($compile: ng.ICompileService) {
    this.$compile = $compile;
  }

  link($scope: ng.IScope, $element: ng.IAugmentedJQuery, $attrs: ng.IAttributes) {
    $scope.$watch(($attrs as any).cheCompile, (value: string) => {
      $element.html(value);
      this.$compile($element.contents())($scope);
    });

  }
}
