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
   */
  constructor ($resource, $q, wsagentPath) {
    this.$q = $q;
    this.$resource = $resource;

    // types per category per workspace ID : workspace ID ==> map<projectTypeId, projectType>
    this.typesIds = new Map();

    // project types per workspace ID
    this.workspaceTypes = [];
    // remote call
    this.remoteProjectTypeAPI = this.$resource(wsagentPath + '/project-type');
  }


  /**
   * Fetch the project types
   */
  fetchTypes() {
    var defer = this.$q.defer();
    let promise = this.remoteProjectTypeAPI.query().$promise;
    let updatedPromise = promise.then((projectTypes) => {

      // reset global list
      this.workspaceTypes = projectTypes;

      projectTypes.forEach((projectType) => {
        this.typesIds.set(projectType.id, projectType);
      });
      defer.resolve();
    }, (error) => {
      if (error.status !== 304) {
        defer.reject(error);
      } else {
        defer.resolve();
      }
    });

    return defer.promise;
  }

  /**
   * Gets all project types
   * @returns {Array}
   */
  getAllProjectTypes() {
    return this.workspaceTypes;
  }

  /**
   * The types per category
   * @returns {CheProjectType.typesPerCategory|*}
   */
  getProjectTypesIDs() {
    return this.typesIds;
  }

}
