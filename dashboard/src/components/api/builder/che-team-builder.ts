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
 * This class is providing a builder for Team
 * @author Oleksii Kurinnyi
 */
export class CheTeamBuilder {
  private team: che.ITeam;

  /**
   * Default constructor.
   */
  constructor() {
    this.team = <che.ITeam>{};
  }


  /**
   * Sets the name of the team
   * @param name the name to use
   * @returns {CheTeamBuilder}
   */
  withName(name: string) {
    this.team.name = name;
    return this;
  }

  /**
   * Sets the id of the team
   * @param id the id to use
   * @returns {CheTeamBuilder}
   */
  withId(id: string) {
    this.team.id = id;
    return this;
  }

  /**
   * Build the team
   * @return {CheTeamBuilder}
   */
  build() {
    return this.team;
  }

}
