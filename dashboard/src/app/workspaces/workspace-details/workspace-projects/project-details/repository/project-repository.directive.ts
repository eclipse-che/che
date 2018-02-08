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
