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
 * This class is handling the projects type retrieval
 * It sets to the array project types
 * @author Florent Benoit
 */
export class CheProjectType {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($resource) {

    // keep resource
    this.$resource = $resource;

    // types per category
    this.typesPerCategory = {};

    // project types
    this.types = [];

    // remote call
    this.remoteProjectTypeAPI = this.$resource('/api/project-type');
  }



  /**
   * Fetch the project types
   */
  fetchTypes() {

    let promise = this.remoteProjectTypeAPI.query().$promise;
    let updatedPromise = promise.then((projectTypes) => {


      // reset global list
      this.types.length = 0;
      for (var member in this.typesPerCategory) {
        delete this.typesPerCategory[member];
      }

      projectTypes.forEach((projectType) => {

        // get attributes
        var category = '';
        var attributeDescriptors = projectType.attributeDescriptors;
        attributeDescriptors.forEach((attributeDescriptor) => {
          if ('language' === attributeDescriptor.name) {
            category = attributeDescriptor.values[0];
          }
        });

        if (attributeDescriptors.length === 0) {
          category = projectType.id;
        }

        if (category === '') {
          return;
        }

        var typeCategory = category;

        // get list
        var lst = this.typesPerCategory[typeCategory];
        if (!lst) {
          lst = [];
          this.typesPerCategory[typeCategory] = lst;
        }

        // add element on the list
        this.typesPerCategory[typeCategory].push(projectType);



        this.types.push(projectType);
      });

    });

    return updatedPromise;
  }



  /**
   * Gets all project types
   * @returns {Array}
   */
  getAllProjectTypes() {
    return this.types;
  }

  /**
   * The types per category
   * @returns {CheProjectType.typesPerCategory|*}
   */
  getTypesByCategory() {
    return this.typesPerCategory;
  }


}
