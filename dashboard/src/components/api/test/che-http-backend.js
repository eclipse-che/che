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
 * This class is providing helper methods for simulating a fake HTTP backend simulating
 * @author Florent Benoit
 */
export class CheHttpBackend {

  /**
   * Constructor to use
   */
  constructor($httpBackend, cheAPIBuilder) {
    this.httpBackend = $httpBackend;
    this.projectsPerWorkspace = new Map();
    this.workspaces = new Map();
    this.userIdMap = new Map();
    this.userEmailMap = new Map();
    this.profilesMap = new Map();
    this.projectDetailsMap = new Map();
    this.projectPermissionsMap = new Map();
    this.remoteGitUrlArraysMap = new Map();
    this.localGitUrlsMap = new Map();
    this.remoteSvnUrlsMap = new Map();
    this.projectTypesWorkspaces = new Map();

    this.memberships = [];


    this.defaultUser = cheAPIBuilder.getUserBuilder().withId('idDefaultUser').withEmail('eclipseChe@eclipse.org').build();
    this.defaultProfile = cheAPIBuilder.getProfileBuilder().withId('idDefaultUser').withEmail('eclipseChe@eclipse.org').withFirstName('FirstName').withLastName('LastName').build();
    this.defaultProfilePrefs = {onBoardingFlowCompleted: 'true'};
    this.defaultBranding = {};
  }


  /**
   * Setup all data that should be retrieved on calls
   */
  setup() {
    // add the remote call
    var workspaceReturn = [];
    var workspaceKeys = this.workspaces.keys();
    for (let key of workspaceKeys) {
      var tmpWorkspace = this.workspaces.get(key);
      workspaceReturn.push(tmpWorkspace);
    }

    this.httpBackend.when('GET', '/api/workspace').respond(workspaceReturn);

    var projectTypeKeys = this.projectTypesWorkspaces.keys();
    for (let key of projectTypeKeys) {
      this.httpBackend.when('GET', '/api/project-type/' + key).respond(this.projectTypesWorkspaces.get(key));
    }

    //memberships:
    this.httpBackend.when('GET', '/api/account').respond(this.memberships);

    //users
    this.httpBackend.when('GET', '/api/user').respond(this.defaultUser);
    var userIdKeys = this.userIdMap.keys();
    for (let key of userIdKeys) {
      this.httpBackend.when('GET', '/api/user/' + key).respond(this.userIdMap.get(key));
    }
    var userEmailKeys = this.userEmailMap.keys();
    for (let key of userEmailKeys) {
      this.httpBackend.when('GET', '/api/user/find?alias=' + key).respond(this.userEmailMap.get(key));
    }
    this.httpBackend.when('GET', '/api/user/inrole?role=admin&scope=system&scopeId=').respond(false);
    this.httpBackend.when('GET', '/api/user/inrole?role=user&scope=system&scopeId=').respond(true);


    //profiles
    this.httpBackend.when('GET', '/api/profile').respond(this.defaultProfile);
    this.httpBackend.when('GET', '/api/profile/prefs').respond(this.defaultProfilePrefs);
    var profileKeys = this.profilesMap.keys();
    for (let key of profileKeys) {
      this.httpBackend.when('GET', '/api/profile/' + key).respond(this.profilesMap.get(key));
    }

    /// project details
    var projectDetailsKeys = this.projectDetailsMap.keys();
    for (let projectKey of projectDetailsKeys) {
      this.httpBackend.when('GET', '/api/project/' + projectKey).respond(this.projectDetailsMap.get(projectKey));
    }

    // permissions
    var projectPermissionsKeys = this.projectPermissionsMap.keys();
    for (let key of projectPermissionsKeys) {
      this.httpBackend.when('GET', '/api/project/' + key.workspaceId + '/permissions/' + key.projectName).respond(this.projectPermissionsMap.get(key));
    }

    // branding
    this.httpBackend.when('GET', 'assets/branding/product.json').respond(this.defaultBranding);

    this.httpBackend.when('POST', '/api/analytics/log/session-usage').respond();

  }


  /**
   * Add the given workspaces on this backend
   * @param workspaces an array of workspaces
   */
  addWorkspaces(workspaces) {
    workspaces.forEach((workspace) => {

      // if there is a workspace ID, add empty projects
      if (workspace.id) {
        this.projectsPerWorkspace.set(workspace.id, []);
      }

      this.workspaces.set(workspace.id, workspace);
    });

  }


  /**
   * Adds the given projects for the given workspace
   * @param workspace the workspace to use for adding projects
   * @param projects the projects to add
   */
  addProjects(workspace, projects) {
    // we need the workspaceReference ID
    if (!workspace.id) {
      throw 'no workspace id set';
    }

    var workspaceFound = this.workspaces.get(workspace.id);
    if (!workspaceFound) {
      this.workspaces.set(workspace.id, workspace);
      workspaceFound = workspace;
    }

    var existingProjects = workspaceFound.config.projects;
    if (!existingProjects) {
      workspaceFound.config.projects = [];
    }
    if (projects) {
      projects.forEach((project) => {
        existingProjects.push(project);
      });
    }


    // empty array if not yet defined
    if (!this.projectsPerWorkspace.get(workspace.id)) {
      this.projectsPerWorkspace.set(workspace.id, []);
    }

    // add each project
    projects.forEach((project) => {
        this.projectsPerWorkspace.get(workspace.id).push(project);
      }
    );

    // add call to the backend
    this.httpBackend.when('GET', '/api/project/' + workspace.id).respond(this.projectsPerWorkspace.get(workspace.id));

  }

