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

/**
 * @ngdoc directive
 * @name workspaces.details.directive:workspaceDetailsProjects
 * @restrict E
 * @element
 *
 * @description
 * <workspace-details-project></workspace-details-projects>` for displaying workspace projects entry.
 *
 * @param {Function=} get-workspace-status
 *
 * @usage
 *   <workspace-details-project get-workspace-status="ctrl.getWorkspaceStatus()"></workspace-details-project>
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
   * @ngInject for Dependency injection
   */
  constructor () {
    this.scope = {
      getWorkspaceStatus: '&'
    };
  }
}
