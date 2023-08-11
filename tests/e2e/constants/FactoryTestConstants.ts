/*********************************************************************
 * Copyright (c) 2020-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { BaseTestConstants } from './BaseTestConstants';

export enum GitProviderType {
    GITHUB = 'github',
    GITLAB = 'gitlab',
    BITBUCKET = 'bitbucket',
    AZURE_DEVOPS = 'azure-devops'
}

export const FactoryTestConstants: any = {
    /**
     * Git provider to check in factory tests
     */
    TS_SELENIUM_FACTORY_GIT_PROVIDER: process.env.TS_SELENIUM_FACTORY_GIT_PROVIDER || GitProviderType.GITHUB,

    /**
     * URL to create factory
     */
    TS_SELENIUM_FACTORY_GIT_REPO_URL: process.env.TS_SELENIUM_FACTORY_GIT_REPO_URL || '',

    /**
     * Git repository name
     */
    TS_SELENIUM_PROJECT_NAME: process.env.TS_SELENIUM_PROJECT_NAME || '',

    /**
     * Is factory repository URL private or no
     */
    TS_SELENIUM_IS_PRIVATE_FACTORY_GIT_REPO: process.env.TS_SELENIUM_IS_PRIVATE_FACTORY_GIT_REPO === 'true',

    /**
     * Git repository main branch name (main or master)
     */
    TS_SELENIUM_FACTORY_GIT_REPO_BRANCH: process.env.TS_SELENIUM_FACTORY_GIT_REPO_BRANCH || 'master',

    /**
     * Full factory URL
     */
    TS_SELENIUM_FACTORY_URL(): string {
        return process.env.TS_SELENIUM_FACTORY_URL || BaseTestConstants.TS_SELENIUM_BASE_URL + '/dashboard/#/' + this.TS_SELENIUM_FACTORY_GIT_REPO_URL;
    },
};
