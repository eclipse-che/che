/** *******************************************************************
 * copyright (c) 2020-2025 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { BASE_TEST_CONSTANTS } from './BASE_TEST_CONSTANTS';

export enum GitProviderType {
	GITHUB = 'github',
	GITLAB = 'gitlab',
	BITBUCKET_SERVER_OAUTH1 = 'bitbucket-server-oauth1',
	BITBUCKET_SERVER_OAUTH2 = 'bitbucket-server-oauth2',
	BITBUCKET_CLOUD_OAUTH2 = 'bitbucket-org',
	AZURE_DEVOPS = 'azure-devops'
}

export const FACTORY_TEST_CONSTANTS: {
	TS_SELENIUM_FACTORY_GIT_REPO_URL: string;
	TS_SELENIUM_AIRGAP_FACTORY_GIT_REPO_URL: string;
	TS_SELENIUM_PROJECT_NAME: string;
	TS_SELENIUM_IS_PRIVATE_FACTORY_GIT_REPO: boolean;
	TS_SELENIUM_FACTORY_GIT_PROVIDER: string;
	TS_SELENIUM_FACTORY_GIT_REPO_BRANCH: string;
	TS_SELENIUM_SSH_PRIVATE_KEY_PATH: string;
	TS_SELENIUM_SSH_PUBLIC_KEY_PATH: string;
	TS_GIT_COMMIT_AUTHOR_NAME: string;
	TS_GIT_COMMIT_AUTHOR_EMAIL: string;
	TS_GIT_PERSONAL_ACCESS_TOKEN: string;
	TS_SELENIUM_SSH_PRIVATE_KEY: string;
	TS_SELENIUM_SSH_PUBLIC_KEY: string;
	TS_SELENIUM_SSH_KEY_PASSPHRASE: string;
	TS_SELENIUM_FACTORY_URL(): string;
} = {
	/**
	 * git provider to check in factory tests
	 */
	TS_SELENIUM_FACTORY_GIT_PROVIDER: process.env.TS_SELENIUM_FACTORY_GIT_PROVIDER || GitProviderType.GITHUB,

	/**
	 * url to create factory
	 */
	TS_SELENIUM_FACTORY_GIT_REPO_URL: process.env.TS_SELENIUM_FACTORY_GIT_REPO_URL || '',

	/**
	 * url to create factory for airgap/disconnected environments
	 */
	TS_SELENIUM_AIRGAP_FACTORY_GIT_REPO_URL: process.env.TS_SELENIUM_AIRGAP_FACTORY_GIT_REPO_URL || '',

	/**
	 * git repository name
	 */
	TS_SELENIUM_PROJECT_NAME: process.env.TS_SELENIUM_PROJECT_NAME || '',

	/**
	 * is factory repository URL private or no
	 */
	TS_SELENIUM_IS_PRIVATE_FACTORY_GIT_REPO: process.env.TS_SELENIUM_IS_PRIVATE_FACTORY_GIT_REPO === 'true',

	/**
	 * git repository main branch name (main or master)
	 */
	TS_SELENIUM_FACTORY_GIT_REPO_BRANCH: process.env.TS_SELENIUM_FACTORY_GIT_REPO_BRANCH || 'master',

	/**
	 * path to SSH private key file
	 */
	TS_SELENIUM_SSH_PRIVATE_KEY_PATH: process.env.TS_SELENIUM_SSH_PRIVATE_KEY_PATH || 'resources/factory/pr-k.txt',

	/**
	 * path to SSH public key file
	 */
	TS_SELENIUM_SSH_PUBLIC_KEY_PATH: process.env.TS_SELENIUM_SSH_PUBLIC_KEY_PATH || 'resources/factory/pub-k.txt',

	/**
	 * git commit author name
	 */
	TS_GIT_COMMIT_AUTHOR_NAME: process.env.TS_GIT_CONFIG_USER_NAME || 'user',

	/**
	 * git commit author email
	 */
	TS_GIT_COMMIT_AUTHOR_EMAIL: process.env.TS_GIT_CONFIG_USER_EMAIL || 'user@user.com',

	/**
	 * personal access token of git provider (or api token if Bitbucket.org)
	 */
	TS_GIT_PERSONAL_ACCESS_TOKEN: process.env.TS_GIT_PERSONAL_ACCESS_TOKEN || '',

	/**
	 * ssh private key as string (from environment variable)
	 */
	TS_SELENIUM_SSH_PRIVATE_KEY: process.env.TS_SELENIUM_SSH_PRIVATE_KEY || '',

	/**
	 * ssh public key as string (from environment variable)
	 */
	TS_SELENIUM_SSH_PUBLIC_KEY: process.env.TS_SELENIUM_SSH_PUBLIC_KEY || '',

	/**
	 * ssh passphrase as string (from environment variable)
	 */
	TS_SELENIUM_SSH_KEY_PASSPHRASE: process.env.TS_SELENIUM_SSH_KEY_PASSPHRASE || '',

	/**
	 * full factory URL
	 */
	TS_SELENIUM_FACTORY_URL(): string {
		return (
			process.env.TS_SELENIUM_FACTORY_URL || BASE_TEST_CONSTANTS.TS_SELENIUM_BASE_URL + '/#/' + this.TS_SELENIUM_FACTORY_GIT_REPO_URL
		);
	}
};
