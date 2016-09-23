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
 * This class is providing a builder for Profile
 * @author Florent Benoit
 */
export class CheProfileBuilder {

  /**
   * Default constructor.
   */
  constructor() {
    this.profile = {};
    this.profile.attributes = {};
  }


  /**
   * Sets the email of the user
   * @param email the email to use
   * @returns {CheProfileBuilder}
   */
  withEmail(email) {
    this.profile.email = email;
    return this;
  }

  /**
   * Sets the firstName of the user
   * @param firstName the firstName to use
   * @returns {CheProfileBuilder}
   */
  withFirstName(firstName) {
    return this.withAttribute('firstName', firstName);
  }

  /**
   * Sets the lastName of the user
   * @param lastName the lastName to use
   * @returns {CheProfileBuilder}
   */
  withLastName(lastName) {
    return this.withAttribute('lastName', lastName);
  }

  /**
   * Sets the id of the profile
   * @param id the id to use
   * @returns {CheProfileBuilder}
   */
  withId(id) {
    this.profile.id = id;
    this.profile.userId = id;
    return this;
  }

  /**
   * Sets an attribute on the profile
   * @param name the attribute name
   * @param name the attribute value
   * @returns {CheProfileBuilder}
   */
  withAttribute(name, value) {
    this.profile.attributes[name] = value;
    return this;
  }


  /**
   * Build the user
   * @returns {CheProfileBuilder.profile|*}
   */
  build() {
    return this.profile;
  }


}
