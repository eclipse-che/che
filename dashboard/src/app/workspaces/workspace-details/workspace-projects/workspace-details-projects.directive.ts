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
 * <workspace-details-project></workspace-details-projects>` for displaying workspace projects entry.
 *
 * @param {expression=} projects-list the list or projects
 * @param {Function=} projects-on-add the callback which is called when user adds a template or imports a project
 *
 * @usage
 *   <workspace-details-project projects-list="ctrl.projects"
 *                              projects-on-add="ctrl.projectsOnAdd(templates)"></workspace-details-project>
 *
 * @author Ann Shumilova
 */
export class WorkspaceDetailsProjects implements ng.IDirective {
  restrict = 'E';
  templateUrl = 'app/workspaces/workspace-details/workspace-projects/workspace-details-projects.html';

  controller = 'WorkspaceDetailsProjectsCtrl';
  controllerAs = 'workspaceDetailsProjectsCtrl';

  bindToController = true;

  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   */
  constructor () {
    this.scope = {
      workspaceId: '@',
      workspaceDetails: '=',
      projectsOnChange: '&'
    };
  }
}
