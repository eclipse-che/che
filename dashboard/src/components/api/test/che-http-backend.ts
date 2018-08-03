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
import {CheAPIBuilder} from '../builder/che-api-builder.factory';

/**
 * This class is providing helper methods for simulating a fake HTTP backend simulating
 * @author Florent Benoit
 */
export class CheHttpBackend {
  static $inject = ['$httpBackend', 'cheAPIBuilder'];

  private $httpBackend: ng.IHttpBackendService;
  private projectsPerWorkspace: Map<string, any>;
  private workspaces: Map<string, any>;
  private profilesMap: Map<string, any>;
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

  private teamsMap: Map<string, che.ITeam>;
  private organizationsMap: Map<string, che.IOrganization>;
  private permissionsMap: Map<string, Array<che.IPermissions>>;
  private resourcesMap: Map<string, Map<string, any>>;

  private installersMap: Map<string, che.IAgent> = new Map();
  private installersList: Array<che.IAgent> = [];

  /**
   * Constructor to use
   */
  constructor($httpBackend: ng.IHttpBackendService, cheAPIBuilder: CheAPIBuilder) {
    this.$httpBackend = $httpBackend;
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

    this.teamsMap = new Map();
    this.organizationsMap = new Map();
    this.permissionsMap = new Map();
    this.resourcesMap = new Map();


    this.defaultUser = <che.IUser>{
      id: '',
      aliases: [],
      name: '',
      email: ''
    };
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
  setup(): void {
    this.$httpBackend.when('OPTIONS', '/api/').respond({});
    this.$httpBackend.when('GET', '/api/').respond(200, {rootResources: []});

    this.$httpBackend.when('GET', '/api/keycloak/settings').respond(404);
    this.$httpBackend.when('GET', '/workspace-loader/').respond(404);

    // add the remote call
    let workspaceReturn = [];
    let workspaceKeys = this.workspaces.keys();
    for (let key of workspaceKeys) {
      let tmpWorkspace = this.workspaces.get(key);
      workspaceReturn.push(tmpWorkspace);
      this.addWorkspaceAgent(key, tmpWorkspace.runtime);

      // get by ID
      this.$httpBackend.when('GET', '/api/workspace/' + key).respond(200, tmpWorkspace);
      // get by namespace/workspaceName
      this.$httpBackend.when('GET', `/api/workspace/${tmpWorkspace.namespace}/${tmpWorkspace.config.name}`).respond(200, tmpWorkspace);

      this.$httpBackend.when('DELETE', '/api/workspace/' + key).respond(200);
    }

    this.$httpBackend.when('GET', '/api/workspace/settings').respond({});

    this.$httpBackend.when('GET', '/api/workspace').respond(workspaceReturn);

    this.$httpBackend.when('GET', '/api/stack?maxItems=50').respond(this.stacks);

    let projectTypeKeys = this.projectTypesWorkspaces.keys();
    for (let key of projectTypeKeys) {
      this.$httpBackend.when('GET', this.workspaceAgentMap.get(key) + '/project-type').respond(this.projectTypesWorkspaces.get(key));
    }

    // profiles
    this.$httpBackend.when('GET', '/api/profile').respond(this.defaultProfile);
    let profileKeys = this.profilesMap.keys();
    for (let key of profileKeys) {
      this.$httpBackend.when('GET', '/api/profile/' + key).respond(this.profilesMap.get(key));
    }

    // preferences
    this.$httpBackend.when('GET', '/api/preferences').respond(this.defaultPreferences);
    this.$httpBackend.when('DELETE', '/api/preferences').respond(200, {});

    /// project details
    let projectDetailsKeys = this.projectDetailsMap.keys();
    for (let projectKey of projectDetailsKeys) {
      let workspaceKey = projectKey.split('/')[0];
      let projectId = projectKey.split('/')[1];
      this.$httpBackend.when('GET', this.workspaceAgentMap.get(workspaceKey) + '/project/' + projectId).respond(this.projectDetailsMap.get(projectKey));
    }

    // branding
    this.$httpBackend.when('GET', 'assets/branding/product.json').respond(this.defaultBranding);

    this.$httpBackend.when('POST', '/api/analytics/log/session-usage').respond(200, {});

    // change password
    this.$httpBackend.when('POST', '/api/user/password').respond(() => {
      return [200, {success: true, errors: []}];
    });

    // create new user
    this.$httpBackend.when('POST', '/api/user').respond(() => {
      return [200, {success: true, errors: []}];
    });

    this.$httpBackend.when('GET', '/api/user').respond(this.defaultUser);

    let userIdKeys = this.userIdMap.keys();
    for (let key of userIdKeys) {
      this.$httpBackend.when('GET', '/api/user/' + key).respond(this.userIdMap.get(key));
    }

    let userEmailKeys = this.userEmailMap.keys();
    for (let key of userEmailKeys) {
      this.$httpBackend.when('GET', '/api/user/find?email=' + key).respond(this.userEmailMap.get(key));
    }
    this.$httpBackend.when('GET', /\/_app\/compilation-mappings(\?.*$)?/).respond(200, '');
  }

  /**
   * Add the given workspaces on this backend
   * @param workspaces an array of workspaces
   */
  addWorkspaces(workspaces: any[]): void {
    workspaces.forEach((workspace: any) => {

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
  addStacks(stacks: any): void {
    this.stacks.push(...stacks);
  }


  /**
   * Adds the given projects for the given workspace
   * @param workspace the workspace to use for adding projects
   * @param projects the projects to add
   */
  addProjects(workspace: any, projects: any[]): void {
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
      projects.forEach((project: any) => {
        existingProjects.push(project);
      });
    }


    // empty array if not yet defined
    if (!this.projectsPerWorkspace.get(workspace.id)) {
      this.projectsPerWorkspace.set(workspace.id, []);
    }

    // add each project
    projects.forEach((project: any) => {
        this.projectsPerWorkspace.get(workspace.id).push(project);
        this.$httpBackend.when('PUT', this.workspaceAgentMap.get(workspace.id) + '/project/' + project.name).respond(200, {});
        this.$httpBackend.when('GET', this.workspaceAgentMap.get(workspace.id) + '/project/resolve/' + project.name).respond(200, []);
      }
    );

    // add call to the backend
    this.$httpBackend.when('GET', this.workspaceAgentMap.get(workspace.id) + '/project/').respond(this.projectsPerWorkspace.get(workspace.id));

  }

  /**
   * Add the given project types
   * @param workspaceId the workspaceId of the project types
   * @param projectTypes
   */
  addProjectTypes(workspaceId: string, projectTypes: any[]): void {
    this.projectTypesWorkspaces.set(workspaceId, projectTypes);
  }

  /**
   * Add the given project types
   * @param workspaceId the workspaceId of the runt
   * @param runtime runtime to add
   */
  addWorkspaceAgent(workspaceId: any, runtime: any): void {
    if (runtime && runtime.links) {
      runtime.links.forEach((link: any) => {
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
  addDefaultProfile(profile: any): void {
    this.defaultProfile = profile;
  }

  /**
   * Add the given preferences
   * @param preferences
   */
  addDefaultPreferences(preferences: any): void {
    this.defaultPreferences = preferences;
  }

  /**
   * Add the given preferences
   * @param preferences
   */
  setPreferences(preferences: any): void {
    this.$httpBackend.when('POST', '/api/preferences').respond(preferences);
    this.defaultPreferences = preferences;
  }

  /**
   * Add the given profile
   * @param profile
   */
  addProfileId(profile: any): void {
    this.profilesMap.set(profile.id, profile);
  }


  /**
   * Set profile attributes
   * @param attributes {che.IProfileAttributes}
   * @param userId {string}
   */
  setAttributes(attributes: che.IProfileAttributes, userId?: string): void {
    if (angular.isUndefined(userId)) {
      this.$httpBackend.when('PUT', '/api/profile/attributes').respond({attributes: attributes});
      this.defaultProfile.attributes = attributes;
      return;
    }
    this.$httpBackend.when('PUT', `/api/profile/${userId}/attributes`).respond({userId: userId, attributes: attributes});
  }

  /**
   * Add the given project templates
   * @param projectTemplates
   */
  addProjectTemplates(projectTemplates: any): void {
    this.$httpBackend.when('GET', '/api/project-template/all').respond(projectTemplates);
  }

  /**
   * Gets the internal http backend used
   * @returns {CheHttpBackend.$httpBackend|*}
   */
  getHttpBackend(): ng.IHttpBackendService {
    return this.$httpBackend;
  }

  /**
   * Add the project details
   * @param projectDetails the project details
   */
  addProjectDetails(projectDetails: any): void {
    this.projectDetailsMap.set(projectDetails.workspaceId + '/' + projectDetails.name, projectDetails);
  }

  /**
   * Add the updated project details
   * @param workspaceId the id of project workspace
   * @param projectName
   * @param newProjectDetails
   */
  addUpdatedProjectDetails(workspaceId: string, projectName: string, newProjectDetails: any): void {
    this.$httpBackend.when('PUT', '/project/' + workspaceId + '/' + projectName).respond(newProjectDetails);
  }

  /**
   * Add the fetch project details
   * @param workspaceId the id of project workspace
   * @param projectName the project name
   */
  addFetchProjectDetails(workspaceId: string, projectName: string): void {
    this.$httpBackend.when('GET', '/project/' + projectName)
      .respond(this.projectDetailsMap.get(workspaceId + '/' + projectName));
  }

  /**
   * Add the updated project name
   * @param workspaceId the id of project workspace
   * @param projectName the project name
   * @param newProjectName the new project name
   */
  addUpdatedProjectName(workspaceId: string, projectName: string, newProjectName: string): void {
    this.$httpBackend.when('POST', '/project/rename/' + projectName + '?name=' + newProjectName).respond(newProjectName);
  }

  /**
   * Add the given remote array of git url to map
   * @param workspaceId
   * @param projectPath
   * @param remoteArray
   */
  addRemoteGitUrlArray(workspaceId: string, projectPath: string, remoteArray: any[]): void {
    this.remoteGitUrlArraysMap.set(workspaceId + projectPath, remoteArray);
  }

  /**
   * Add the given local git url to map
   * @param workspaceId
   * @param projectPath
   * @param localUrl
   */
  addLocalGitUrl(workspaceId: string, projectPath: string, localUrl: string): void {
    this.localGitUrlsMap.set(workspaceId + projectPath, localUrl);
  }

  /**
   * Add the given local svn url to map
   * @param workspaceId
   * @param projectPath
   * @param localUrl
   */
  addRemoteSvnUrl(workspaceId: string, projectPath: string, localUrl: string): void {
    this.remoteSvnUrlsMap.set(workspaceId + projectPath, localUrl);
  }

  /**
   * Get local git url
   * @param workspaceId
   * @param projectPath
   */
  getLocalGitUrl(workspaceId: string, projectPath: string): void {
    this.$httpBackend.when('GET', this.workspaceAgentMap.get(workspaceId) + '/git/read-only-url?projectPath=' + projectPath)
      .respond(200, this.localGitUrlsMap.get(workspaceId + projectPath));
  }

  /**
   * Get remote array of git url
   * @param workspaceId
   * @param projectPath
   */
  getRemoteGitUrlArray(workspaceId: string, projectPath: string): void {
    this.$httpBackend.when('POST', this.workspaceAgentMap.get(workspaceId) + '/git/remote-list?projectPath=' + projectPath)
      .respond(this.remoteGitUrlArraysMap.get(workspaceId + projectPath));
  }

  /**
   * Get remote svn url
   * @param workspaceId
   * @param projectPath
   */
  getRemoteSvnUrl(workspaceId: string, projectPath: string): void {
    let svnInfo: {items?: any[]} = {};
    svnInfo.items = [{uRL: this.remoteSvnUrlsMap.get(workspaceId + projectPath)}];

    this.$httpBackend.when('POST', this.workspaceAgentMap.get(workspaceId) + '/svn/info?workspaceId=' + workspaceId).respond(svnInfo);
  }

  /**
   * Setup Backend for factories
   */
  factoriesBackendSetup(): void {
    this.setup();

    let allFactories = [];
    let pageFactories = [];

    let factoriesKeys = this.factoriesMap.keys();
    for (let key of factoriesKeys) {
      let factory = this.factoriesMap.get(key);
      this.$httpBackend.when('GET', '/api/factory/' + factory.id).respond(factory);
      this.$httpBackend.when('PUT', `/api/factory/${factory.id}`).respond({});
      this.$httpBackend.when('DELETE', '/api/factory/' + factory.id).respond(() => {
        return [200, {success: true, errors: []}];
      });
      if (this.defaultUser) {
        this.$httpBackend.when('GET', `/api/factory/find?creator.userId=${this.defaultUser.id}&name=${factory.name}`).respond([factory]);
      }
      allFactories.push(factory);
    }

    if (this.defaultUser) {
      this.$httpBackend.when('GET', '/api/user').respond(this.defaultUser);

      if (allFactories.length >  this.pageSkipCount) {
        if (allFactories.length > this.pageSkipCount + this.pageMaxItem) {
          pageFactories = allFactories.slice(this.pageSkipCount, this.pageSkipCount + this.pageMaxItem);
        } else {
          pageFactories = allFactories.slice(this.pageSkipCount);
        }
      }
      this.$httpBackend.when('GET', '/api/factory/find?creator.userId=' + this.defaultUser.id + '&maxItems=' + this.pageMaxItem + '&skipCount=' + this.pageSkipCount).respond(pageFactories);
    }
  }

  /**
   * Setup all users
   */
  usersBackendSetup(): void {
    this.$httpBackend.when('GET', '/api/user').respond(this.defaultUser);

    let userIdKeys = this.userIdMap.keys();
    for (let key of userIdKeys) {
      this.$httpBackend.when('GET', '/api/user/' + key).respond(this.userIdMap.get(key));
    }

    let userEmailKeys = this.userEmailMap.keys();
    for (let key of userEmailKeys) {
      this.$httpBackend.when('GET', '/api/user/find?email=' + key).respond(this.userEmailMap.get(key));
    }
  }

  /**
   * Add the given factory
   * @param factory
   */
  addUserFactory(factory: any): void {
    this.factoriesMap.set(factory.id, factory);
  }

  /**
   * Sets max objects on response
   * @param pageMaxItem
   */
  setPageMaxItem(pageMaxItem: number): void {
    this.pageMaxItem = pageMaxItem;
  }

  /**
   * Sets skip count of values
   * @param pageSkipCount
   */
  setPageSkipCount(pageSkipCount: number): void  {
    this.pageSkipCount = pageSkipCount;
  }

  /**
   * Add the given user
   * @param user
   */
  setDefaultUser(user: che.IUser): void {
    this.defaultUser = user;
  }

  /**
   * Add the given user to userIdMap
   * @param user
   */
  addUserById(user: che.IUser): void {
    this.userIdMap.set(user.id, user);
  }

  /**
   * Add the given user to userEmailMap
   * @param user
   */
  addUserEmail(user: che.IUser): void {
    this.userEmailMap.set(user.email, user);
  }

  /**
   * Setup Backend for teams
   */
  teamsBackendSetup() {
    let allTeams = [];

    let teamsKeys = this.teamsMap.keys();
    for (let key of teamsKeys) {
      let team = this.teamsMap.get(key);
      this.$httpBackend.when('GET', '/api/organization/' + team.id).respond(team);
      this.$httpBackend.when('DELETE', '/api/organization/' + team.id).respond(() => {
        return [200, {success: true, errors: []}];
      });
      allTeams.push(team);
    }

    this.$httpBackend.when('GET', /\/api\/organization(\?.*$)?/).respond(allTeams);
  }

  /**
   * Add the given team to teamsMap
   * @param {che.ITeam} team
   */
  addTeamById(team: che.ITeam) {
    this.teamsMap.set(team.id, team);
  }

  /**
   * Setup Backend for organizations
   */
  organizationsBackendSetup(): void {
    const allOrganizations = [];

    const organizationKeys = this.organizationsMap.keys();
    for (let key of organizationKeys) {
      const organization = this.organizationsMap.get(key);
      this.$httpBackend.when('GET', '/api/organization/' + organization.id).respond(organization);
      this.$httpBackend.when('GET', '/api/organization/find?name=' + encodeURIComponent(organization.qualifiedName)).respond(organization);
      this.$httpBackend.when('DELETE', '/api/organization/' + organization.id).respond(() => {
        return [200, {success: true, errors: []}];
      });
      allOrganizations.push(organization);
    }
    this.$httpBackend.when('GET', /^\/api\/organization\/find\?name=.*$/).respond(404, {}, {message: 'Organization is not found.'});
    this.$httpBackend.when('GET', /\/api\/organization(\?.*$)?/).respond(allOrganizations);
  }

  /**
   * Add the given organization to organizationsMap
   *
   * @param {che.IOrganization} organization the organization
   */
  addOrganizationById(organization: che.IOrganization): void {
    this.organizationsMap.set(organization.id, organization);
  }

  /**
   * Setup Backend for permissions.
   */
  permissionsBackendSetup(): void {
    const keys = this.permissionsMap.keys();
    for (let domainInstanceKey of keys) {
      const permissionsList = this.permissionsMap.get(domainInstanceKey);
      const {domainId, instanceId} = permissionsList[0];

      this.$httpBackend.when('GET', `/api/permissions/${domainId}/all?instance=${instanceId}`).respond(permissionsList);
    }
  }

  /**
   * Add permission to a permissions map
   *
   * @param {che.IPermissions} permissions
   */
  addPermissions(permissions: che.IPermissions): void {
    let domainInstanceKey = permissions.domainId + '|' + permissions.instanceId;

    if (this.permissionsMap.has(domainInstanceKey)) {
      this.permissionsMap.get(domainInstanceKey).push(permissions);
    } else {
      this.permissionsMap.set(domainInstanceKey, [permissions]);
    }
  }

  /**
   * Setup Backend for resources.
   */
  resourcesBackendSetup(): void {
    const keys = this.resourcesMap.keys();
    for (let organizationId of keys) {
      const organizationResourcesMap = this.resourcesMap.get(organizationId);

      // distributed
      if (organizationResourcesMap.has('distributed')) {
        const resources = organizationResourcesMap.get('distributed');
        this.$httpBackend.when('GET', `/api/organization/resource/${organizationId}/cap`).respond(resources);
      }

      // total
      if (organizationResourcesMap.has('total')) {
        const resources = organizationResourcesMap.get('total');
        this.$httpBackend.when('GET', `/api/resource/${organizationId}`).respond(resources);
      }
    }
  }

  /**
   * Add resource to a resources map
   *
   * @param {string} organizationId organization ID
   * @param {string} scope total, used or available
   * @param {any} resource
   */
  addResource(organizationId: string, scope: string, resource: any): void {
    if (!this.resourcesMap.has(organizationId)) {
      this.resourcesMap.set(organizationId, new Map());
    }

    const organizationResourcesMap = this.resourcesMap.get(organizationId);
    if (organizationResourcesMap.has(scope)) {
      organizationResourcesMap.get(scope).push(resource);
    } else {
      organizationResourcesMap.set(scope, [resource]);
    }
  }

  /**
   * Setup backend for installers.
   */
  installersBackendSetup(): void {
    for (const [installerId, installer] of this.installersMap) {
      this.$httpBackend.when('GET', `/api/installer/${installerId}`).respond(installer);
    }
    this.$httpBackend.when('GET', '/api/installer').respond(this.installersList);
  }

  /**
   * Add installers.
   * @param {che.IAgent} installer an installer to add
   */
  addInstaller(installer: che.IAgent): void {
    const latest = this.installersMap.get(installer.id);
    if (!latest || installer.version > latest.version) {
      this.installersMap.set(installer.id, installer);
    }
    this.installersList.push(installer);
  }

}
