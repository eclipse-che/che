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

import {CheWorkspaceBuilder} from './che-workspace-builder';
import {CheProjectReferenceBuilder} from './che-projectreference-builder';
import {CheProjectDetailsBuilder} from './che-projectdetails-builder';
import {CheProjectTypeBuilder} from './che-projecttype-builder';
import {CheProjectTemplateBuilder} from './che-projecttemplate-builder';
import {CheProjectTypeAttributeDescriptorBuilder} from './che-projecttype-attribute-descriptor-builder';
import {CheProfileBuilder} from './che-profile-builder';
import {CheStackBuilder} from './che-stack-builder';

/**
 * This class is providing the entry point for accessing the builders
 * @author Florent Benoit
 */
export class CheAPIBuilder {

  /**
   * Default constructor
   * @ngInject for Dependency injection
   */
  constructor () {
  }

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
  getProfileBuilder() {
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
}
