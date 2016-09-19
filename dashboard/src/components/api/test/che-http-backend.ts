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
    this.profilesMap = new Map();
    this.projectDetailsMap = new Map();
    this.remoteGitUrlArraysMap = new Map();
    this.localGitUrlsMap = new Map();
    this.remoteSvnUrlsMap = new Map();
    this.projectTypesWorkspaces = new Map();
    this.workspaceAgentMap = new Map();
    this.stacks = [];

    this.defaultProfile = cheAPIBuilder.getProfileBuilder().withId('idDefaultUser').withEmail('eclipseChe@eclipse.org').withFirstName('FirstName').withLastName('LastName').build();
    this.defaultProfilePrefs = {};
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
      this.addWorkspaceAgent(key, tmpWorkspace.runtime);
      this.httpBackend.when('GET', '/api/workspace/' + key).respond(tmpWorkspace);
    }

    this.httpBackend.when('GET', '/api/workspace').respond(workspaceReturn);

    this.httpBackend.when('GET', '/api/stack?maxItems=50').respond(this.stacks);

    var projectTypeKeys = this.projectTypesWorkspaces.keys();
    for (let key of projectTypeKeys) {
      this.httpBackend.when('GET', this.workspaceAgentMap.get(key) + '/project-type').respond(this.projectTypesWorkspaces.get(key));
    }

    //profiles
    this.httpBackend.when('GET', '/api/profile').respond(this.defaultProfile);
    var profileKeys = this.profilesMap.keys();
    for (let key of profileKeys) {
      this.httpBackend.when('GET', '/api/profile/' + key).respond(this.profilesMap.get(key));
    }

    //preferences
    this.httpBackend.when('GET', '/api/preferences').respond(this.defaultPreferences);
    this.httpBackend.when('DELETE', '/api/preferences').respond();

    /// project details
    var projectDetailsKeys = this.projectDetailsMap.keys();
    for (let projectKey of projectDetailsKeys) {
      let workspaceKey = projectKey.split('/')[0];
      let projectId = projectKey.split('/')[1];
      this.httpBackend.when('GET', this.workspaceAgentMap.get(workspaceKey) + '/project/' + projectId).respond(this.projectDetailsMap.get(projectKey));
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
   * Add the given stacks on this backend
   * @param stacks an array of stacks
   */
  addStacks(stacks) {
    this.stacks.push(...stacks);
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
    this.httpBackend.when('GET', this.workspaceAgentMap.get(workspace.id) + '/project/').respond(this.projectsPerWorkspace.get(workspace.id));

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
   * Add the given project types
   * @param workspaceId the workspaceId of the runt
   * @param runtime runtime to add
   */
  addWorkspaceAgent(workspaceId, runtime) {
    if (runtime && runtime.links) {
      runtime.links.forEach((link) => {
        if (link.rel === 'wsagent') {
          this.workspaceAgentMap.set(workspaceId, link.href);
        }
      });
    }
  }

  /**
   * Add the given profile
   * @param profile
   */
  addDefaultProfile(profile) {
    this.defaultProfile = profile;
  }

  /**
   * Add the given preferences
   * @param preferences
   */
  addDefaultPreferences(preferences) {
    this.defaultPreferences = preferences;
  }

  /**
   * Add the given preferences
   * @param preferences
   */
  setPreferences(preferences) {
    this.httpBackend.when('POST', '/api/preferences').respond(preferences);
    this.defaultPreferences = preferences;
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
    this.httpBackend.when('PUT', '/api/profile/attributes').respond(attributes);
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
    this.httpBackend.when('PUT', '/project/' + workspaceId + '/' + projectName).respond(newProjectDetails);
  }

  /**
   * Add the fetch project details
   * @param workspaceId the id of project workspace
   * @param projectName the project name
   */
  addFetchProjectDetails(workspaceId, projectName) {
    this.httpBackend.when('GET', '/project/' + projectName)
      .respond(this.projectDetailsMap.get(workspaceId + '/' + projectName));
  }

  /**
   * Add the updated project name
   * @param workspaceId the id of project workspace
   * @param projectName the project name
   * @param newProjectName the new project name
   */
  addUpdatedProjectName(workspaceId, projectName, newProjectName) {
    this.httpBackend.when('POST', '/project/rename/' + projectName + '?name=' + newProjectName).respond(newProjectName);
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
    this.httpBackend.when('GET', this.workspaceAgentMap.get(workspaceId) + '/git/read-only-url?projectPath=' + projectPath)
      .respond(this.localGitUrlsMap.get(workspaceId + projectPath));
  }

  /**
   * Get remote array of git url
   * @param workspaceId
   * @param projectPath
   */
  getRemoteGitUrlArray(workspaceId, projectPath) {
    this.httpBackend.when('POST', this.workspaceAgentMap.get(workspaceId) + '/git/remote-list?projectPath=' + projectPath)
      .respond(this.remoteGitUrlArraysMap.get(workspaceId + projectPath));
  }

  /**
   * Get remote svn url
   * @param workspaceId
   * @param projectPath
   */
  getRemoteSvnUrl(workspaceId, projectPath) {
    var svnInfo = {};
    svnInfo.items = [{uRL: this.remoteSvnUrlsMap.get(workspaceId + projectPath)}];

    this.httpBackend.when('POST', this.workspaceAgentMap.get(workspaceId) + '/svn/info?workspaceId='+workspaceId).respond(svnInfo);
  }

}
