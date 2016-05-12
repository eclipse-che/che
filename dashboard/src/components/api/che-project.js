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
 * This class is handling the projects retrieval
 * It sets to the array projects for any workspace that is not temporary
 * @author Florent Benoit
 */
export class CheProject {

  /**
   * Default constructor that is using resource
   */
  constructor($resource, $q, cheWebsocket, wsagentPath) {

    // keep resource
    this.$resource = $resource;

    this.cheWebsocket = cheWebsocket;

    this.$q = $q;

    // project details map with key = workspaceId+projectPath
    this.projectDetailsMap = new Map();

    // map of estimate per workspace Id + project + project type: <workspaceID:project:projectType> --> Estimate
    this.estimateMap = new Map();

    // map of resolve per workspace Id/project  <workspaceID:project:projectType> --> Source Estimation
    this.resolveMap = new Map();

    // remote call
    this.remoteProjectsAPI = this.$resource(wsagentPath + '/project/:workspaceId', {workspaceId: '@id'}, {
      import: {method: 'POST', url: wsagentPath + '/project/:workspaceId/import/:path'},
      create: {method: 'POST', url: wsagentPath + '/project/:workspaceId?name=:path'},
      details: {method: 'GET', url: wsagentPath + '/project/:workspaceId/:path'},
      estimate: {method: 'GET', url: wsagentPath + '/project/:workspaceId/estimate/:path?type=:type'},
      rename: {method: 'POST', url: wsagentPath + '/project/:workspaceId/rename/:path?name=:name'},
      remove: {method: 'DELETE', url: wsagentPath + '/project/:workspaceId/:path'},
      resolve: {method: 'GET', url: wsagentPath + '/project/:workspaceId/resolve/:path', isArray: true},
      update: {method: 'PUT', url: wsagentPath + '/project/:workspaceId/:path'}
    });
  }

  /**
   * Import a project based located on the given workspace id and path
   * @param workspaceId the workspace ID to use
   * @param path the path of the project
   * @param data the project body description
   * @returns {$promise|*|T.$promise}
   */
  importProject(workspaceId, path, data) {
    // remove unused description because we cannot set project description without project type
    if ((!data.type || data.type.length === 0) && data.description) {
      delete(data.description);
    }
    let promise = this.remoteProjectsAPI.import({workspaceId: workspaceId, path: path}, data).$promise;
    return promise;
  }


  /**
   * Create a project based located on the given workspace id and path
   * @param workspaceId the workspace ID to use
   * @param path the path of the project
   * @param data the project body description
   * @returns {$promise|*|T.$promise}
   */
  createProject(workspaceId, path, data) {
    let promise = this.remoteProjectsAPI.create({workspaceId: workspaceId, path: path}, data).$promise;
    return promise;
  }

  /**
   * Gets the fullname from a profile
   * @param profile the profile to analyze
   * @returns {string} a name
   */
  getFullName(profile) {
    var firstName = profile.attributes.firstName;
    if (!firstName) {
      firstName = '';
    }
    var lastName = profile.attributes.lastName;
    if (!lastName) {
      lastName = '';
    }

    return firstName + ' ' + lastName;
  }

  /**
   * Fetch project details on the given workspace id and path
   * @param workspaceId the workspace ID to use
   * @param projectPath the path of the project
   */
  fetchProjectDetails(workspaceId, projectPath) {
    //TODO why we cannot use project path
    var projectName = projectPath[0] === '/' ? projectPath.slice(1) : projectPath;
    let promise = this.remoteProjectsAPI.details({workspaceId: workspaceId, path: projectName}).$promise;

    // check if it was OK or not
    let parsedResultPromise = promise.then((projectDetails) => {
      if (projectDetails) {
        projectDetails.workspaceId = workspaceId;
        this.projectDetailsMap.set(workspaceId + projectPath, projectDetails);
      }
    });

    return parsedResultPromise;
  }

  getProjectDetailsByKey(workspaceId, projectPath) {
    return this.projectDetailsMap.get(workspaceId + projectPath);
  }

  removeProjectDetailsByKey(workspaceId, projectPath) {
    this.projectDetailsMap.delete(workspaceId + projectPath);
  }

  updateProjectDetails(projectDetails) {
    return this.updateProject(projectDetails.workspaceId, projectDetails.name, projectDetails);
  }

  updateProject(workspaceId, path, projectDetails) {
    let newProjectDetails = angular.copy(projectDetails);
    if(newProjectDetails.workspaceId){
      delete(newProjectDetails.workspaceId);
    }
    let promiseUpdateProjectDetails = this.remoteProjectsAPI.update({
      workspaceId: workspaceId,
      path: path
    }, newProjectDetails).$promise;

    return promiseUpdateProjectDetails;
  }

  rename(workspaceId, projectName, newProjectName) {
    let promise = this.remoteProjectsAPI.rename({workspaceId: workspaceId, path: projectName, name: newProjectName}, null).$promise;
    return promise;
  }

  remove(workspaceId, projectName) {
    let promiseDelete = this.remoteProjectsAPI.remove({workspaceId: workspaceId, path: projectName}).$promise;
    return promiseDelete;
  }

  /**
   * Fetch estimate and return promise for this estimate
   * @param workspaceId the workspace ID of the project
   * @param projectPath the path to the project in the workspace
   * @param projectType the project type in the list of available types
   * @returns {Promise}
   */
  fetchEstimate(workspaceId, projectPath, projectType) {
      let projectName = projectPath[0] === '/' ? projectPath.slice(1) : projectPath;
      let promise = this.remoteProjectsAPI.estimate({workspaceId: workspaceId, path: projectName, type: projectType}).$promise;
      let parsedResultPromise = promise.then((estimate) => {
        if (estimate) {
          this.estimateMap.set(workspaceId + projectName + projectType, estimate);
        }
      });
    return parsedResultPromise;
  }

  /**
   * @return the estimation based on the given 3 inputs : workspace ID, project path and project type
   */
  getEstimate(workspaceId, projectPath, projectType) {
    let projectName = projectPath[0] === '/' ? projectPath.slice(1) : projectPath;
    return this.estimateMap.get(workspaceId + projectName + projectType);
  }


  /**
   * Fetch resolve and return promise for this resolution
   * @param workspaceId the workspace ID of the project
   * @param projectPath the path to the project in the workspace
   * @returns {Promise}
   */
  fetchResolve(workspaceId, projectPath) {
    let projectName = projectPath[0] === '/' ? projectPath.slice(1) : projectPath;
    let promise = this.remoteProjectsAPI.resolve({workspaceId: workspaceId, path: projectName}).$promise;
    let parsedResultPromise = promise.then((resolve) => {
      if (resolve) {
        this.resolveMap.set(workspaceId + projectName, resolve);
      }
    });
    return parsedResultPromise;
  }

  /**
   * @return the estimation based on the given 2 inputs : workspace ID, project path
   */
  getResolve(workspaceId, projectPath) {
    let projectName = projectPath[0] === '/' ? projectPath.slice(1) : projectPath;
    return this.resolveMap.get(workspaceId + projectName);
  }

}
