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
 * Defines a directive for Logs output
 * @author Oleksii Kurinnyi
 */
export class CheLogsOutput {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($timeout) {
    this.$timeout = $timeout;
    this.restrict = 'E';
    this.templateUrl = 'components/widget/logs-output/che-logs-output.html';

    // scope values
    this.scope = {
      title: '@cheTitle',
      rawText: '=cheText',
      scrollback: '@?cheScrollback',
      refreshTime: '@?cheRefreshTime'
    };
  }

  link ($scope, element) {
    if (isNaN(parseInt($scope.scrollback,10))) {
      $scope.scrollback = 100;
    }
    if (isNaN(parseInt($scope.refreshTime,10))) {
      $scope.refreshTime = 500;
    }

    let timeoutPromise,
      wait = false,
      updateNeeded = false;
    $scope.$watch(() => {return $scope.rawText}, () => {
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

    let processText = () => {
      if (!$scope.rawText || !$scope.rawText.length){
        return;
      }
      let lines = $scope.rawText.split(/\n/);
      let removeUntilIdx = lines.length - $scope.scrollback;
      if (removeUntilIdx > 0) {
        lines.splice(0, removeUntilIdx);
        $scope.text = lines.join('\n');
        $scope.text = removeUntilIdx + ' lines are hidden...\n\n' + $scope.text;
      }
      else {
        $scope.text = $scope.rawText;
      }
    };
  }
}
