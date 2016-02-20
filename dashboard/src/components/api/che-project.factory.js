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
   * @ngInject for Dependency injection
   */
  constructor($resource, $q, cheUser, cheProfile, cheWebsocket) {

    // keep resource
    this.$resource = $resource;

    this.cheUser = cheUser;

    this.cheProfile = cheProfile;

    this.cheWebsocket = cheWebsocket;

    this.$q = $q;

    // projects per workspace id
    this.projectsPerWorkspace = {};

    // projects per workspace id
    this.projectsPerWorkspaceMap = new Map();

    // projects per workspace id
    this.projectsDetailsPerWorkspaceMap = new Map();

    // map containing map (first map is workspace with key = workspaceId, inner map is key = project, value = permissions
    this.permissionsPerWorkspaceMap = new Map();

    // workspaces used to retrieve projects
    this.workspaces = [];

    // projects on all workspaces
    this.projects = [];

    // project details map with key = workspaceId+projectPath
    this.projectDetailsMap = new Map();

    // map of estimate per workspace Id + project + project type: <workspaceID:project:projectType> --> Estimate
    this.estimateMap = new Map();

    // map of resolve per workspace Id/project  <workspaceID:project:projectType> --> Source Estimation
    this.resolveMap = new Map();

    // remote call
    this.remoteProjectsAPI = this.$resource('/api/ext/project/:workspaceId', {workspaceId: '@id'}, {
      import: {method: 'POST', url: '/api/ext/project/:workspaceId/import/:path'},
      create: {method: 'POST', url: '/api/ext/project/:workspaceId?name=:path'},
      details: {method: 'GET', url: '/api/ext/project/:workspaceId/:path'},
      estimate: {method: 'GET', url: '/api/ext/project/:workspaceId/estimate/:path?type=:type'},
      getPermissions: {method: 'GET', url: '/api/ext/project/:workspaceId/permissions/:path', isArray: true},
      updatePermissions: {method: 'POST', url: '/api/ext/project/:workspaceId/permissions/:path', isArray: true},
      rename: {method: 'POST', url: '/api/ext/project/:workspaceId/rename/:path?name=:name'},
      remove: {method: 'DELETE', url: '/api/ext/project/:workspaceId/:path'},
      resolve: {method: 'GET', url: '/api/ext/project/:workspaceId/resolve/:path', isArray: true},
      update: {method: 'PUT', url: '/api/ext/project/:workspaceId/:path'}
    });
  }


  /**
   * Notified when workspaces are updated
   * @param workspaces the array of workspaces that are now used
   */
  onChangeWorkspaces(workspaces) {
    var promises = [];

    // well we need to clear globally the current projects
    //TODO this.projectsPerWorkspaceMap.clear();

    this.workspaces.length = 0;

    // for each workspace
    workspaces.forEach((workspace) => {

      // update the current workspaces (replacing old one)
      this.workspaces.push(workspace);

      // init bus
      let bus = this.cheWebsocket.getBus(workspace.id);
      bus.subscribe('vfs', (message) => {
        // if vfs is updated, and this is a root module, refresh

        if (workspace.id === message.workspaceId) {
          if ('CREATED' === message.type || 'DELETED' === message.type || 'RENAMED' === message.type) {

            // count number of slashes to detect if this is a root project
            let slashCount = (message.path.match(/\//g) || []).length;

            if (1 === slashCount) {
              // refresh
              this.fetchProjectsForWorkspaceId(workspace.id);
            }
          }
        }
      });


      // fetch projects for this workspace
      let promise = this.fetchProjectsForWorkspace(workspace);
      promises.push(promise);
    });


    return this.$q.all(promises);

  }

  onDeleteWorkspace(workspaceId) {
    delete this.projectsPerWorkspace[workspaceId];
  }

  /**
   * Fetch the projects for the given workspace
   * @param workspace
   */
  fetchProjectsForWorkspaceId(workspaceId) {
    let promise = this.remoteProjectsAPI.query({workspaceId: workspaceId}).$promise;
    let updatedPromise = promise.then((projectReferences) => {
      var remoteProjects = [];
      projectReferences.forEach((projectReference) => {
        remoteProjects.push(projectReference);
      });

      // add the map key
      this.projectsPerWorkspaceMap.set(workspaceId, remoteProjects);
      this.projectsPerWorkspace[workspaceId] = remoteProjects;

      // refresh global projects list
      this.projects.length = 0;


      for (var member in this.projectsPerWorkspace) {
        let projects = this.projectsPerWorkspace[member];
        projects.forEach((project) => {
          this.projects.push(project);
        });
      }
    }, (error) => {
      if (error.status !== 304) {
        console.log(error);
      }
    });

    return updatedPromise;
  }


  /**
   * Fetch the projects for the given workspace
   * @param workspace
   */
  fetchProjectsForWorkspace(workspace) {
    var workspaceId = workspace.id;

    var remoteProjects = [];
    if (workspace.config.projects) {
      workspace.config.projects.forEach((projectReference) => {
        projectReference.workspaceId = workspace.id;
        projectReference.workspaceName = workspace.config.name;
        remoteProjects.push(projectReference);
      });
    }

    // add the map key
    this.projectsPerWorkspaceMap.set(workspaceId, remoteProjects);
    this.projectsPerWorkspace[workspaceId] = remoteProjects;

    // refresh global projects list
    this.projects.length = 0;


    for (var member in this.projectsPerWorkspace) {
      let projects = this.projectsPerWorkspace[member];
      projects.forEach((project) => {
        this.projects.push(project);
      });
    }

    // needs to return a promise
    var deferred = this.$q.defer();
    deferred.resolve('success');
    return deferred.promise;
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
    // update projects as well
    promise.then(this.fetchProjectsForWorkspaceId(workspaceId));
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
    // update projects as well
    promise.then(this.fetchProjectsForWorkspaceId(workspaceId));
    return promise;
  }

  /**
   * Fetch permissions for a project based located on the given workspace id and path
   * @param workspaceId the workspace ID to use
   * @param path the path of the project
   * @param data the project body description
   * @returns {$promise|*|T.$promise}
   */
  fetchPermissions(workspaceId, path) {
    let promise = this.remoteProjectsAPI.getPermissions({workspaceId: workspaceId, path: path}).$promise;

    let storePermissionsPromise = promise.then((permissions) => {

      var workspaceMap = this.permissionsPerWorkspaceMap.get(workspaceId);
      if (!workspaceMap) {
        workspaceMap = new Map();
        this.permissionsPerWorkspaceMap.set(workspaceId, workspaceMap);
      }

      var projectMap = workspaceMap.get(path);
      if (!projectMap) {
        projectMap = new Map();
        workspaceMap.set(path, projectMap);
      }
      projectMap.set(path, permissions);

      return permissions;
    });

    // update permissions with users

    let updateUsersPromise = storePermissionsPromise.then((permissions) => {
      var allpromises = [];
      //allpromises.push(storePermissionsPromise);

      permissions.forEach((permission)=> {
        if (permission.principal.type === 'USER' && permission.principal.name === 'any') {
          permission.principal.email = 'Any user';
          permission.principal.fullname = 'Public project sharing';
        } else if (permission.principal.type === 'USER') {
          var principalId = permission.principal.name;
          let userPromise = this.cheUser.fetchUserId(principalId);
          let profilePromise = this.cheProfile.fetchProfileId(principalId);
          let updateProfilePromise = profilePromise.then(() => {
            var profile = this.cheProfile.getProfileFromId(principalId);
            permission.principal.fullname = this.getFullName(profile);

          }, (error) => {
            if (error.status === 304) {
              // get from cache
              var profile = this.cheProfile.getProfileFromId(principalId);
              permission.principal.fullname = this.getFullName(profile);

            } else {
              // unable to get user
              permission.principal.fullname = permission.principal.name;
            }
          });


          let updatedUserPromise = userPromise.then(() => {
            var user = this.cheUser.getUserFromId(principalId);
            permission.principal.email = user.email;

          }, (error) => {
            if (error.status === 304) {
              // get from cache
              var user = this.cheUser.getUserFromId(principalId);
              permission.principal.email = user.email;

            } else {
              // unable to get user
              permission.principal.email = permission.principal.name;
            }
          });


          allpromises.push(updatedUserPromise);
          allpromises.push(updateProfilePromise);
        }
      });
      return this.$q.all(allpromises);
    });


    return updateUsersPromise;

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


  updatePermissions(workspaceId, path, data) {
    let promise = this.remoteProjectsAPI.updatePermissions({workspaceId: workspaceId, path: path}, data).$promise;
    return promise;

  }

  /**
   * Return last retrieved permissions
   * @param workspaceId
   * @param path
   * @returns {*}
   */
  getPermissions(workspaceId, path) {
    var workspaceMap = this.permissionsPerWorkspaceMap.get(workspaceId);
    if (!workspaceMap) {
      return [];
    }
    var projectMap = workspaceMap.get(path);
    if (!projectMap) {
      return [];
    }
    return projectMap.get(path);

  }


  /**
   * Gets all projects that are currently monitored
   * @returns {Array}
   */
  getAllProjects() {
    return this.projects;
  }

  /**
   * The projects per workspace id
   * @returns {CheProject.projectsPerWorkspace|*}
   */
  getProjectsByWorkspace() {
    return this.projectsPerWorkspace;
  }

  /**
   * The projects per workspace id
   * @returns {Map}
   */
  getProjectsByWorkspaceMap() {
    return this.projectsPerWorkspaceMap;
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


    // update list of projects
    let promiseUpdateProjects = promiseUpdateProjectDetails.then(() => {
      this.fetchProjectsForWorkspaceId(workspaceId);
    });

    return promiseUpdateProjects;
  }

  rename(workspaceId, projectName, newProjectName) {
    let promise = this.remoteProjectsAPI.rename({workspaceId: workspaceId, path: projectName, name: newProjectName}, null).$promise;

    return promise;
  }

  remove(workspaceId, projectName) {
    let promiseDelete = this.remoteProjectsAPI.remove({workspaceId: workspaceId, path: projectName}).$promise;
    // update list of projects
    let promiseUpdateProjects = promiseDelete.then(() => {
      this.fetchProjectsForWorkspaceId(workspaceId);
    });


    return promiseUpdateProjects;
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
