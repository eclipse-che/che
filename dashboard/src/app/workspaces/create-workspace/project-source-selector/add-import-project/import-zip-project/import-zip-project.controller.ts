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

import {ImportZipProjectService} from './import-zip-project.service';
import {ProjectSource} from '../../project-source.enum';
import {AddImportProjectService} from '../add-import-project.service';

/**
 * This class is handling the controller for the Zip project import.
 *
 * @author Oleksii Kurinnyi
 */
export class ImportZipProjectController {

  static $inject = ['$scope', 'importZipProjectService', 'addImportProjectService'];

  /**
   * Import Zip project service.
   */
  private importZipProjectService: ImportZipProjectService;
  /**
   * Service for adding or importing projects.
   */
  private addImportProjectService: AddImportProjectService;
  /**
   * Zip repository location.
   */
  private location: string;
  /**
   * Skip the root folder of archive if <code>true</code>
   */
  private skipFirstLevel: boolean;
  /**
   * Directive's form.
   */
  private form: ng.IFormController;

  /**
   * Default constructor that is using resource injection
   */
  constructor($scope: ng.IScope, importZipProjectService: ImportZipProjectService, addImportProjectService: AddImportProjectService) {
    this.importZipProjectService = importZipProjectService;
    this.addImportProjectService = addImportProjectService;

    this.location = this.importZipProjectService.location;
    this.skipFirstLevel = this.importZipProjectService.skipFirstLevel;

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
   * Clears project's location.
   *
   * @param {ProjectSource} source project's source
   */
  onAddImportProjectServicePublish(source: ProjectSource): void {
    if (source !== ProjectSource.ZIP) {
      return;
    }

    this.location = '';
    this.skipFirstLevel = false;

    this.importZipProjectService.onChanged();

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
   * Callback which is called when location or source parameter is changed.
   *
   * @param {string} location the zip project's location
   */
  onUrlChanged(location: string): void {
    this.importZipProjectService.onChanged(location, this.skipFirstLevel);
  }

  /**
   * Callback which is called when location or source parameter is changed.
   */
  onCheckboxChanged(): void {
    this.importZipProjectService.onChanged(this.location, this.skipFirstLevel);
  }
}
