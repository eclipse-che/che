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
 * This class is providing helper methods for simulating a fake HTTP backend simulating
 * @author Florent Benoit
 */
export class CheHttpBackend {
  private httpBackend: ng.IHttpBackendService;
  private projectsPerWorkspace: Map<string, any>;
  private workspaces: Map<string, any>;
  private profilesMap: any;
  private projectDetailsMap: Map<string, any>;
  private remoteGitUrlArraysMap: Map<string, any>;
  private localGitUrlsMap: Map<string, any>;
  private remoteSvnUrlsMap: Map<string, any>;
  private projectTypesWorkspaces: Map<string, any>;
  private workspaceAgentMap: Map<string, any>;
  private stacks: che.IStack[];
  private defaultProfile: any;
  private defaultProfilePrefs: any;
  private defaultBranding: any;
  private defaultPreferences: any;
  private defaultUser: che.IUser;
  private userIdMap: Map<string, che.IUser>;
  private userEmailMap: Map<string, che.IUser>;
  private factoriesMap: Map<string, che.IFactory>;
  private pageMaxItem: number;
  private pageSkipCount: number;



  private   isAutoSnapshot: boolean = false;
  private   isAutoRestore: boolean = false;

  /**
   * Constructor to use
   */
  constructor(private $httpBackend, private cheAPIBuilder) {
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

    this.defaultUser = {};
    this.userIdMap = new Map();
    this.userEmailMap = new Map();
    this.factoriesMap = new Map();
    this.pageMaxItem = 5;
    this.pageSkipCount = 0;

    this.defaultProfile = cheAPIBuilder.getProfileBuilder().withId('idDefaultUser').withEmail('eclipseChe@eclipse.org').withFirstName('FirstName').withLastName('LastName').build();
    this.defaultProfilePrefs = {};
    this.defaultBranding = {};
  }


  /**
   * Setup all data that should be retrieved on calls
   */
  setup() {
    // add the remote call
    let workspaceReturn = [];
    let workspaceKeys = this.workspaces.keys();
    for (let key of workspaceKeys) {
      let tmpWorkspace = this.workspaces.get(key);
      workspaceReturn.push(tmpWorkspace);
      this.addWorkspaceAgent(key, tmpWorkspace.runtime);
      this.httpBackend.when('GET', '/api/workspace/' + key).respond(tmpWorkspace);
    }

    let workspacSettings = {
      'che.workspace.auto_snapshot': this.isAutoSnapshot,
      'che.workspace.auto_restore': this.isAutoRestore
    };
    this.httpBackend.when('GET', '/api/workspace/settings').respond(200, workspacSettings);

    this.httpBackend.when('OPTIONS', '/api/').respond({});
    this.httpBackend.when('GET', '/api/workspace/settings').respond({});

    this.httpBackend.when('GET', '/api/workspace').respond(workspaceReturn);

    this.httpBackend.when('GET', '/api/stack?maxItems=50').respond(this.stacks);

    let projectTypeKeys = this.projectTypesWorkspaces.keys();
    for (let key of projectTypeKeys) {
      this.httpBackend.when('GET', this.workspaceAgentMap.get(key) + '/project-type').respond(this.projectTypesWorkspaces.get(key));
    }

    //profiles
    this.httpBackend.when('GET', '/api/profile').respond(this.defaultProfile);
    let profileKeys = this.profilesMap.keys();
    for (let key of profileKeys) {
      this.httpBackend.when('GET', '/api/profile/' + key).respond(this.profilesMap.get(key));
    }

    //preferences
    this.httpBackend.when('GET', '/api/preferences').respond(this.defaultPreferences);
    this.httpBackend.when('DELETE', '/api/preferences').respond(200, {});

    /// project details
    let projectDetailsKeys = this.projectDetailsMap.keys();
    for (let projectKey of projectDetailsKeys) {
      let workspaceKey = projectKey.split('/')[0];
      let projectId = projectKey.split('/')[1];
      this.httpBackend.when('GET', this.workspaceAgentMap.get(workspaceKey) + '/project/' + projectId).respond(this.projectDetailsMap.get(projectKey));
    }

    // branding
    this.httpBackend.when('GET', 'assets/branding/product.json').respond(this.defaultBranding);

    this.httpBackend.when('POST', '/api/analytics/log/session-usage').respond(200, {});

    // change password
    this.httpBackend.when('POST', '/api/user/password').respond(() => {
      return [200, {success: true, errors: []}];
    });

    // create new user
    this.httpBackend.when('POST', '/api/user').respond(() => {
      return [200, {success: true, errors: []}];
    });

    this.httpBackend.when('GET', '/api/user').respond(this.defaultUser);

    let userIdKeys = this.userIdMap.keys();
    for (let key of userIdKeys) {
      this.httpBackend.when('GET', '/api/user/' + key).respond(this.userIdMap.get(key));
    }

    let userEmailKeys = this.userEmailMap.keys();
    for (let key of userEmailKeys) {
      this.httpBackend.when('GET', '/api/user/find?email=' + key).respond(this.userEmailMap.get(key));
    }
  }

