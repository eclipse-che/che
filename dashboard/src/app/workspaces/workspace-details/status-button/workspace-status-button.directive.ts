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
import {CheWorkspace, WorkspaceStatus} from '../../../../components/api/workspace/che-workspace.factory';

interface IWorkspaceStatusButtonScope extends ng.IScope {
  buttonDisabled: boolean;
  isDisabled: boolean;
  showStopButton: boolean;
  workspaceStatus: string;
  onRunWorkspace: () => void;
  onStopWorkspace: () => void;
}

const STARTING = WorkspaceStatus[WorkspaceStatus.STARTING];
const RUNNING = WorkspaceStatus[WorkspaceStatus.RUNNING];
const STOPPING = WorkspaceStatus[WorkspaceStatus.STOPPING];

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
 * @param {Function=} onRunWorkspace
 * @param {Function=} onStopWorkspace
 *
 * @author Oleksii Orel
 */


export class CheWorkspaceStatusButton {

  static $inject = ['cheWorkspace'];

  restrict = 'E';
  templateUrl = 'app/workspaces/workspace-details/status-button/workspace-status-button.html';

  // scope values
  scope = {
    workspaceStatus: '=',
    buttonDisabled: '=',
    onRunWorkspace: '&',
    onStopWorkspace: '&'
  };

  private cheWorkspace: CheWorkspace;

  /**
   * Default constructor that is using resource
   */
  constructor(cheWorkspace: CheWorkspace) {
    this.cheWorkspace = cheWorkspace;
  }

  /**
   * Keep reference to the model controller
   */
  link($scope: IWorkspaceStatusButtonScope) {
    const runStatuses = [STARTING, RUNNING, STOPPING];

    const updateButton = (workspaceStatus: string) => {
      if (!workspaceStatus) {
        $scope.showStopButton = false;
      } else {
        $scope.showStopButton = runStatuses.indexOf(workspaceStatus) !== -1;
      }
      $scope.isDisabled = [STOPPING].indexOf(workspaceStatus) !== -1;
    };
    updateButton($scope.workspaceStatus);

    const watcher = $scope.$watch(() => {
      return $scope.workspaceStatus;
    }, (workspaceStatus: string) => {
      updateButton(workspaceStatus);
    }, true);
    $scope.$on('$destroy', () => {
      watcher();
    });
  }
}
