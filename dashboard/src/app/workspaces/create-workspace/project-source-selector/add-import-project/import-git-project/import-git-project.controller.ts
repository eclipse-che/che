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

import {ImportGitProjectService} from './import-git-project.service';
import {ProjectSource} from '../../project-source.enum';
import {AddImportProjectService} from '../add-import-project.service';

/**
 * This class is handling the controller for the Git project import.
 *
 * @author Oleksii Kurinnyi
 */
export class ImportGitProjectController {

  static $inject = ['$scope', 'importGitProjectService', 'addImportProjectService'];

  /**
   * Import Git project service.
   */
  private importGitProjectService: ImportGitProjectService;
  /**
   * Service for adding or importing projects.
   */
  private addImportProjectService: AddImportProjectService;
  /**
   * Git repository location.
   */
  private location: string;
  /**
   * Directive's form.
   */
  private form: ng.IFormController;

  /**
   * Default constructor that is using resource injection
   */
  constructor($scope: ng.IScope, importGitProjectService: ImportGitProjectService, addImportProjectService: AddImportProjectService) {
    this.importGitProjectService = importGitProjectService;
    this.addImportProjectService = addImportProjectService;

    this.location = this.importGitProjectService.location;

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
    if (source !== ProjectSource.GIT) {
      return;
    }

    this.location = '';
    this.importGitProjectService.onLocationChanged();

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
   * Callback which is called when location is changed.
   *
   * @param {string} location the git project location
   */
  onChanged(location: string): void {
    this.importGitProjectService.onLocationChanged(location);
  }

}
