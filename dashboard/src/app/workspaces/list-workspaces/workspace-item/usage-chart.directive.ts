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

interface IUsageChartScope extends ng.IScope {
  usedColor: string;
  loaded: boolean;
  provided: number;
  used: number;
  config: any;
  data: any;
  large: boolean;
}

/**
 * Defines a directive for displaying usage of resource: chart + description.
 * @author Ann Shumilova
 */
export class UsageChart implements ng.IDirective {
  restrict: string;
  templateUrl: string;
  replace: boolean;

  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   */
    constructor () {
    this.restrict = 'E';
    this.templateUrl = 'app/workspaces/list-workspaces/workspace-item/usage-chart.html';
    this.replace = true;

    this.scope = {
      label: '@cheLabel',
      used: '@cheUsed',
      usedDescription: '@cheUsedDescription',
      usedColor: '@cheUsedColor',
      provided: '@cheProvided',
      providedDescription: '@cheProvidedDescription',
      large: '@cheLarge'
    };

  }

  link($scope: IUsageChartScope, $element: ng.IAugmentedJQuery, $attrs: ng.IAttributes): void {
    if ($scope.usedColor) {
      $element.find('.usage-chart-used-value').css('color', $scope.usedColor);
      $element.find('.usage-chart-label').css('color', $scope.usedColor);
    }

    $scope.$watch(function () {
      return $element.is(':visible');
    }, function () {
      if ($element.is(':visible')) {
        $scope.loaded = true;
      }
    });

    let t = this;
    $attrs.$observe('cheUsed', function () {
      if ($scope.used && $scope.provided) {
        t.initChart($scope);
      }
    });

    $attrs.$observe('cheProvided', function () {
      if ($scope.used && $scope.provided) {
        t.initChart($scope);
      }
    });

  }

  initChart($scope: IUsageChartScope): void {
    let available = $scope.provided - $scope.used;
    let usedPercents = ($scope.used * 100 / $scope.provided).toFixed(0);
    let availablePercents = 100 - parseInt(usedPercents, 10);
    let usedColor = $scope.usedColor ? $scope.usedColor : '#4e5a96';

    $scope.config = {
      tooltips: true,
      labels: false,
      // mouseover: () => {},
      // mouseout: () => {},
      // click: () => {},
      legend: {
        display: false,
        position: 'right'
      },
      innerRadius: $scope.large ? '50%' : '75%',
      colors: [usedColor, '#d4d4d4']
    };

    $scope.data = {
      data: [{
        x: 'Used',
        y: [$scope.used],
        tooltip: 'Used (' + usedPercents + '%)'
      }, {
        x: 'Available',
        y: [available],
        tooltip: 'Available (' + availablePercents + '%)'
      }]
    };
  }
}

