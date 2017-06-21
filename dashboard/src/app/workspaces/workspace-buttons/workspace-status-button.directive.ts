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

interface IWorkspaceStatusButtonScope extends ng.IScope {
  isDisabled: boolean;
  isStopButton: boolean;
  isCreationFlow: boolean;
  isAutoSnapshot: boolean;
  workspaceStatus: string;
  runWorkspace: Function;
  stopWorkspace: Function;
  changeWorkspaceStatus: Function;
}

const STARTING = 'STARTING';
const RUNNING = 'RUNNING';
const STOPPING = 'STOPPING';
const SNAPSHOTTING = 'SNAPSHOTTING';

/**
 * @ngdoc directive
 * @name workspace.status.button.directive:cheWorkspaceStatusButton
 * @restrict E
 * @function
 * @element
 *
 * @description
 * `<workspace-status-button>` defines a status-button component
 *
 * @param {string=} workspaceStatus
 * @param {boolean=} isCreationFlow
 * @param {boolean=} isAutoSnapshot
 * @param {Function=} runWorkspace
 * @param {Function=} stopWorkspace
 *
 * @author Oleksii Orel
 */


export class CheWorkspaceStatusButton {
  restrict = 'E';
  templateUrl = 'app/workspaces/workspace-buttons/workspace-status-button.html';

  // scope values
  scope = {
    isCreationFlow: '=',
    isAutoSnapshot: '=',
    workspaceStatus: '=',
    runWorkspace: '&',
    stopWorkspace: '&'
  };

  /**
   * Keep reference to the model controller
   */
  link($scope: IWorkspaceStatusButtonScope) {
    const runStatuses = [STARTING, RUNNING, STOPPING, SNAPSHOTTING];

    const updateButton = (workspaceStatus: string) => {
      if (!workspaceStatus) {
        $scope.isStopButton = false;
      } else {
        $scope.isStopButton = runStatuses.indexOf(workspaceStatus) !== -1;
      }
      $scope.isDisabled = $scope.isCreationFlow || workspaceStatus === STOPPING || workspaceStatus === SNAPSHOTTING;
    };

    updateButton($scope.workspaceStatus);

    $scope.$watch('workspaceStatus',
      (workspaceStatus: string) => {
        updateButton(workspaceStatus);
      });

    $scope.changeWorkspaceStatus = () => {
      if ($scope.isStopButton) {
        $scope.stopWorkspace();
      } else {
        $scope.runWorkspace();
      }
    };
  }
}
