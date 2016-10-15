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
 * This class contains popular job titles.
 */
export class CheJobsConfig {

    constructor(register) {
        // Register this factory
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
