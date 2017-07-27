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

import {ImportGitProjectService} from './import-git-project.service';
import {ProjectSourceSelectorService} from '../project-source-selector.service';
import {IProjectSourceSelectorServiceObserver} from '../project-source-selector-service.observer';
import {ProjectSource} from '../project-source.enum';
import {ActionType} from '../project-source-selector-action-type.enum';

/**
 * This class is handling the controller for the Git project import.
 *
 * @author Oleksii Kurinnyi
 */
export class ImportGitProjectController implements IProjectSourceSelectorServiceObserver {
  /**
   * Import Git project service.
   */
  private importGitProjectService: ImportGitProjectService;
  /**
   * Project source selector service.
   */
  private projectSourceSelectorService: ProjectSourceSelectorService;
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
   * @ngInject for Dependency injection
   */
  constructor(importGitProjectService: ImportGitProjectService, projectSourceSelectorService: ProjectSourceSelectorService) {
    this.importGitProjectService = importGitProjectService;
    this.projectSourceSelectorService = projectSourceSelectorService;

    this.location = this.importGitProjectService.location;

    this.projectSourceSelectorService.subscribe(this.onProjectSourceSelectorServicePublish.bind(this));
  }

  /**
   * Callback which is called when project template is added to the list of ready-to-import projects.
   * Clears project's location.
   *
   * @param {ActionType} action the type of action
   * @param {ProjectSource} source the project's source
   */
  onProjectSourceSelectorServicePublish(action: ActionType, source: ProjectSource): void {
    if (action !== ActionType.ADD_PROJECT || source !== ProjectSource.GIT) {
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
