/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
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
 * Defines a directive for formatting output.
 * @author Ann Shumilova
 */
export class CheFormatOutput {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(jsonOutputColors, $compile) {
    this.restrict = 'A';
    this.outputColors = angular.fromJson(jsonOutputColors);
    this.$compile = $compile;
  }

  /**
   * Keep reference to the model controller
   */
  link($scope, element, attr) {
    $scope.$watch(attr.ngModel, (value) => {
      if (!value || value.length === 0) {
        return;
      }

      var regExp = new RegExp('\n', 'g');
      var result = value.replace(regExp, '<br/>')

      this.outputColors.forEach((outputColor) => {
        regExp = new RegExp('\\[\\s*' + outputColor.type + '\\s*\\]', 'g');
        result = result.replace(regExp, '[<span style=\"color: ' + outputColor.color + '\">' + outputColor.type + '</span>]')
      });

      result = '<span>' + result + '</span>';
      element.html(this.$compile(result)($scope));
    });
  }
}

