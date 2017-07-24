/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

import {ProjectMetadataService} from './project-metadata.service';
import {ProjectSourceSelectorService} from '../project-source-selector.service';
import {IProjectSourceSelectorServiceObserver} from '../project-source-selector-service.observer';
import {ActionType} from '../project-source-selector-action-type.enum';
import {ProjectSource} from '../project-source.enum';

/**
 * This class is handling the controller for project's metadata.
 *
 * @author Oleksii Kurinnyi
 */
export class ProjectMetadataController implements IProjectSourceSelectorServiceObserver {
  /**
   * Project metadata service.
   */
  private projectMetadataService: ProjectMetadataService;
  /**
   * Project's source selector service.
   */
  private projectSourceSelectorService: ProjectSourceSelectorService;
  /**
   * The project's template provided from parent controller.
   */
  private origTemplate: che.IProjectTemplate;
  /**
   * Original template name provided from parent controller.
   */
  private templateName: string;
  /**
   * The project template to edit.
   */
  private template: che.IProjectTemplate;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($scope: ng.IScope, projectMetadataService: ProjectMetadataService, projectSourceSelectorService: ProjectSourceSelectorService) {

    this.projectMetadataService = projectMetadataService;
    this.projectSourceSelectorService = projectSourceSelectorService;

    const watcher = $scope.$watch(() => {
      return this.origTemplate;
    }, () => {
      this.template = angular.copy(this.origTemplate);
      this.projectMetadataService.setOrigProjectTemplate(this.origTemplate);
    });
    $scope.$on('$destroy', () => {
      watcher();
    });

    this.projectSourceSelectorService.subscribe(this.onProjectSourceSelectorServicePublish.bind(this));
  }

  /**
   * Restores project's name, description and source location from original state.
   *
   * @param {ActionType} action the type of action
   * @param {ProjectSource} source the project's source
   */
  onProjectSourceSelectorServicePublish(action: ActionType, source: ProjectSource): void {
    if (action !== ActionType.EDIT_PROJECT) {
      return;
    }

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
    return this.projectSourceSelectorService.isProjectTemplateNameUnique(name, this.templateName);
  }

}
