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

    // project details map with key projectPath
    this.projectDetailsMap = new Map();

    // map of estimate per project + project type: <project:projectType> --> Estimate
    this.estimateMap = new Map();

    // map of resolve per project  <project:projectType> --> Source Estimation
    this.resolveMap = new Map();

    // remote call
    this.remoteProjectsAPI = this.$resource(wsagentPath + '/project', {}, {
      import: {method: 'POST', url: wsagentPath + '/project/import/:path'},
      create: {method: 'POST', url: wsagentPath + '/project?name=:path'},
      batchCreate: {method: 'POST', url: wsagentPath + '/project/batch', isArray: true},
      details: {method: 'GET', url: wsagentPath + '/project/:path'},
      estimate: {method: 'GET', url: wsagentPath + '/project/estimate/:path?type=:type'},
      rename: {method: 'POST', url: wsagentPath + '/project/rename/:path?name=:name'},
      remove: {method: 'DELETE', url: wsagentPath + '/project/:path'},
      resolve: {method: 'GET', url: wsagentPath + '/project/resolve/:path', isArray: true},
      update: {method: 'PUT', url: wsagentPath + '/project/:path'}
    });
  }

  /**
   * Import a project based located on the given path
   * @param path the path of the project
   * @param data the project body description
   * @returns {$promise|*|T.$promise}
   */
  importProject(path, data) {
    // remove unused description because we cannot set project description without project type
    if ((!data.type || data.type.length === 0) && data.description) {
      delete(data.description);
    }
    let promise = this.remoteProjectsAPI.import({path: path}, data).$promise;
    return promise;
  }


  /**
   * Create a project based located on the given path
   * @param path the path of the project
   * @param data the project body description
   * @returns {$promise|*|T.$promise}
   */
  createProject(path, data) {
    let promise = this.remoteProjectsAPI.create({path: path}, data).$promise;
    return promise;
  }

  /**
   * Create a batch of projects.
   * @param projects the list of projects to be created
   * @returns {$promise|*|T.$promise}
   */
  createProjects(projects) {
    let promise = this.remoteProjectsAPI.batchCreate(projects).$promise;
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
   * Fetch project details on the given path
   * @param projectPath the path of the project
   */
  fetchProjectDetails(workspaceId, projectPath) {
    //TODO why we cannot use project path
    var projectName = projectPath[0] === '/' ? projectPath.slice(1) : projectPath;
    let promise = this.remoteProjectsAPI.details({path: projectName}).$promise;

    // check if it was OK or not
    let parsedResultPromise = promise.then((projectDetails) => {
      if (projectDetails) {
        projectDetails.workspaceId = workspaceId;
        this.projectDetailsMap.set(projectPath, projectDetails);
      }
    });

    return parsedResultPromise;
  }

  getProjectDetailsByKey(projectPath) {
    return this.projectDetailsMap.get(projectPath);
  }

  removeProjectDetailsByKey(projectPath) {
    this.projectDetailsMap.delete(projectPath);
  }

  updateProjectDetails(projectDetails) {
    return this.updateProject(projectDetails.name, projectDetails);
  }

  updateProject(path, projectDetails) {
    let newProjectDetails = angular.copy(projectDetails);
    if(newProjectDetails.workspaceId){
      delete(newProjectDetails.workspaceId);
    }
    let promiseUpdateProjectDetails = this.remoteProjectsAPI.update({
      path: path
    }, newProjectDetails).$promise;

    return promiseUpdateProjectDetails;
  }

  rename(projectName, newProjectName) {
    let promise = this.remoteProjectsAPI.rename({path: projectName, name: newProjectName}, null).$promise;
    return promise;
  }

  remove(projectName) {
    let promiseDelete = this.remoteProjectsAPI.remove({path: projectName}).$promise;
    return promiseDelete;
  }

  /**
   * Fetch estimate and return promise for this estimate
   * @param projectPath the path to the project in the workspace
   * @param projectType the project type in the list of available types
   * @returns {Promise}
   */
  fetchEstimate(projectPath, projectType) {
      let projectName = projectPath[0] === '/' ? projectPath.slice(1) : projectPath;
      let promise = this.remoteProjectsAPI.estimate({path: projectName, type: projectType}).$promise;
      let parsedResultPromise = promise.then((estimate) => {
        if (estimate) {
          this.estimateMap.set(projectName + projectType, estimate);
        }
      });
    return parsedResultPromise;
  }

  /**
   * @return the estimation based on the given 2 inputs : project path and project type
   */
  getEstimate(projectPath, projectType) {
    let projectName = projectPath[0] === '/' ? projectPath.slice(1) : projectPath;
    return this.estimateMap.get(projectName + projectType);
  }


  /**
   * Fetch resolve and return promise for this resolution
   * @param projectPath the path to the project in the workspace
   * @returns {Promise}
   */
  fetchResolve(projectPath) {
    let projectName = projectPath[0] === '/' ? projectPath.slice(1) : projectPath;
    let promise = this.remoteProjectsAPI.resolve({path: projectName}).$promise;
    let parsedResultPromise = promise.then((resolve) => {
      if (resolve) {
        this.resolveMap.set(projectName, resolve);
      }
    });
    return parsedResultPromise;
  }

  /**
   * @return the estimation based on the given input : project path
   */
  getResolve(projectPath) {
    let projectName = projectPath[0] === '/' ? projectPath.slice(1) : projectPath;
    return this.resolveMap.get(projectName);
  }

}
