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
 * Defines a directive for items in project list.
 * Expects in parent scope:
 * @param{string} workspaceId
 * @param{object} project
 */
export class CheProjectItem implements ng.IDirective {
  restrict = 'E';

  // we require ngModel as we want to use it inside our directive
  require = ['ngModel'];

  // scope values
  scope = {
    workspace: '=cheProjectItemWorkspace',
    project: '=cheProjectItemProject',
    profileCreationDate: '=cheProfileCreationDate',
    isDisplayWorkspace: '=cheDisplayWorkspace',
    isSelectable: '=cheSelectable',
    isSelect: '=?ngModel',
    onCheckboxClick: '&?cheOnCheckboxClick',
    hasAction: '=?cheHasAction'
  };

  templateUrl = 'app/workspaces/workspace-details/workspace-projects/project-item/project-item.html';

  controller = 'ProjectItemCtrl';
  controllerAs = 'projectItemCtrl';
  bindToController = true;

}
