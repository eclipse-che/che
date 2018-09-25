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

import {ImportBlankProjectService} from './import-blank-project.service';
import {ProjectSource} from '../../project-source.enum';
import {AddImportProjectService} from '../add-import-project.service';

/**
 * This class is handling the controller for the blank project import.
 *
 * @author Oleksii Kurinnyi
 */
export class ImportBlankProjectController {

  static $inject = ['$scope', 'addImportProjectService', 'importBlankProjectService'];

  /**
   * Service for adding or importing projects.
   */
  private addImportProjectService: AddImportProjectService;
  /**
   * Importing blank project service.
   */
  private importBlankProjectService: ImportBlankProjectService;
  /**
   * Project's name.
   */
  private name: string;
  /**
   * Project's description.
   */
  private description: string;
  /**
   * Directive's form.
   */
  private form: ng.IFormController;
  /**
   * Callback to check uniqueness of project name.
   * Provided by parent controller.
   */
  private isProjectNameUnique: (data: {name: string}) => boolean;

  /**
   * Default constructor that is using resource injection
   */
  constructor($scope: ng.IScope, addImportProjectService: AddImportProjectService, importBlankProjectService: ImportBlankProjectService) {
    this.addImportProjectService = addImportProjectService;
    this.importBlankProjectService = importBlankProjectService;

    this.name = this.importBlankProjectService.name;
    this.description = this.importBlankProjectService.description;

    const actionOnPublish = (source: ProjectSource) => {
      this.onAddImportProjectServicePublish(source);
    };
    this.addImportProjectService.subscribe(actionOnPublish);

    $scope.$on('$destroy', () => {
      this.addImportProjectService.unsubscribe(actionOnPublish);
    });
  }

  /**
   * Callback which is called when project template is added to the list of ready-to-import projects.
   * Clears name and description.
   *
   * @param {ProjectSource} source project's source
   */
  onAddImportProjectServicePublish(source: ProjectSource): void {
    if (source !== ProjectSource.BLANK) {
      return;
    }

    this.name = '';
    this.description = '';

    this.importBlankProjectService.onChanged();

    this.refreshForm();
  }

  /**
   * Register directive's form.
   *
   * @param {ng.IFormController} form
   */
  registerForm(form: ng.IFormController): void {
    this.form = form;
  }

  /**
   * Refresh form to eliminate all errors.
   */
  refreshForm(): void {
    this.form.$setUntouched();
    this.form.$setPristine();
  }

  /**
   * Callback which is called when project's name is changed.
   *
   * @param {string} name the blank project's name
   */
  onNameChanged(name: string): void {
    this.importBlankProjectService.onChanged(name, this.description);
  }

  /**
   * Callback which is called when project's description is changed.
   *
   * @param {string} description the blank project's description
   */
  onDescriptionChanged(description: string): void {
    this.importBlankProjectService.onChanged(this.name, description);
  }

  /**
   * Returns <code>true</code> name is unique in current workspace.
   *
   * @return {boolean}
   */
  isNameUnique(name: string): boolean {
    return this.isProjectNameUnique({name: name});
  }

}
