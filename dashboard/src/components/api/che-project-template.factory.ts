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

/**
 * This class is handling the projects template retrieval
 * It sets to the array project templates
 * @author Florent Benoit
 */
export class CheProjectTemplate {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($resource) {

    // keep resource
    this.$resource = $resource;

    // types per category
    this.templatesPerCategory = {};

    // project templates
    this.templates = [];

    // remote call
    this.remoteProjectTemplateAPI = this.$resource('/api/project-template/all');
  }



  /**
   * Fetch the project templates
   */
  fetchTemplates() {

    let promise = this.remoteProjectTemplateAPI.query().$promise;
    let updatedPromise = promise.then((projectTemplates) => {


      // reset global list
      this.templates.length = 0;
      for (var member in this.templatesPerCategory) {
        delete this.templatesPerCategory[member];
      }

      projectTemplates.forEach((projectTemplate) => {

        // get attributes
        var category = projectTemplate.category;

        // get list
        var lst = this.templatesPerCategory[category];
        if (!lst) {
          lst = [];
          this.templatesPerCategory[category] = lst;
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
   * @returns {Array}
   */
  getAllProjectTemplates() {
    return this.templates;
  }

  /**
   * The templates per category
   * @returns {CheProjectTemplate.templatesPerCategory|*}
   */
  getTemplatesByCategory() {
    return this.templatesPerCategory;
  }


}
