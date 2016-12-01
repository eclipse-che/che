/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';
import {CheAPI} from '../../../../components/api/che-api.factory';
import {CreateProjectController} from '../create-project.controller';

/**
 * This class is handling the controller for the samples part
 * @author Florent Benoit
 */
export class CreateProjectSamplesController {
  $rootScope: ng.IRootScopeService;
  templates: Array<any>;
  selectedTemplateName: string = '';

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($rootScope: ng.IRootScopeService, cheAPI: CheAPI) {
    this.$rootScope = $rootScope;

    this.templates = cheAPI.getProjectTemplate().getAllProjectTemplates();
    if (!this.templates.length) {
      cheAPI.getProjectTemplate().fetchTemplates();
    }
  }

  /**
   * Callback when a template is selected and also give the controller on which to select the data
   * @param template: che.IProject -  the selected template
   * @param createProjectCtrl: CreateProjectController - callback controller
   */
  selectTemplate(template: che.IProject, createProjectCtrl: CreateProjectController) {
    // selected item
    this.selectedTemplateName = template.name;
    // update source details
    createProjectCtrl.importProjectData.source.type = template.source.type;
    createProjectCtrl.importProjectData.source.location = template.source.location;
    createProjectCtrl.importProjectData.source.parameters = template.source.parameters;
    // update name, type, description
    createProjectCtrl.setProjectDescription(template.description);
    createProjectCtrl.importProjectData.project.type = template.projectType;
    createProjectCtrl.importProjectData.project.commands = template.commands;
    createProjectCtrl.importProjectData.projects = template.projects;

    let name: string = template.displayName;
    // strip space
    name = name.replace(/\s/g, '_');
    // strip dot
    name = name.replace(/\./g, '_');
    createProjectCtrl.setProjectName(name);
    // broadcast event
    this.$rootScope.$broadcast('create-project-samples:selected');
  }

  /**
   * Select the first element in the list
   * @param templateName: che.IProject
   * @param createProjectCtrl: CreateProjectController
   */
  initItem(template: che.IProject, createProjectCtrl: CreateProjectController): void {
    if (!template || this.selectedTemplateName === template.name) {
      return;
    }
    this.selectTemplate(template, createProjectCtrl);
  }

  /**
   * Helper method used to get the length of keys of the given object
   * @param items: Array<any>
   * @returns {number} length of keys
   */
  getItemsSize(items: Array<any>): number {
    return items.length;
  }
}
