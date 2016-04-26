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
  constructor ($q, $resource, $location, cheWorkspace) {
    this.$q = $q;
    this.$resource = $resource;
    this.cheWorkspace = cheWorkspace;

    // types per category per workspace ID : workspace ID ==> map<projectTypeId, projectType>
    this.typesIdPerWorkspace = new Map();

    // project types per workspace ID
    this.typesWorkspaces = new Map();
    // remote call
    this.remoteProjectTypeAPI = this.$resource('//:agent/api/ext/project-type/:workspaceId');

  }


  /**
   * Fetch the project types
   */
  fetchTypes(workspaceId) {
    let agent = this.cheWorkspace.getWorkspaceAgent(workspaceId);
    var defer = this.$q.defer();
    let promise = this.remoteProjectTypeAPI.query({agent: agent, workspaceId: workspaceId}).$promise;
    let updatedPromise = promise.then((projectTypes) => {

      var idProjectTypesMap = this.typesIdPerWorkspace.get(workspaceId);
      if (!idProjectTypesMap) {
        idProjectTypesMap = new Map();
        this.typesIdPerWorkspace.set(workspaceId, idProjectTypesMap);
      }

      var typesWorkspace = this.typesWorkspaces.get(workspaceId);
      if (!typesWorkspace) {
        typesWorkspace = [];
        this.typesWorkspaces.set(workspaceId, typesWorkspace);
      }

      // reset global list
      typesWorkspace.length = 0;

      projectTypes.forEach((projectType) => {
        var id = projectType.id;
        // add in map
        idProjectTypesMap.set(id, projectType);
        // add in global list
        typesWorkspace.push(projectType);
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
  getAllProjectTypes(workspaceId) {
    let val = this.typesWorkspaces.get(workspaceId);
    return !val ? [] : val;
  }

  /**
   * The types per category
   * @returns {CheProjectType.typesPerCategory|*}
   */
  getProjectTypesIDs(workspaceId) {
    let val = this.typesIdPerWorkspace.get(workspaceId);
    return !val ? {} : val;
  }


}
