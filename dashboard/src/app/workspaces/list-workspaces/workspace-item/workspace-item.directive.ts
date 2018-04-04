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
 * Defines a directive for items in workspace list.
 * Expects in parent scope:
 * @param{object} workspace
 */
export class CheWorkspaceItem implements ng.IDirective {
  restrict = 'E';
  // we require ngModel as we want to use it inside our directive
  require = ['ngModel'];

  templateUrl = 'app/workspaces/list-workspaces/workspace-item/workspace-item.html';

  controller = 'WorkspaceItemCtrl';
  controllerAs = 'workspaceItemCtrl';
  bindToController = true;
  // scope values
  scope = {
    workspace: '=cheWorkspaceItem',
    isSelectable: '=cheSelectable',
    isSelect: '=?ngModel',
    onCheckboxClick: '&?cheOnCheckboxClick',
    displayLabels: '=cheDisplayLabels',
    isRequestPending: '=?'
  };

}
