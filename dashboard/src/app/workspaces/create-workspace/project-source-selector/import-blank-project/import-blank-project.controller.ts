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

import {ProjectSourceSelectorService} from '../project-source-selector.service';
import {ImportBlankProjectService} from './import-blank-project.service';
import {IProjectSourceSelectorServiceObserver} from '../project-source-selector-service.observer';
import {ProjectSource} from '../project-source.enum';
import {ActionType} from '../project-source-selector-action-type.enum';

/**
 * This class is handling the controller for the blank project import.
 *
 * @author Oleksii Kurinnyi
 */
export class ImportBlankProjectController implements IProjectSourceSelectorServiceObserver {
  /**
   * Project selector service.
   */
  private projectSourceSelectorService: ProjectSourceSelectorService;
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
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor(projectSourceSelectorService: ProjectSourceSelectorService, importBlankProjectService: ImportBlankProjectService) {
    this.projectSourceSelectorService = projectSourceSelectorService;
    this.importBlankProjectService = importBlankProjectService;

    this.name = this.importBlankProjectService.name;
    this.description = this.importBlankProjectService.description;

    this.projectSourceSelectorService.subscribe(this.onProjectSourceSelectorServicePublish.bind(this));
  }

  /**
   * Callback which is called when project template is added to the list of ready-to-import projects.
   * Clears name and description.
   *
   * @param {ActionType} action the type of action
   * @param {ProjectSource} source the project's source
   */
  onProjectSourceSelectorServicePublish(action: ActionType, source: ProjectSource): void {
    if (action !== ActionType.ADD_PROJECT || source !== ProjectSource.BLANK) {
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
    return this.projectSourceSelectorService.isProjectTemplateNameUnique(name);
  }

}
