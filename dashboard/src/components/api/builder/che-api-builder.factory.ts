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

import {CheWorkspaceBuilder} from './che-workspace-builder';
import {CheProjectReferenceBuilder} from './che-projectreference-builder';
import {CheFactoryBuilder} from './che-factory-builder';
import {CheProjectDetailsBuilder} from './che-projectdetails-builder';
import {CheProjectTypeBuilder} from './che-projecttype-builder';
import {CheProjectTemplateBuilder} from './che-projecttemplate-builder';
import {CheProjectTypeAttributeDescriptorBuilder} from './che-projecttype-attribute-descriptor-builder';
import {CheProfileBuilder} from './che-profile-builder';
import {CheStackBuilder} from './che-stack-builder';
import {CheUserBuilder} from './che-user-builder';
import {ChePermissionsBuilder} from './che-permissions-builder';
import {CheOrganizationsBuilder} from './che-organizations-builder';
import {CheResourceBuilder} from './che-resource-builder';
import {CheTeamBuilder} from './che-team-builder';

/**
 * This class is providing the entry point for accessing the builders
 * @author Florent Benoit
 */
export class CheAPIBuilder {

  /**
   * The Che Workspace builder
   * @returns {CheWorkspaceBuilder}
   */
  getWorkspaceBuilder() {
    return new CheWorkspaceBuilder();
  }

  /***
   * The Che Profile builder
   * @returns {CheProfileBuilder}
   */
  getProfileBuilder(): CheProfileBuilder {
    return new CheProfileBuilder();
  }

  /***
   * The Che Project Reference builder
   * @returns {CheProjectReferenceBuilder}
   */
  getProjectReferenceBuilder() {
    return new CheProjectReferenceBuilder();
  }


  /***
   * The Che Project Details builder
   * @returns {CheProjectDetailsBuilder}
   */
  getProjectDetailsBuilder() {
    return new CheProjectDetailsBuilder();
  }

  /***
   * The Che Project Template builder
   * @returns {CheProjectTemplateBuilder}
   */
  getProjectTemplateBuilder() {
    return new CheProjectTemplateBuilder();
  }

  /***
   * The Che Project Type builder
   * @returns {CheProjectTypeBuilder}
   */
  getProjectTypeBuilder() {
    return new CheProjectTypeBuilder();
  }

  /**
   * Attribute descriptor builder
   * @returns {CheProjectTypeAttributeDescriptorBuilder}
   */
  getProjectTypeAttributeDescriptorBuilder() {
    return new CheProjectTypeAttributeDescriptorBuilder();
  }

  /***
   * The Che Stack builder
   * @returns {CheStackBuilder}
   */
  getStackBuilder() {
    return new CheStackBuilder();
  }

  /***
   * The Che Factory builder
   * @returns {CheFactoryBuilder}
   */
  getFactoryBuilder() {
    return new CheFactoryBuilder();
  }

  /***
   * The Che User builder
   * @returns {CheUserBuilder}
   */
  getUserBuilder() {
    return new CheUserBuilder();
  }

  /**
   * The Che Permissions builder
   * @return {ChePermissionsBuilder}
   */
  getPermissionsBuilder() {
    return new ChePermissionsBuilder();
  }

  /***
   * The Che Team builder
   * @returns {CheTeamBuilder}
   */
  getTeamBuilder() {
    return new CheTeamBuilder();
  }

  /**
   * The Che Organizations builder
   * @return {CheOrganizationsBuilder}
   */
  getOrganizationsBuilder() {
    return new CheOrganizationsBuilder();
  }

  /**
   * The Che Resources builder
   *
   * @return {CheResourceBuilder}
   */
  getResourceBuilder() {
    return new CheResourceBuilder();
  }
}
