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
 * Defines a directive for displaying usage of resource: chart + description.
 * @author Ann Shumilova
 */
export class UsageChart {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
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

  link($scope, element, attrs) {
    if ($scope.usedColor) {
      element.find('.usage-chart-used-value').css('color', $scope.usedColor);
      element.find('.usage-chart-label').css('color', $scope.usedColor);
    }

    $scope.$watch(function () {
      return element.is(':visible');
    }, function () {
      if (element.is(':visible')) {
        $scope.loaded = true;
      }
    });

    var t = this;
    attrs.$observe('cheUsed', function () {
      if ($scope.used && $scope.provided) {
        t.initChart($scope);
      }
    });

    attrs.$observe('cheProvided', function () {
      if ($scope.used && $scope.provided) {
        t.initChart($scope);
      }
    });

  }

  initChart($scope) {
    let available = $scope.provided - $scope.used;
    let usedPercents = ($scope.used * 100 / $scope.provided).toFixed(0);
    let availablePercents = 100 - usedPercents;
    let usedColor = $scope.usedColor ? $scope.usedColor : '#4e5a96';

    $scope.config = {
      tooltips: true,
      labels: false,
      mouseover: function() {},
      mouseout: function() {},
      click: function() {},
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

