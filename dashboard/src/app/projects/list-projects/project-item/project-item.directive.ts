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
 * Defines a directive for items in project list.
 * Expects in parent scope:
 * @param{string} workspaceId
 * @param{object} project
 */
export class CheProjectItem {

  /**
   * Default constructor.
   */
  constructor() {
    this.restrict = 'E';

    // we require ngModel as we want to use it inside our directive
    this.require = ['ngModel'];

    // scope values
    this.scope = {
      workspace: '=cheProjectItemWorkspace',
      project: '=cheProjectItemProject',
      profileCreationDate: '=cheProfileCreationDate',
      isDisplayWorkspace: '=cheDisplayWorkspace',
      isSelectable: '=cheSelectable',
      isSelect: '=?ngModel',
      onCheckboxClick: '&?cheOnCheckboxClick'
    };

    this.templateUrl = 'app/projects/list-projects/project-item/project-item.html';


    this.controller = 'ProjectItemCtrl';
    this.controllerAs = 'projectItemCtrl';
    this.bindToController = true;

  }

}
