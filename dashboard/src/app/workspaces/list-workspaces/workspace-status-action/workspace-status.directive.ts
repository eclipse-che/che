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

/**
 * @ngdoc directive
 * @name workspace.status.directive:cheWorkspaceStatus
 * @restrict E
 * @element
 *
 * @description
 * `<che-workspace-status>` defines workspace status component for start/stop workspace.
 *
 * @usage
 *   <che-workspace-status workspace-id="workspaceId"></che-workspace-status>
 *
 * @author Oleksii Orel
 */
export class CheWorkspaceStatus implements ng.IDirective {
  restrict = 'E';
  templateUrl = 'app/workspaces/list-workspaces/workspace-status-action/workspace-status.html';
  bindToController = true;
  controller = 'WorkspaceStatusController';
  controllerAs = 'workspaceStatusController';
  scope = {
    workspaceId: '=workspaceId',
    isRequestPending: '=?'
  };

}
