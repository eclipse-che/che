/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
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
 * @name workspaces.details.directive:workspaceDetailsSSH
 * @restrict E
 * @element
 *
 * @description
 * <workspace-details-ssh></workspace-ssh-ssh>` for displaying workspace ssh entry.
 *
 * @usage
 *   <workspace-details-ssh></workspace-details-ssh>
 *
 * @author Florent Benoit
 */
export class WorkspaceDetailsSsh {


  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/workspace-details/workspace-ssh/workspace-details-ssh.html';

  controller: string = 'WorkspaceDetailsSshCtrl';
  controllerAs: string = 'workspaceDetailsSshCtrl';
  bindToController: boolean = true;


  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {

  }

}