  /**
   * Set workspace auto snapshot status
   * @param isAutoSnapshot {boolean}
   */
  setWorkspaceAutoSnapshot(isAutoSnapshot: boolean) {
    this.isAutoSnapshot = isAutoSnapshot;
  }

  /**
   * Set workspace auto restore status
   * @param isAutoRestore {boolean}
   */
  setWorkspaceAutoRestore(isAutoRestore: boolean) {
    this.isAutoRestore = isAutoRestore;
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

    let workspaceFound = this.workspaces.get(workspace.id);
    if (!workspaceFound) {
      this.workspaces.set(workspace.id, workspace);
      workspaceFound = workspace;
    }

    let existingProjects = workspaceFound.config.projects;
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
        this.httpBackend.when('PUT', this.workspaceAgentMap.get(workspace.id) + '/project/' + project.name).respond(200, {});
        this.httpBackend.when('GET', this.workspaceAgentMap.get(workspace.id) + '/project/resolve/' + project.name).respond(200, []);
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
    let svnInfo: {items: any[]} = {};
    svnInfo.items = [{uRL: this.remoteSvnUrlsMap.get(workspaceId + projectPath)}];

    this.httpBackend.when('POST', this.workspaceAgentMap.get(workspaceId) + '/svn/info?workspaceId='+workspaceId).respond(svnInfo);
  }

  /**
   * Setup Backend for factories
   */
  factoriesBackendSetup() {
    this.setup();

    let allFactories = [];
    let pageFactories = [];

    let factoriesKeys = this.factoriesMap.keys();
    for (let key of factoriesKeys) {
      let factory = this.factoriesMap.get(key);
      this.httpBackend.when('GET', '/api/factory/' + factory.id).respond(factory);
      this.httpBackend.when('DELETE', '/api/factory/' + factory.id).respond(() => {
        return [200, {success: true, errors: []}];
      });
      allFactories.push(factory);
    }

    if (this.defaultUser) {
      this.httpBackend.when('GET', '/api/user').respond(this.defaultUser);

      if (allFactories.length >  this.pageSkipCount) {
        if(allFactories.length > this.pageSkipCount + this.pageMaxItem) {
          pageFactories = allFactories.slice(this.pageSkipCount, this.pageSkipCount + this.pageMaxItem);
        } else {
          pageFactories = allFactories.slice(this.pageSkipCount);
        }
      }
      this.httpBackend.when('GET', '/api/factory/find?creator.userId=' + this.defaultUser.id + '&maxItems=' + this.pageMaxItem + '&skipCount=' + this.pageSkipCount).respond(pageFactories);
    }
  }

  /**
   * Add the given factory
   * @param factory
   */
  addUserFactory(factory) {
    this.factoriesMap.set(factory.id, factory);
  }

  /**
   * Sets max objects on response
   * @param pageMaxItem
   */
  setPageMaxItem(pageMaxItem) {
    this.pageMaxItem = pageMaxItem;
  }

  /**
   * Sets skip count of values
   * @param pageSkipCount
   */
  setPageSkipCount(pageSkipCount) {
    this.pageSkipCount = pageSkipCount;
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
  addUserById(user) {
    this.userIdMap.set(user.id, user);
  }

  /**
   * Add the given user to userEmailMap
   * @param user
   */
  addUserEmail(user) {
    this.userEmailMap.set(user.email, user);
  }
}
