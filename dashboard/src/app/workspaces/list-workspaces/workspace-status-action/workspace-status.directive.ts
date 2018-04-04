/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
