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
