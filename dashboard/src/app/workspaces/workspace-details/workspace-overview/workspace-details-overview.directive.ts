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
 * @name workspaces.details.overview.directive:workspaceDetailsOverview
 * @restrict E
 * @element
 *
 * @description
 * <workspace-details-overview></workspace-details-overview>` for displaying workspace overview entry.
 *
 * @param {Function=} on-change
 * @param {che.IWorkspace=} workspace-details
 * @param {ng.IFormController=} overview-form
 *
 * @usage
 *   <workspace-details-overview  workspace-details="ctrl.getWorkspaceDetails()"
 *                                overview-form="overviewForm"
 *                                on-change="ctrl.onWorkspaceChange()"></workspace-details-overview>
 *
 * @author Oleksii Orel
 */
export class WorkspaceDetailsOverview implements ng.IDirective {
  restrict = 'E';
  templateUrl = 'app/workspaces/workspace-details/workspace-overview/workspace-details-overview.html';

  controller = 'WorkspaceDetailsOverviewController';
  controllerAs = 'workspaceDetailsOverviewController';

  bindToController = true;

  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   */
  constructor() {
    this.scope = {
      overviewForm: '=',
      workspaceDetails: '=',
      onChange: '&'
    };
  }
}
