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

interface ICheFormatOutputScope extends ng.IScope {
  ngModel: string;
}

/**
 * Defines a directive for formatting output.
 * @author Ann Shumilova
 */
export class CheFormatOutput implements ng.IDirective {

  static $inject = ['jsonOutputColors', '$compile'];

  restrict = 'A';
  require = ['ngModel'];
  outputColors: any;
  $compile: ng.ICompileService;
  scope = {
    ngModel: '='
  };

  /**
   * Default constructor that is using resource
   */
  constructor(jsonOutputColors: string,
              $compile: ng.ICompileService) {
    this.outputColors = angular.fromJson(jsonOutputColors);
    this.$compile = $compile;
  }

  /**
   * Keep reference to the model controller
   */
  link($scope: ICheFormatOutputScope, $element: ng.IAugmentedJQuery): void {
    $scope.$watch(() => {
        return $scope.ngModel;
      }, (value: string) => {
      if (!value || value.length === 0) {
        return;
      }

      let regExp = new RegExp('\n', 'g');
      let result = value.replace(regExp, '<br/>');

      this.outputColors.forEach((outputColor: any) => {
        regExp = new RegExp('\\[\\s*' + outputColor.type + '\\s*\\]', 'g');
        result = result.replace(regExp, '[<span style=\"color: ' + outputColor.color + '\">' + outputColor.type + '</span>]');
      });

      result = '<span>' + result + '</span>';

      $element.html(this.$compile(result)($scope).html());
    });
  }
}

