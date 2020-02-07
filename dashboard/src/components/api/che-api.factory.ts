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
import {CheService} from './che-service.factory';
import {CheOAuthProvider} from './che-o-auth-provider.factory';
import {CheUser} from './che-user.factory';
import {CheDevfile} from './che-devfile.factory';
import {CheKubernetesNamespace} from './che-kubernetes-namespace.factory';

/**
 * This class is providing the entry point for accessing to Che API
 * It handles workspaces, projects, etc.
 * @author Florent Benoit
 */
export class CheAPI {

  static $inject = [
    'cheDevfile',
    'cheFactory',
    'cheFactoryTemplate',
    'cheKubernetesNamespace',
    'cheOAuthProvider',
    'cheOrganization',
    'chePermissions',
    'chePreferences',
    'cheProfile',
    'cheService',
    'cheSsh',
    'cheUser',
    'cheWorkspace',
  ];

  private cheDevfile: CheDevfile;
  private cheFactory: CheFactory;
  private cheFactoryTemplate: CheFactoryTemplate;
  private cheKubernetesNamespace: che.api.ICheKubernetesNamespace;
  private cheOAuthProvider: CheOAuthProvider;
  private cheOrganization: che.api.ICheOrganization;
  private chePermissions: che.api.IChePermissions;
  private chePreferences: ChePreferences;
  private cheProfile: CheProfile;
  private cheService: CheService;
  private cheSsh: CheSsh;
  private cheUser: CheUser;
  private cheWorkspace: CheWorkspace;

  /**
   * Default constructor that is using resource
   */
  constructor(
    cheDevfile: CheDevfile,
    cheFactory: CheFactory,
    cheFactoryTemplate: CheFactoryTemplate,
    cheKubernetesNamespace: CheKubernetesNamespace,
    cheOAuthProvider: CheOAuthProvider,
    cheOrganization: che.api.ICheOrganization,
    chePermissions: che.api.IChePermissions,
    chePreferences: ChePreferences,
    cheProfile: CheProfile,
    cheService: CheService,
    cheSsh: CheSsh,
    cheUser: CheUser,
    cheWorkspace: CheWorkspace,
  ) {
    this.cheDevfile = cheDevfile;
    this.cheFactory = cheFactory;
    this.cheFactoryTemplate = cheFactoryTemplate;
    this.cheKubernetesNamespace = cheKubernetesNamespace;
    this.cheOAuthProvider = cheOAuthProvider;
    this.cheOrganization = cheOrganization;
    this.chePermissions = chePermissions;
    this.chePreferences = chePreferences;
    this.cheProfile = cheProfile;
    this.cheService = cheService;
    this.cheSsh = cheSsh;
    this.cheUser = cheUser;
    this.cheWorkspace = cheWorkspace;
  }

  /**
   * The Che Workspace API
   * @returns {CheAPI.cheWorkspace}
   */
  getWorkspace(): CheWorkspace {
    return this.cheWorkspace;
  }

  /**
   * The Che OAuth Provider API
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
   * The Che Services API
   * @returns {CheService}
   */
  getService(): CheService {
    return this.cheService;
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

  /**
   * The Che Devfile API
   * @returns {che.api.ICheDevfile}
   */
  getDevfile(): che.api.ICheDevfile {
    return this.cheDevfile;
  }

  /**
   * The Che Kubernetes Namespace API
   */
  getKubernetesNamespace(): che.api.ICheKubernetesNamespace {
    return this.cheKubernetesNamespace;
  }
}
