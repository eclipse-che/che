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
 * Defines a directive in html element, which value will be self compiled.
 *
 * @author Ann Shumilova
 */
export class CheCompile {
  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($compile) {
    this.restrict='A';

    this.$compile = $compile;
  }

  link($scope, element, attrs) {
    $scope.$watch(attrs.cheCompile, (value) => {
      element.html(value);
      this.$compile(element.contents())($scope);
    });

  }
}
