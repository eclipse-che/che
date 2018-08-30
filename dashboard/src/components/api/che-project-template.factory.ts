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

/**
 * This class is handling the projects template retrieval
 * It sets to the array project templates
 * @author Florent Benoit
 */
export class CheProjectTemplate {

  static $inject = ['$resource'];

  $resource: ng.resource.IResourceService;
  templatesPerCategory: {
    [category: string]: Array<che.IProjectTemplate>;
  };
  templates: Array<che.IProjectTemplate>;

  remoteProjectTemplateAPI: ng.resource.IResourceClass<any>;

  /**
   * Default constructor that is using resource
   */
  constructor ($resource: ng.resource.IResourceService) {

    // keep resource
    this.$resource = $resource;

    // types per category
    this.templatesPerCategory = {};

    // project templates
    this.templates = [];

    // remote call
    this.remoteProjectTemplateAPI = <ng.resource.IResourceClass<any>> this.$resource('/api/project-template/all');
  }



  /**
   * Fetch the project templates
   *
   * @return {IPromise<any>}
   */
  fetchTemplates(): ng.IPromise<any> {

    const promise = this.remoteProjectTemplateAPI.query().$promise;
    const updatedPromise = promise.then((projectTemplates: Array<che.IProjectTemplate>) => {

      // reset global list
      this.templates.length = 0;
      for (const member in this.templatesPerCategory) {
        if (this.templatesPerCategory.hasOwnProperty(member)) {
          delete this.templatesPerCategory[member];
        }
      }

      projectTemplates.forEach((projectTemplate: che.IProjectTemplate) => {
        // get attributes
        const category = projectTemplate.category;

        // get list
        if (!this.templatesPerCategory[category]) {
          this.templatesPerCategory[category] = [];
        }

        // add element on the list
        this.templatesPerCategory[category].push(projectTemplate);

        this.templates.push(projectTemplate);
      });

    });

    return updatedPromise;
  }

  /**
   * Gets all project templates
   *
   * @return {Array<che.IProjectTemplate>}
   */
  getAllProjectTemplates(): Array<che.IProjectTemplate> {
    return this.templates;
  }

  /**
   * The templates per category
   *
   * @return {{[p: string]: Array<che.IProjectTemplate>}}
   */
  getTemplatesByCategory(): {[category: string]: Array<che.IProjectTemplate>} {
    return this.templatesPerCategory;
  }


}
