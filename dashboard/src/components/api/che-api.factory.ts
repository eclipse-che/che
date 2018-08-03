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
import {CheSsh} from './che-ssh.factory';
import {CheWorkspace} from './workspace/che-workspace.factory';
import {CheProfile} from './che-profile.factory';
import {CheFactory} from './che-factory.factory';
import {CheFactoryTemplate} from './che-factory-template.factory';
import {ChePreferences} from './che-preferences.factory';
import {CheProjectTemplate} from './che-project-template.factory';
import {CheService} from './che-service.factory';
import {CheStack} from './che-stack.factory';
import {CheOAuthProvider} from './che-o-auth-provider.factory';
import {CheAgent} from './che-agent.factory';
import {CheUser} from './che-user.factory';


/**
 * This class is providing the entry point for accessing to Che API
 * It handles workspaces, projects, etc.
 * @author Florent Benoit
 */
export class CheAPI {

  static $inject = ['cheWorkspace', 'cheFactory', 'cheFactoryTemplate',
               'cheProfile', 'chePreferences', 'cheProjectTemplate',
              'cheService', 'cheStack', 'cheOAuthProvider', 'cheAgent',
            'cheSsh', 'cheUser', 'chePermissions', 'cheOrganization'];

  private cheWorkspace: CheWorkspace;
  private cheProfile: CheProfile;
  private chePreferences: ChePreferences;
  private cheProjectTemplate: CheProjectTemplate;
  private cheFactory: CheFactory;
  private cheFactoryTemplate: CheFactoryTemplate;
  private cheService: CheService;
  private cheStack: CheStack;
  private cheOAuthProvider: CheOAuthProvider;
  private cheAgent: CheAgent;
  private cheSsh: CheSsh;
  private cheUser: CheUser;
  private chePermissions: che.api.IChePermissions;
  private cheOrganization: che.api.ICheOrganization;

  /**
   * Default constructor that is using resource
   */
  constructor(cheWorkspace: CheWorkspace, cheFactory: CheFactory, cheFactoryTemplate: CheFactoryTemplate,
              cheProfile: CheProfile, chePreferences: ChePreferences, cheProjectTemplate: CheProjectTemplate,
              cheService: CheService, cheStack: CheStack, cheOAuthProvider: CheOAuthProvider, cheAgent: CheAgent,
              cheSsh: CheSsh, cheUser: CheUser, chePermissions: che.api.IChePermissions, cheOrganization: che.api.ICheOrganization) {
    this.cheWorkspace = cheWorkspace;
    this.cheProfile = cheProfile;
    this.cheFactory = cheFactory;
    this.cheFactoryTemplate = cheFactoryTemplate;
    this.chePreferences = chePreferences;
    this.cheProjectTemplate = cheProjectTemplate;
    this.cheService = cheService;
    this.cheStack = cheStack;
    this.cheOAuthProvider = cheOAuthProvider;
    this.cheAgent = cheAgent;
    this.cheSsh = cheSsh;
    this.cheUser = cheUser;
    this.chePermissions = chePermissions;
    this.cheOrganization = cheOrganization;
  }

  /**
   * The Che Workspace API
   * @returns {CheAPI.cheWorkspace}
   */
  getWorkspace(): CheWorkspace {
    return this.cheWorkspace;
  }

  /**
   * The Che oAuth Provider API
   * @returns {CheOAuthProvider}
   */
  getOAuthProvider(): CheOAuthProvider {
    return this.cheOAuthProvider;
  }

  /**
   * The Che Profile API
   * @returns {CheProfile}
   */
  getProfile(): CheProfile {
    return this.cheProfile;
  }

  /**
   * The Che Preferences API
   * @returns {ChePreferences}
   */
  getPreferences(): ChePreferences {
    return this.chePreferences;
  }

  /**
   * The Che Project Template API
   * @returns {CheProjectTemplate}
   */
  getProjectTemplate(): CheProjectTemplate {
    return this.cheProjectTemplate;
  }

  /**
   * The Che Services API
   * @returns {CheService}
   */
  getService(): CheService {
    return this.cheService;
  }

  /**
   * The Che Stack API
   * @returns {CheStack}
   */
  getStack(): CheStack {
    return this.cheStack;
  }

  /**
   * The Che Agent API
   * @returns {CheAgent}
   */
  getAgent(): CheAgent {
    return this.cheAgent;
  }

  /**
   * Gets Che ssh API
   * @returns {CheSsh}
   */
  getSsh(): CheSsh {
    return this.cheSsh;
  }

  /**
   * The Che Factory API
   * @returns {CheFactory|*}
   */
  getFactory(): CheFactory {
    return this.cheFactory;
  }

  /**
   * The Che Factory Template API
   * @returns {CheFactoryTemplate|*}
   */
  getFactoryTemplate(): CheFactoryTemplate {
    return this.cheFactoryTemplate;
  }

  /**
   * The Che use API.
   *
   * @returns {CheUser}
   */
  getUser(): CheUser {
    return this.cheUser;
  }

  /**
   * The Che Permissions API
   * @returns {che.api.IChePermissions|*}
   */
  getPermissions(): che.api.IChePermissions {
    return this.chePermissions;
  }

  /**
   * The Che Organization API
   * @return {che.api.ICheOrganization}
   */
  getOrganization(): che.api.ICheOrganization {
    return this.cheOrganization;
  }
}
