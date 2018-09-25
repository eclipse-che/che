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

import {ImportZipProjectService} from './import-zip-project/import-zip-project.service';
import {ImportGithubProjectService} from './import-github-project/import-github-project.service';
import {ImportGitProjectService} from './import-git-project/import-git-project.service';
import {ImportBlankProjectService} from './import-blank-project/import-blank-project.service';
import {TemplateSelectorSvc} from './template-selector/template-selector.service';
import {ProjectSource} from '../project-source.enum';
import {IObservable, IObservableCallbackFn, Observable} from '../../../../../components/utils/observable';
import {editingProgress} from '../project-source-selector-editing-progress';

/**
 * This class is handling the service for project adding and importing.
 *
 * @author Oleksii Kurinnyi
 */
export class AddImportProjectService {

  static $inject = ['templateSelectorSvc', 'importBlankProjectService', 'importGitProjectService', 'importGithubProjectService', 'importZipProjectService'];

  /**
   * Template selector service.
   */
  private templateSelectorSvc: TemplateSelectorSvc;
  /**
   * Import blank project service.
   */
  private importBlankProjectService: ImportBlankProjectService;
  /**
   * Import Git project service.
   */
  private importGitProjectService: ImportGitProjectService;
  /**
   * Import GitHub project service.
   */
  private importGithubProjectService: ImportGithubProjectService;
  /**
   * Import Zip project service.
   */
  private importZipProjectService: ImportZipProjectService;
  /**
   * Project source.
   */
  private activeProjectSource: ProjectSource;
  /**
   * Instance of Observable.
   */
  private observable: IObservable<ProjectSource>;

  /**
   * Default constructor that is using resource injection
   */
  constructor(templateSelectorSvc: TemplateSelectorSvc, importBlankProjectService: ImportBlankProjectService, importGitProjectService: ImportGitProjectService, importGithubProjectService: ImportGithubProjectService, importZipProjectService: ImportZipProjectService) {
    this.templateSelectorSvc = templateSelectorSvc;
    this.importBlankProjectService = importBlankProjectService;
    this.importGitProjectService = importGitProjectService;
    this.importGithubProjectService = importGithubProjectService;
    this.importZipProjectService = importZipProjectService;

    this.observable = new Observable<ProjectSource>();
  }

  /**
   * Add callback to the list of subscribers.
   *
   * @param {IObservableCallbackFn<ProjectSource>} action the callback
   */
  subscribe(action: IObservableCallbackFn<ProjectSource>): void {
    this.observable.subscribe(action);
  }

  /**
   * Unregister callback.
   *
   * @param {IObservableCallbackFn<ProjectSource>} action the callback
   */
  unsubscribe(action: IObservableCallbackFn<ProjectSource>): void {
    this.observable.unsubscribe(action);
  }

  /**
   * Remove callback from the list of subscribers.
   *
   * @param {ProjectSource} projectSource
   */
  setProjectSource(projectSource: ProjectSource): void {
    this.activeProjectSource = projectSource;
  }

  /**
   * Builds new project template based on blank project template.
   *
   * @param {any} props
   * @return {che.IProjectTemplate}
   */
  buildProjectTemplate(props: any): che.IProjectTemplate {
    const blankProjectTemplate = this.templateSelectorSvc.getTemplateByName('blank-project');
    return angular.merge({}, blankProjectTemplate, props);
  }

  /**
   * Returns list of project templates from selected source.
   *
   * @param {ProjectSource} source the project's source
   * @return {Array<che.IProjectTemplate>}
   */
  getProjectTemplatesFromSource(source: ProjectSource): Array<che.IProjectTemplate> {
    const projectTemplates: Array<che.IProjectTemplate> = [];

    switch (source) {
      case ProjectSource.SAMPLES:
        const _projectTemplates = this.templateSelectorSvc.getTemplates();
        _projectTemplates.forEach((projectTemplate: che.IProjectTemplate) => {
          projectTemplates.push(projectTemplate);
        });
        break;
      case ProjectSource.BLANK: {
        const projectProps = this.importBlankProjectService.getProjectProps();
        const projectTemplate = this.buildProjectTemplate(projectProps);
        projectTemplates.push(projectTemplate);
      }
        break;
      case ProjectSource.GIT: {
        const projectProps = this.importGitProjectService.getProjectProps();
        const projectTemplate = this.buildProjectTemplate(projectProps);
        delete projectTemplate.type;
        delete projectTemplate.projectType;
        projectTemplates.push(projectTemplate);
      }
        break;
      case ProjectSource.GITHUB: {
        const repositoriesProps = this.importGithubProjectService.getRepositoriesProps();
        repositoriesProps.forEach((repositoryProps: che.IProjectTemplate) => {
          const projectTemplate = this.buildProjectTemplate(repositoryProps);
          delete projectTemplate.type;
          delete projectTemplate.projectType;
          projectTemplates.push(projectTemplate);
        });
      }
        break;
      case ProjectSource.ZIP: {
        const projectProps = this.importZipProjectService.getProjectProps();
        const projectTemplate = this.buildProjectTemplate(projectProps);
        delete projectTemplate.type;
        delete projectTemplate.projectType;
        projectTemplates.push(projectTemplate);
      }
        break;
    }

    return projectTemplates;
  }

  /**
   * Resets project's section flow.
   */
  clearAllSources(): void {
    ProjectSource.values().forEach((source: ProjectSource) => {
      this.clearSource(source);
    });
  }

  /**
   * Resets project template's source.
   *
   * @param {ProjectSource} source the project's source
   */
  clearSource(source: ProjectSource): void {
    this.observable.publish(source);
  }

  /**
   * Checks if any project's template is adding or importing.
   *
   * @return {editingProgress}
   */
  checkEditingProgress(): editingProgress {
    switch (this.activeProjectSource) {
      case ProjectSource.SAMPLES:
        return this.templateSelectorSvc.checkEditingProgress();
      case ProjectSource.BLANK:
        return this.importBlankProjectService.checkEditingProgress();
      case ProjectSource.GIT:
        return this.importGitProjectService.checkEditingProgress();
      case ProjectSource.GITHUB:
        return this.importGithubProjectService.checkEditingProgress();
      case ProjectSource.ZIP:
        return this.importZipProjectService.checkEditingProgress();
    }

    return null;
  }

}
