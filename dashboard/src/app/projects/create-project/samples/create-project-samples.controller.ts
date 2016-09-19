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

/**
 * This class is handling the controller for the samples part
 * @author Florent Benoit
 */
export class CreateProjectSamplesController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($rootScope, cheAPI, $timeout, lodash) {
    this.$rootScope = $rootScope;
    this.cheAPI = cheAPI;
    this.$timeout = $timeout;
    this.lodash = lodash;

    // ask to load che templates
    let promise = cheAPI.getProjectTemplate().fetchTemplates();

    // promise update
    promise.then(() => {
        this.updateData();
      },
      (error) => {
        if (error.status === 304) {
          // ok
          this.updateData();
          return;
        }
        this.state = 'error';
      });
  }

  /**
   * Defines the samples
   */
  updateData() {
    this.templatesByCategory = this.cheAPI.getProjectTemplate().getTemplatesByCategory();
    this.templates = this.cheAPI.getProjectTemplate().getAllProjectTemplates();
  }

  /**
   * Callback when a template is selected and also give the controller on which to select the data
   * @param template the selected template
   * @param createProjectCtrl callback controller
   */
  selectTemplate(template, createProjectCtrl) {

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

    var name = template.displayName;
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
   */
  initItem(templateName, createProjectCtrl) {
    if (createProjectCtrl.selectSourceOption === 'select-source-new') {
      let template = this.lodash.find(this.templates, (template) => {return template.name === templateName;});
      this.$timeout(() => {
        this.selectTemplate(template, createProjectCtrl);
      });
    }
  }

  /**
   * Returns true if template is already selected
   * @param templateName
   * @returns {boolean}
   */
  isTemplateSelected(templateName) {
    return this.selectedTemplateName === templateName;
  }

  /**
   * Helper method used to get the length of keys of the given object
   * @param items
   * @returns length of keys
   */
  getItemsSize(items) {
    return items && Object.keys(items).length;
  }

}
