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
 * @name workspaces.details.directive:workspaceDetailsProjects
 * @restrict E
 * @element
 *
 * @description
 * <export-workspace workspace-id="workspaceID" workspace-details="workspaceDetails"></export-workspace>
 *
 * @usage
 *   <export-workspace workspace-id="workspaceID" workspace-details="workspaceDetails"></export-workspace>
 *
 * @author Florent Benoit
 */
export class ExportWorkspace implements ng.IDirective {

  restrict = 'E';
  templateUrl = 'app/workspaces/workspace-details/export-workspace/export-workspace.html';

  controller = 'ExportWorkspaceController';
  controllerAs = 'exportWorkspaceCtrl';
  bindToController = true;

  // scope values
  scope = {
    workspaceId: '@workspaceId',
    workspaceDetails: '=workspaceDetails',
    workspaceExportDisabled: '='
  };

}
