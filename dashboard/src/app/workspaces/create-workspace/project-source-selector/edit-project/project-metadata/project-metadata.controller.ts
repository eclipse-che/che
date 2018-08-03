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

import {ProjectMetadataService} from './project-metadata.service';
import {ProjectSourceSelectorService} from '../../project-source-selector.service';
import {EditProjectService} from '../edit-project.service';

/**
 * This class is handling the controller for project's metadata.
 *
 * @author Oleksii Kurinnyi
 */
export class ProjectMetadataController {

  static $inject = ['$scope', 'projectMetadataService', 'projectSourceSelectorService', 'editProjectService'];

  /**
   * Project metadata service.
   */
  private projectMetadataService: ProjectMetadataService;
  /**
   * Project's source selector service.
   */
  private projectSourceSelectorService: ProjectSourceSelectorService;
  /**
   * Edit project section service.
   */
  private editProjectService: EditProjectService;
  /**
   * The project's template provided from parent controller.
   * Provided by parent controller.
   */
  private origTemplate: che.IProjectTemplate;
  /**
   * Original template name provided from parent controller.
   * Provided by parent controller.
   */
  /* tslint:disable */
  private templateName: string;

  private projectForm: ng.IFormController;
  /* tslint:enable */
  /**
   * Callback to check uniqueness of project name.
   * Provided by parent controller.
   */
  private isProjectNameUnique: (data: {name: string}) => boolean;
  /**
   * The project template to edit.
   */
  private template: che.IProjectTemplate;

  /**
   * Default constructor that is using resource injection
   */
  constructor($scope: ng.IScope, projectMetadataService: ProjectMetadataService, projectSourceSelectorService: ProjectSourceSelectorService,
     editProjectService: EditProjectService) {

    this.projectMetadataService = projectMetadataService;
    this.projectSourceSelectorService = projectSourceSelectorService;
    this.editProjectService = editProjectService;

    const watcher = $scope.$watch(() => {
      return this.origTemplate;
    }, () => {
      this.template = angular.copy(this.origTemplate);
      this.projectMetadataService.setOrigProjectTemplate(this.origTemplate);
      this.projectMetadataService.onMetadataChanged(this.template);
    });
    $scope.$on('$destroy', () => {
      watcher();
    });

    this.editProjectService.subscribe(() => {
      this.onEditProjectServicePublish();
    });
  }

  /**
   * Restores project's name, description and source location from original state.
   */
  onEditProjectServicePublish(): void {
    this.template = angular.copy(this.origTemplate);
    this.projectMetadataService.onMetadataChanged(this.template);
  }

  /**
   * Callback which is called when name is changed.
   *
   * @param {string} name the project's name
   */
  onNameChanged(name: string): void {
    this.template.name = name;
    this.projectMetadataService.onMetadataChanged(this.template);
  }

  /**
   * Callback which is called when description is changed.
   *
   * @param {string} description the project's description
   */
  onDescriptionChanged(description: string): void {
    this.template.description = description;
    this.projectMetadataService.onMetadataChanged(this.template);
  }

  /**
   * Callback which is called when metadata is changed.
   *
   * @param {string} location the project's source location
   */
  onUrlChanged(location: string): void {
    this.template.source.location = location;
    this.projectMetadataService.onMetadataChanged(this.template);
  }

  /**
   * Returns <code>true</code> if name is unique.
   *
   * @param {string} name new project's name
   * @return {boolean}
   */
  isNameUnique(name: string): boolean {
    return this.isProjectNameUnique({name: name});
  }

}
