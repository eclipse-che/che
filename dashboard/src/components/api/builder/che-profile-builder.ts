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
 * This class is providing a builder for Profile
 * @author Florent Benoit
 */
export class CheProfileBuilder {

  private profile: che.IProfile;

  /**
   * Default constructor.
   */
  constructor() {
    this.profile = {} as che.IProfile;
    this.profile.attributes = {};
  }


  /**
   * Sets the email of the user
   * @param {string} email the email to use
   * @returns {CheProfileBuilder}
   */
  withEmail(email: string): CheProfileBuilder {
    this.profile.email = email;
    return this;
  }

  /**
   * Sets the firstName of the user
   * @param {string} firstName the firstName to use
   * @returns {CheProfileBuilder}
   */
  withFirstName(firstName: string): CheProfileBuilder {
    return this.withAttribute('firstName', firstName);
  }

  /**
   * Sets the lastName of the user
   * @param {string} lastName the lastName to use
   * @returns {CheProfileBuilder}
   */
  withLastName(lastName: string): CheProfileBuilder {
    return this.withAttribute('lastName', lastName);
  }

  /**
   * Sets the id of the profile
   * @param {string} id the id to use
   * @returns {CheProfileBuilder}
   */
  withId(id: string): CheProfileBuilder {
    this.profile.userId = id;
    return this;
  }

  /**
   * Sets an attribute on the profile
   * @param {string} name the attribute name
   * @param {string} value the attribute value
   * @returns {CheProfileBuilder}
   */
  withAttribute(name: string, value: string): CheProfileBuilder {
    this.profile.attributes[name] = value;
    return this;
  }


  /**
   * Build the user
   * @returns {CheProfileBuilder.profile|*}
   */
  build(): che.IProfile {
    return this.profile;
  }

}
