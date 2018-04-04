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
