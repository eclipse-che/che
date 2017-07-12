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
import {ProjectSource} from './project-source.enum';
import {TemplateSelectorSvc} from './template-selector/template-selector.service';
import {ImportBlankProjectService} from './import-blank-project/import-blank-project.service';
import {ImportGitProjectService} from './import-git-project/import-git-project.service';
import {ImportZipProjectService} from './import-zip-project/import-zip-project.service';
import {ProjectSourceSelectorServiceObservable} from './project-source-selector-service-observable';
import {ProjectMetadataService} from './project-metadata/project-metadata.service';
import {RandomSvc} from '../../../../components/utils/random.service';
import {ImportGithubProjectService} from './import-github-project/import-github-project.service';

/**
 * This class is handling the service for the project selector.
 *
 * @author Oleksii Kurinnyi
 */
export class ProjectSourceSelectorService extends ProjectSourceSelectorServiceObservable {
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
   * Project metadata service.
   */
  private projectMetadataService: ProjectMetadataService;
  /**
   * Generator for random strings.
   */
  private randomSvc: RandomSvc;
  /**
   * Project templates to import.
   */
  private projectTemplates: Array<che.IProjectTemplate>;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor(templateSelectorSvc: TemplateSelectorSvc, importBlankProjectService: ImportBlankProjectService, importGitProjectService: ImportGitProjectService, importGithubProjectService: ImportGithubProjectService, importZipProjectService: ImportZipProjectService, projectMetadataService: ProjectMetadataService, randomSvc: RandomSvc) {
    super();

    this.templateSelectorSvc = templateSelectorSvc;
    this.importBlankProjectService = importBlankProjectService;
    this.importGitProjectService = importGitProjectService;
    this.importGithubProjectService = importGithubProjectService;
    this.importZipProjectService = importZipProjectService;
    this.projectMetadataService = projectMetadataService;
    this.randomSvc = randomSvc;

    this.projectTemplates = [];
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
   * Adds project from source.
   *
   * @param {ProjectSource} source the project's source
   */
  addProjectTemplateFromSource(source: ProjectSource): void {
    switch (source) {
      case ProjectSource.SAMPLES:
        const projectTemplates = this.templateSelectorSvc.getTemplates();
        projectTemplates.forEach((projectTemplate: che.IProjectTemplate) => {
          this.addProjectTemplate(source, projectTemplate);
        });
        break;
      case ProjectSource.BLANK: {
        const projectProps = this.importBlankProjectService.getProjectProps();
        const projectTemplate = this.buildProjectTemplate(projectProps);
        this.addProjectTemplate(source, projectTemplate);
      }
        break;
      case ProjectSource.GIT: {
        const projectProps = this.importGitProjectService.getProjectProps();
        const projectTemplate = this.buildProjectTemplate(projectProps);
        this.addProjectTemplate(source, projectTemplate);
      }
        break;
      case ProjectSource.GITHUB: {
        const repositoriesProps = this.importGithubProjectService.getRepositoriesProps();
        repositoriesProps.forEach((repositoryProps: che.IProjectTemplate) => {
          const projectTemplate = this.buildProjectTemplate(repositoryProps);
          this.addProjectTemplate(source, projectTemplate);
        });
      }
        break;
      case ProjectSource.ZIP: {
        const projectProps = this.importZipProjectService.getProjectProps();
        const projectTemplate = this.buildProjectTemplate(projectProps);
        this.addProjectTemplate(source, projectTemplate);
      }
        break;
    }

    this.publish(source);
  }

  /**
   * Adds project template to the list.
   *
   * @param {ProjectSource} source the project's source
   * @param {che.IProjectTemplate} projectTemplate the project template
   */
  addProjectTemplate(source: ProjectSource, projectTemplate: che.IProjectTemplate): void {
    const origName = projectTemplate.name;

    if (this.isProjectTemplateNameUnique(origName) === false) {
      // update name, displayName and path
      const newName = this.getUniqueName(origName);
      projectTemplate.name = newName;
      projectTemplate.displayName = newName;
      projectTemplate.path = '/' +  newName.replace(/[^\w-_]/g, '_');
    }

    this.projectTemplates.push(projectTemplate);
  }

  /**
   * Adds increment or random string to the name
   *
   * @param {string} name
   */
  getUniqueName(name: string): string {
    const limit = 100;
    for (let i = 1; i < limit + 1; i++) {
      const newName = name + '-' + i;
      if (this.isProjectTemplateNameUnique(newName)) {
        return newName;
      }
    }

    return this.randomSvc.getRandString({prefix: name + '-'});
  }

  /**
   * Returns list of project templates.
   *
   * @return {Array<che.IProjectTemplate>}
   */
  getProjectTemplates(): Array<che.IProjectTemplate> {
    return this.projectTemplates;
  }

  /**
   * Returns <code>true</code> if project's template name is unique.
   *
   * @param {string} name the project's template name
   * @param {string=} excluded the project's template name which should be skipped from comparison
   * @return {boolean}
   */
  isProjectTemplateNameUnique(name: string, excluded?: string): boolean {
    return this.projectTemplates.every((projectTemplate: che.IProjectTemplate) => {
      return projectTemplate.name !== name || projectTemplate.name === excluded;
    });
  }

  /**
   * Removes project's template from the list.
   *
   * @param {string} projectTemplateName the template to remove
   */
  removeProjectTemplate(projectTemplateName: string): void {
    let indexToRemove = -1;
    this.projectTemplates.find((projectTemplate: che.IProjectTemplate, index: number) => {
      if (projectTemplate.name === projectTemplateName) {
        indexToRemove = index;
        return true;
      }
      return false;
    });

    if (indexToRemove !== -1) {
      this.projectTemplates.splice(indexToRemove, 1);
    }
  }

  /**
   * Updates project template's metadata.
   *
   * @param {che.IProjectTemplate} projectTemplateName project template name
   * @return {che.IProjectTemplate}
   */
  updateProjectTemplateMetadata(projectTemplateName: string): che.IProjectTemplate {
    if (!projectTemplateName) {
      return;
    }

    const projectTemplateNew = this.projectMetadataService.getProjectTemplate();
    if (!projectTemplateNew) {
      return;
    }

    const projectTemplateOld = this.projectTemplates.find((_projectTemplate: che.IProjectTemplate) => {
      return _projectTemplate.name === projectTemplateName;
    });

    if (!projectTemplateOld) {
      return;
    }

    return angular.extend(projectTemplateOld, projectTemplateNew);
  }

  /**
   * Resets project's section flow.
   */
  clearAllSources(): void {
    ProjectSource.values().forEach((source: ProjectSource) => {
      this.clearSource(source);
    });

    this.projectTemplates = [];
  }

  /**
   * Resets project template's source.
   *
   * @param {ProjectSource} source the project's source
   */
  clearSource(source: ProjectSource): void {
    this.publish(source);
  }

}
