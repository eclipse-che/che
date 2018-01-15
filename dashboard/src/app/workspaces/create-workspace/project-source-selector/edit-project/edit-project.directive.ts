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
 * Defines the directive for project editing.
 *
 * @author Oleksii Kurinnyi
 */
export class EditProject implements ng.IDirective {
  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/create-workspace/project-source-selector/edit-project/edit-project.html';

  bindToController: boolean = true;
  controller: string = 'EditProjectController';
  controllerAs: string = 'editProjectController';

  scope: {
    [propName: string]: string;
  } = {
    isProjectNameUnique: '&',
    projectTemplate: '=',
    projectOnEdit: '&',
    projectOnRemove: '&'
  };

}
