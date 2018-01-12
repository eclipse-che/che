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
 * Defines the directive for popover which includes add/import project widget.
 *
 * @author Oleksii Kurinnyi
 */
export class AddProjectPopover {
  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/workspace-details/workspace-projects/add-project-popover/add-project-popover.html';

  bindToController: boolean = true;
  controller: string = 'AddProjectPopoverController';
  controllerAs: string = 'addProjectPopoverController';

  scope: {
    [propName: string]: string;
  } = {
    isProjectNameUnique: '&',
    projectOnAdd: '&'
  };
}
