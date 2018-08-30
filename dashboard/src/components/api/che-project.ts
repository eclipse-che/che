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

interface ICHEProjectResource<T> extends ng.resource.IResourceClass<T> {
  import: any;
  create: any;
  batchCreate: any;
  details: any;
  estimate: any;
  rename: any;
  remove: any;
  resolve: any;
  update: any;
}

/**
 * This class is handling the projects retrieval
 * It sets to the array projects for any workspace that is not temporary
 * @author Florent Benoit
 */
export class CheProject {
  private $q: ng.IQService;
  private $resource: ng.resource.IResourceService;
  private resolveMap: Map <string, any>;
  private estimateMap: Map <string, any>;
  private projectDetailsMap: Map <string, any>;
  private remoteProjectsAPI: ICHEProjectResource<any>;


  /**
   * Default constructor that is using resource
   */
  constructor($resource: ng.resource.IResourceService, $q: ng.IQService, wsagentPath: string, machineToken: string) {
    this.$q = $q;
    this.$resource = $resource;

    // project details map with key projectPath
    this.projectDetailsMap = new Map();

    // map of estimate per project + project type: <project:projectType> --> Estimate
    this.estimateMap = new Map();

    // map of resolve per project  <project:projectType> --> Source Estimation
    this.resolveMap = new Map();

    // remote call
    this.remoteProjectsAPI = <ICHEProjectResource<any>>this.$resource(wsagentPath + '/project', {}, {
      import: {method: 'POST', url: wsagentPath + '/project/import/:path', headers: this.getRequestHeaders(machineToken)},
      create: {method: 'POST', url: wsagentPath + '/project?name=:path', headers: this.getRequestHeaders(machineToken)},
      batchCreate: {method: 'POST', url: wsagentPath + '/project/batch', isArray: true, headers: this.getRequestHeaders(machineToken)},
      details: {method: 'GET', url: wsagentPath + '/project/:path', headers: this.getRequestHeaders(machineToken)},
      estimate: {method: 'GET', url: wsagentPath + '/project/estimate/:path?type=:type', headers: this.getRequestHeaders(machineToken)},
      rename: {method: 'POST', url: wsagentPath + '/project/rename/:path?name=:name', headers: this.getRequestHeaders(machineToken)},
      remove: {method: 'DELETE', url: wsagentPath + '/project/:path', headers: this.getRequestHeaders(machineToken)},
      resolve: {method: 'GET', url: wsagentPath + '/project/resolve/:path', isArray: true, headers: this.getRequestHeaders(machineToken)},
      update: {method: 'PUT', url: wsagentPath + '/project/:path', headers: this.getRequestHeaders(machineToken)}
    });
  }

  getRequestHeaders(machineToken: string): any {
    if (!machineToken) {
      return;
    }

    return {'Authorization': machineToken};
  }

  /**
   * Import a project based located on the given path
   * @param path the path{string} of the project
   * @param data the project{any} body description
   * @returns {ng.IPromise<any>}
   */
  importProject(path: string, data: any): ng.IPromise<any> {
    // remove unused description because we cannot set project description without project type
    if ((!data.type || data.type.length === 0) && data.description) {
      delete(data.description);
    }
    let promise = this.remoteProjectsAPI.import({path: path}, data).$promise;
    return promise;
  }


  /**
   * Create a project based located on the given path
   * @param path{string} the path of the project
   * @param data{che.IProject} the project body description
   * @returns {ng.IPromise<any>}
   */
  createProject(path: string, data: che.IProject): ng.IPromise<any> {
    let promise = this.remoteProjectsAPI.create({path: path}, data).$promise;
    return promise;
  }

  /**
   * Create a batch of projects.
   * @param projects{Array<che.IProjectTemplate>} the list of projects to be created
   * @returns {ng.IPromise<any>}
   */
  createProjects(projects: Array<che.IProjectTemplate>): ng.IPromise<any> {
    let promise = this.remoteProjectsAPI.batchCreate(projects).$promise;
    return promise;
  }

