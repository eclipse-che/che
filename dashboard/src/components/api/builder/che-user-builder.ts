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
 * This class is providing a builder for User
 * @author Florent Benoit
 * @author Oleksii Orel
 */
export class CheUserBuilder {
  private user: che.IUser;

  /**
   * Default constructor.
   */
  constructor() {
    this.user = <che.IUser>{
      email: '',
      name: '',
      aliases: [],
      id: ''
    };
  }

  /**
   * Sets the email of the user
   * @param {string} email the email to use
   * @returns {CheUserBuilder}
   */
  withEmail(email: string): CheUserBuilder {
    this.user.email = email;
    return this;
  }

  /**
   * Sets the id of the user
   * @param {string} id the id to use
   * @returns {CheUserBuilder}
   */
  withId(id: string): CheUserBuilder {
    this.user.id = id;
    return this;
  }

  /**
   * Sets the aliases of the user
   * @param {any[]} aliases the aliases to use
   * @returns {CheUserBuilder}
   */
  withAliases(aliases: any[]): CheUserBuilder {
    this.user.aliases = aliases;
    return this;
  }

  /**
   * Build the user
   * @returns {che.IUser}
   */
  build(): che.IUser {
    return this.user;
  }
}
