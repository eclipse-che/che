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

/**
 * This class is providing a builder for Project Reference
 * @author Florent Benoit
 */
export class CheProjectReferenceBuilder {
  private projectReference: any;

  /**
   * Default constructor.
   */
  constructor() {
    this.projectReference = {};
  }

  /**
   * Sets the name of the project reference
   * @param {string} name the name to use
   * @returns {CheProjectReferenceBuilder}
   */
  withName(name: string): CheProjectReferenceBuilder {
    this.projectReference.name = name;
    return this;
  }

  /**
   * Build the project reference
   * @returns {CheProjectReferenceBuilder.projectReference|*}
   */
  build(): CheProjectReferenceBuilder {
    return this.projectReference;
  }

}