  /**
   * Gets the fullname from a profile
   * @param profile{che.IProfile} the profile to analyze
   * @returns {string} a name
   */
  getFullName(profile: che.IProfile): string {
    let firstName = profile.attributes.firstName;
    if (!firstName) {
      firstName = '';
    }
    let lastName = profile.attributes.lastName;
    if (!lastName) {
      lastName = '';
    }

    return firstName + ' ' + lastName;
  }

  /**
   * Fetch project details on the given path
   * @param projectPath the path of the project
   * @returns {ng.IPromise<any>}
   */
  fetchProjectDetails(workspaceId: string, projectPath: string): ng.IPromise<any> {
    // todo why we cannot use project path
    let projectName = projectPath[0] === '/' ? projectPath.slice(1) : projectPath;
    let promise = this.remoteProjectsAPI.details({path: projectName}).$promise;

    // check if it was OK or not
    let parsedResultPromise = promise.then((projectDetails: any) => {
      if (projectDetails) {
        projectDetails.workspaceId = workspaceId;
        this.projectDetailsMap.set(projectPath, projectDetails);
      }
    });

    return parsedResultPromise;
  }

  getProjectDetailsByKey(projectPath: string): any {
    return this.projectDetailsMap.get(projectPath);
  }

  removeProjectDetailsByKey(projectPath: string): void {
    this.projectDetailsMap.delete(projectPath);
  }

  updateProjectDetails(projectDetails: any): ng.IPromise<any> {
    return this.updateProject(projectDetails.name, projectDetails);
  }

  updateProject(path: string, projectDetails: any): ng.IPromise<any> {
    let newProjectDetails = angular.copy(projectDetails);
    if (newProjectDetails.workspaceId) {
      delete(newProjectDetails.workspaceId);
    }
    let promiseUpdateProjectDetails = this.remoteProjectsAPI.update({
      path: path
    }, newProjectDetails).$promise;

    return promiseUpdateProjectDetails;
  }

  rename(projectName: string, newProjectName: string): ng.IPromise<any> {
    let promise = this.remoteProjectsAPI.rename({path: projectName, name: newProjectName}, null).$promise;
    return promise;
  }

  remove(projectName: string): ng.IPromise<any> {
    let promiseDelete = this.remoteProjectsAPI.remove({path: projectName}).$promise;
    return promiseDelete;
  }

  /**
   * Fetch estimate and return promise for this estimate
   * @param projectPath{string} the path to the project in the workspace
   * @param projectType{string} the project type in the list of available types
   * @returns {ng.IPromise<any>}
   */
  fetchEstimate(projectPath: string, projectType: string): ng.IPromise<any> {
    let projectName = projectPath[0] === '/' ? projectPath.slice(1) : projectPath;
    let promise = this.remoteProjectsAPI.estimate({path: projectName, type: projectType}).$promise;
    let parsedResultPromise = promise.then((estimate: any) => {
      if (estimate) {
        this.estimateMap.set(projectName + projectType, estimate);
      }
    });
    return parsedResultPromise;
  }

  /**
   * @return the estimation based on the given 2 inputs : project path and project type
   */
  getEstimate(projectPath: string, projectType: string): any {
    let projectName = projectPath[0] === '/' ? projectPath.slice(1) : projectPath;
    return this.estimateMap.get(projectName + projectType);
  }


  /**
   * Fetch resolve and return promise for this resolution
   * @param projectPath the path to the project in the workspace
   * @returns {ng.IPromise<any>}
   */
  fetchResolve(projectPath: string): ng.IPromise<any> {
    let projectName = projectPath[0] === '/' ? projectPath.slice(1) : projectPath;
    let promise = this.remoteProjectsAPI.resolve({path: projectName}).$promise;
    let parsedResultPromise = promise.then((resolve: any) => {
      if (resolve) {
        this.resolveMap.set(projectName, resolve);
      }
    });
    return parsedResultPromise;
  }

  /**
   * @return the estimation based on the given input : project path
   */
  getResolve(projectPath: string): any {
    let projectName = projectPath[0] === '/' ? projectPath.slice(1) : projectPath;
    return this.resolveMap.get(projectName);
  }

}
