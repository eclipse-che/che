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

interface ICheLogsOutputScope extends ng.IScope {
  scrollback: any;
  refreshTime: any;
  rawText: string;
  text: string;
}

/**
 * Defines a directive for Logs output
 * @author Oleksii Kurinnyi
 */
export class CheLogsOutput implements ng.IDirective {

  static $inject = ['$timeout'];

  restrict = 'E';
  templateUrl = 'components/widget/logs-output/che-logs-output.html';

  // scope values
  scope = {
    title: '@cheTitle',
    rawText: '=cheText',
    scrollback: '@?cheScrollback',
    refreshTime: '@?cheRefreshTime'
  };

  $timeout: ng.ITimeoutService;

  /**
   * Default constructor that is using resource
   */
  constructor($timeout: ng.ITimeoutService) {
    this.$timeout = $timeout;
  }

  link ($scope: ICheLogsOutputScope) {
    if (isNaN(parseInt($scope.scrollback, 10))) {
      $scope.scrollback = 100;
    }
    if (isNaN(parseInt($scope.refreshTime, 10))) {
      $scope.refreshTime = 500;
    }

    let timeoutPromise,
      wait = false,
      updateNeeded = false;

    let processText = () => {
      if (!$scope.rawText || !$scope.rawText.length) {
        return;
      }
      let lines = $scope.rawText.split(/\n/);
      let removeUntilIdx = lines.length - $scope.scrollback;
      if (removeUntilIdx > 0) {
        lines.splice(0, removeUntilIdx);
        $scope.text = lines.join('\n');
        $scope.text = removeUntilIdx + ' lines are hidden...\n\n' + $scope.text;
      } else {
        $scope.text = $scope.rawText;
      }
    };

    $scope.$watch(() => { return $scope.rawText; }, () => {
      if (wait) {
        updateNeeded = true;
        return;
      }

      processText();
      wait = true;

      timeoutPromise = this.$timeout(() => {
        if (updateNeeded) {
          processText();
        }
        wait = false;
      }, $scope.refreshTime);
    });
    $scope.$on('$destroy', () => {
      this.$timeout.cancel(timeoutPromise);
    });
  }
}
