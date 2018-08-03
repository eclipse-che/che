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
import {ProjectSource} from '../project-source.enum';
import {AddImportProjectService} from './add-import-project.service';
import {ProjectSourceSelectorService} from '../project-source-selector.service';

/**
 * This class is handling the controller for project templates adding or importing.
 *
 * @author Oleksii Kurinyi
 */
export class AddImportProjectController {

  static $inject = ['$scope', 'addImportProjectService', 'projectSourceSelectorService'];

  /**
   * Project selector service.
   */
  private addImportProjectService: AddImportProjectService;
  /**
   * Project source selector service.
   */
  private projectSourceSelectorService: ProjectSourceSelectorService;
  /**
   * Active project's source.
   */
  private activeProjectSource: ProjectSource;
  /**
   * Available project sources.
   */
  private projectSource: Object;
  /**
   * Callback to check uniqueness of project name.
   * Provided by parent controller.
   */
  /* tslint:disable */
  private isProjectNameUnique: (data: {name: string}) => boolean;
  /* tslint:enable */
  /**
   * Callback provided by parent controller.
   */
  private projectOnAdd: (data: {templates: Array<che.IProjectTemplate>}) => void;

  /**
   * Default constructor that is using resource injection
   */
  constructor($scope: ng.IScope, addImportProjectService: AddImportProjectService, projectSourceSelectorService: ProjectSourceSelectorService) {
    this.addImportProjectService = addImportProjectService;
    this.projectSourceSelectorService = projectSourceSelectorService;

    this.projectSource = ProjectSource;

    this.activeProjectSource = ProjectSource.SAMPLES;
    this.sourceChanged();

    $scope.$on('$destroy', () => {
      this.addImportProjectService.clearAllSources();
    });
  }

  /**
   * Set source of project which is going to be added.
   */
  sourceChanged(): void {
    this.addImportProjectService.setProjectSource(this.activeProjectSource);
  }

  /**
   * Gets project templates from selected source.
   */
  projectTemplateOnSave(): void {
    const projectTemplates = this.addImportProjectService.getProjectTemplatesFromSource(this.activeProjectSource);

    this.addImportProjectService.clearSource(this.activeProjectSource);

    this.projectOnAdd({templates: projectTemplates});
  }

  /**
   * Resets input fields and checkboxes for selected source.
   */
  projectTemplateOnCancel(): void {
    this.addImportProjectService.clearSource(this.activeProjectSource);
  }

  /**
   * Returns <code>true</code> if "Save" and "Cancel" buttons should be disabled.
   *
   * @return {boolean}
   */
  disableSaveAndCancelButtons(): boolean {
    return this.addImportProjectService.checkEditingProgress() === null;
  }

}
