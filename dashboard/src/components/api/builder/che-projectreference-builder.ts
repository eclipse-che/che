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
 * This class is providing a builder for Project Reference
 * @author Florent Benoit
 */
export class CheProjectReferenceBuilder {

  /**
   * Default constructor.
   */
  constructor() {
    this.projectReference = {};
  }


  /**
   * Sets the name of the project reference
   * @param name the name to use
   * @returns {CheProjectReferenceBuilder}
   */
  withName(name) {
    this.projectReference.name = name;
    return this;
  }

  /**
   * Build the project reference
   * @returns {CheProjectReferenceBuilder.projectReference|*}
   */
  build() {
    return this.projectReference;
  }


}

