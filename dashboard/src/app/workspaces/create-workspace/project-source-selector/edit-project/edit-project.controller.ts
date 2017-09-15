/*
 * Copyright (c) 2015-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {EditProjectService} from './edit-project.service';

/**
 * This class is handling the service for project editing.
 *
 * @author Oleksii Kurinnyi
 */
export class EditProjectController {
  /**
   * Edit project section service.
   */
  private editProjectService: EditProjectService;
  /**
   * Callback to check uniqueness of project name.
   * Provided by parent controller.
   */
  private isProjectNameUnique: (data: {name: string}) => boolean;
  /**
   * Callback which should be called for changes to be saved.
   * Provided by parent controller.
   */
  private projectOnEdit: (data: {template: che.IProjectTemplate}) => void;
  /**
   * Callback which should be called to remove project template from list.
   * Provided by parent controller.
   */
  private projectOnRemove: () => void;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($scope: ng.IScope, editProjectService: EditProjectService) {
    this.editProjectService = editProjectService;

    $scope.$on('$destroy', () => {
      this.editProjectService.restoreTemplate();
    });  }

  /**
   * Callback which should be called when "Save" button is pressed.
   */
  projectTemplateOnSave(): void {
    const projectTemplate = this.editProjectService.getProjectTemplate();

    this.projectOnEdit({template: projectTemplate});
  }

  /**
   * Callback which should be called when "Cancel" button is pressed.
   */
  projectTemplateOnCancel(): void {
    this.editProjectService.restoreTemplate();
  }

  /**
   * Callback which should be called when "Remove" button is pressed.
   */
  projectTemplateOnRemove(): void {
    this.projectOnRemove();
  }

  /**
   * Returns <code>true</code> if "Save" and "Cancel" buttons should be disabled.
   *
   * @return {boolean}
   */
  disableSaveAndCancelButtons(): boolean {
    return this.editProjectService.checkEditingProgress() === null;
  }

}
