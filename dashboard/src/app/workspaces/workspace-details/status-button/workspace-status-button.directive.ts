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
import {CheWorkspace} from '../../../../components/api/che-workspace.factory';

interface IWorkspaceStatusButtonScope extends ng.IScope {
  isDisabled: boolean;
  isStarting: boolean;
  isStopButton: boolean;
  isAutoSnapshot: boolean;
  workspaceStatus: string;
  dropDownSelectPos: number;
  dropDownItems: Array<string>;
  runWorkspace: Function;
  changeWorkspaceStatus: Function;
  onSelect: (dropDownItem: number) => void;
  stopWorkspace: (data: { isCreateSnapshot: boolean }) => void;
}

const STARTING = 'STARTING';
const RUNNING = 'RUNNING';
const STOPPING = 'STOPPING';
const SNAPSHOTTING = 'SNAPSHOTTING';
const STOP_WITH_SNAPSHOT = 'Stop with snapshot';
const STOP_WITHOUT_SNAPSHOT = 'Stop without snapshot';

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
 * @param {Function=} runWorkspace
 * @param {Function=} stopWorkspace
 *
 * @author Oleksii Orel
 */


export class CheWorkspaceStatusButton {
  restrict = 'E';
  templateUrl = 'app/workspaces/workspace-details/status-button/workspace-status-button.html';

  // scope values
  scope = {
    workspaceStatus: '=',
    runWorkspace: '&',
    stopWorkspace: '&'
  };

  private cheWorkspace: CheWorkspace;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(cheWorkspace: CheWorkspace) {
    this.cheWorkspace = cheWorkspace;
  }

  /**
   * Keep reference to the model controller
   */
  link($scope: IWorkspaceStatusButtonScope) {
    const runStatuses = [STARTING, RUNNING, STOPPING, SNAPSHOTTING];
    $scope.dropDownItems = [STOP_WITH_SNAPSHOT, STOP_WITHOUT_SNAPSHOT];

    const preselectItem = $scope.dropDownItems.indexOf(this.cheWorkspace.getAutoSnapshotSettings() ? STOP_WITH_SNAPSHOT : STOP_WITHOUT_SNAPSHOT);
    $scope.dropDownSelectPos = preselectItem > 0 ? preselectItem : 0;

    const updateButton = (workspaceStatus: string) => {
      if (!workspaceStatus) {
        $scope.isStopButton = false;
      } else {
        $scope.isStopButton = runStatuses.indexOf(workspaceStatus) !== -1;
      }
      $scope.isDisabled = [STOPPING, SNAPSHOTTING].indexOf(workspaceStatus) !== -1;
      $scope.isStarting = workspaceStatus === STARTING;
    };
    updateButton($scope.workspaceStatus);

    $scope.onSelect = (dropDownItem: number) => {
      $scope.dropDownSelectPos = dropDownItem;
    };

    $scope.changeWorkspaceStatus = () => {
      if ($scope.isStopButton) {
        const isCreateSnapshot = !$scope.isStarting ? $scope.dropDownItems.indexOf(STOP_WITH_SNAPSHOT) === $scope.dropDownSelectPos : false;
        $scope.stopWorkspace({isCreateSnapshot: isCreateSnapshot});
      } else {
        $scope.runWorkspace();
      }
    };

    const watcher = $scope.$watch(() => {
      return $scope.workspaceStatus;
    }, (workspaceStatus: string) => {
      updateButton(workspaceStatus);
    });
    $scope.$on('$destroy', () => {
      watcher();
    });
  }
}
