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
 * This class contains popular job titles.
 */
export class CheJobsConfig {

  constructor(register: che.IRegisterService) {
    // register this factory
    register.app.constant('jsonJobs', JSON.stringify([{
        name: 'Architect'
      }, {
        name: 'Team Lead'
      }, {
        name: 'DevOps'
      }, {
        name: 'Developer'
      }, {
        name: 'System Administrator'
      }, {
        name: 'Manager'
      }, {
        name: 'Director'
      }, {
        name: 'VP'
      }, {
        name: 'C-Level'
      }, {
        name: 'Freelance'
      }, {
        name: 'Educator'
      }, {
        name: 'Student'
      }, {
        name: 'Hobbyist'
      }]
    ));

  }
}
