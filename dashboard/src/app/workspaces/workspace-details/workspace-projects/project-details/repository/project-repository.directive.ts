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
 * Defines a directive for displaying project repository widget.
 * @author Oleksii Orel
 */
export class ProjectRepository implements ng.IDirective {
  restrict = 'E';

  templateUrl = 'app/workspaces/workspace-details/workspace-projects/project-details/repository/project-repository.html';

  controller = 'ProjectRepositoryController';
  controllerAs = 'projectRepositoryController';

  bindToController = true;

  scope = true;

}