  /**
   * Add the given project types
   * @param workspaceId the workspaceId of the project types
   * @param projectTypes
   */
  addProjectTypes(workspaceId, projectTypes) {
    this.projectTypesWorkspaces.set(workspaceId, projectTypes);
  }

  /**
   * Add the given user
   * @param user
   */
  setDefaultUser(user) {
    this.defaultUser = user;
  }

  /**
   * Add the given user to userIdMap
   * @param user
   */
  addUserId(user) {
    this.userIdMap.set(user.id, user);
  }

  /**
   * Add the given user to userEmailMap
   * @param user
   */
  addUserEmail(user) {
    this.userEmailMap.set(user.email, user);
  }

  /**
   * Set new user password
   */
  setPassword() {
    this.httpBackend.when('POST', '/api/user/password').respond(() => {
      return [200, {success: true, errors: []}];
    });
  }

  /**
   * Create new user
   */
  createUser() {
    this.httpBackend.when('POST', '/api/user/create').respond(() => {
      return [200, {success: true, errors: []}];
    });
  }

  /**
   * Add membership of the current user
   * @param membership
   */
  addMembership(membership) {
    this.memberships.push(membership);
  }

  /**
   * Add the given profile
   * @param profile
   */
  addDefaultProfile(profile) {
    this.defaultProfile = profile;
  }

  /**
   * Add the given profile
   * @param profile
   */
  addProfileId(profile) {
    this.profilesMap.put(profile.id, profile);
  }


  /**
   * Set attributes of the current user
   * @param attributes
   */
  setAttributes(attributes) {
    this.httpBackend.when('POST', '/api/profile').respond(attributes);
    this.defaultProfile.attributes = attributes;
  }

  /**
   * Add the given project templates
   * @param projectTemplates
   */
  addProjectTemplates(projectTemplates) {
    this.httpBackend.when('GET', '/api/project-template/all').respond(projectTemplates);
  }

  /**
   * Gets the internal http backend used
   * @returns {CheHttpBackend.httpBackend|*}
   */
  getHttpBackend() {
    return this.httpBackend;
  }

  /**
   * Add the project details
   * @param projectDetails the project details
   */
  addProjectDetails(projectDetails) {
    this.projectDetailsMap.set(projectDetails.workspaceId + '/' + projectDetails.name, projectDetails);
  }

  /**
   * Add the updated project details
   * @param workspaceId the id of project workspace
   * @param projectName
   * @param newProjectDetails
   */
  addUpdatedProjectDetails(workspaceId, projectName, newProjectDetails) {
    this.httpBackend.when('PUT', '/api/project/' + workspaceId + '/' + projectName).respond(newProjectDetails);
  }

  /**
   * Add the fetch project details
   * @param workspaceId the id of project workspace
   * @param projectName the project name
   */
  addFetchProjectDetails(workspaceId, projectName) {
    this.httpBackend.when('GET', '/api/project/' + workspaceId + '/' + projectName)
      .respond(this.projectDetailsMap.get(workspaceId + '/' + projectName));
  }

  /**
   * Add the updated project name
   * @param workspaceId the id of project workspace
   * @param projectName the project name
   * @param newProjectName the new project name
   */
  addUpdatedProjectName(workspaceId, projectName, newProjectName) {
    this.httpBackend.when('POST', '/api/project/' + workspaceId + '/rename/' + projectName + '?name=' + newProjectName).respond(newProjectName);
  }

  addPermissions(workspaceId, projectName, permissions) {
    var key = {workspaceId: workspaceId, projectName: projectName};
    this.projectPermissionsMap.set(key, permissions);
  }

  /**
   * Add the given remote array of git url to map
   * @param workspaceId
   * @param projectPath
   * @param remoteArray
   */
  addRemoteGitUrlArray(workspaceId, projectPath, remoteArray) {
    this.remoteGitUrlArraysMap.set(workspaceId + projectPath, remoteArray);
  }

  /**
   * Add the given local git url to map
   * @param workspaceId
   * @param projectPath
   * @param localUrl
   */
  addLocalGitUrl(workspaceId, projectPath, localUrl) {
    this.localGitUrlsMap.set(workspaceId + projectPath, localUrl);
  }

  /**
   * Add the given local svn url to map
   * @param workspaceId
   * @param projectPath
   * @param localUrl
   */
  addRemoteSvnUrl(workspaceId, projectPath, localUrl) {
    this.remoteSvnUrlsMap.set(workspaceId + projectPath, localUrl);
  }

  /**
   * Get local git url
   * @param workspaceId
   * @param projectPath
   */
  getLocalGitUrl(workspaceId, projectPath) {
    this.httpBackend.when('GET', '/api/git/' + workspaceId + '/read-only-url?projectPath=' + projectPath)
      .respond(this.localGitUrlsMap.get(workspaceId + projectPath));
  }

  /**
   * Get remote array of git url
   * @param workspaceId
   * @param projectPath
   */
  getRemoteGitUrlArray(workspaceId, projectPath) {
    this.httpBackend.when('POST', '/api/git/' + workspaceId + '/remote-list?projectPath=' + projectPath)
      .respond(this.remoteGitUrlArraysMap.get(workspaceId + projectPath));
  }

  /**
   * Get remote svn url
   * @param workspaceId
   * @param projectPath
   */
  getRemoteSvnUrl(workspaceId, projectPath) {
    var svnInfo = {};
    svnInfo.repositoryUrl = this.remoteSvnUrlsMap.get(workspaceId + projectPath);

    this.httpBackend.when('POST', '/api/svn/' + workspaceId + '/info').respond(svnInfo);
  }

}

