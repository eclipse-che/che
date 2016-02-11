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
 * This class is providing a builder for User
 * @author Florent Benoit
 */
export class CheUserBuilder {

  /**
   * Default constructor.
   */
  constructor() {
    this.user = {};
  }


  /**
   * Sets the email of the user
   * @param email the email to use
   * @returns {CheUserBuilder}
   */
  withEmail(email) {
    this.user.email = email;
    return this;
  }



  /**
   * Sets the id of the user
   * @param id the id to use
   * @returns {CheUserBuilder}
   */
  withId(id) {
    this.user.id = id;
    return this;
  }

  /**
   * Sets the aliases of the user
   * @param aliases the aliases to use
   * @returns {CheUserBuilder}
   */
  withAliases(aliases) {
    this.user.aliases = aliases;
    return this;
  }


  /**
   * Build the user
   * @returns {CheUserBuilder.user|*}
   */
  build() {
    return this.user;
  }


}
