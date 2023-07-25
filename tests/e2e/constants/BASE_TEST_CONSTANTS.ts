/** *******************************************************************
 * copyright (c) 2020-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
export enum Platform {
	OPENSHIFT = 'openshift',
	KUBERNETES = 'kubernetes'
}

export const BASE_TEST_CONSTANTS: {
	TS_DEBUG_MODE: boolean;
	TS_PLATFORM: string;
	TS_SELENIUM_RESPONSE_INTERCEPTOR: boolean;
	TS_SELENIUM_BASE_URL: string;
	DELETE_WORKSPACE_ON_FAILED_TEST: boolean;
	TS_SELENIUM_EDITOR: string;
	TS_LOAD_TESTS: string;
	TS_SELENIUM_REQUEST_INTERCEPTOR: boolean;
	TS_SELENIUM_PROJECT_ROOT_FILE_NAME: string;
	TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME: string;
	IS_CLUSTER_DISCONNECTED: () => boolean;
} = {
	/**
	 * base URL of the application which should be checked
	 */
	TS_SELENIUM_BASE_URL: !process.env.TS_SELENIUM_BASE_URL ? 'http://sample-url' : process.env.TS_SELENIUM_BASE_URL.replace(/\/$/, ''),

	IS_CLUSTER_DISCONNECTED: (): boolean => BASE_TEST_CONSTANTS.TS_SELENIUM_BASE_URL.includes('airgap'),

	/**
	 * choose the platform where "che" application deployed, "openshift" by default.
	 */
	TS_PLATFORM: process.env.TS_PLATFORM || Platform.OPENSHIFT,

	/**
	 * editor the tests are running against, "code" by default.
	 * Possible values: "che-code"
	 */
	TS_SELENIUM_EDITOR: process.env.TS_SELENIUM_EDITOR || 'che-code',

	/**
	 * file name to check if project was imported
	 */
	TS_SELENIUM_PROJECT_ROOT_FILE_NAME: process.env.TS_SELENIUM_PROJECT_ROOT_FILE_NAME || 'devfile.yaml',

	/**
	 * name of workspace created for 'Happy Path' scenario validation.
	 */
	TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME: process.env.TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME || 'EmptyWorkspace',

	/**
	 * this variable specifies that run test is used for load testing and that all artifacts will be sent to ftp client.
	 */
	TS_LOAD_TESTS: process.env.TS_LOAD_TESTS || 'false',

	/**
	 * enable Axios request interceptor, false by default
	 */
	TS_SELENIUM_REQUEST_INTERCEPTOR: process.env.TS_SELENIUM_REQUEST_INTERCEPTOR === 'true',

	/**
	 * enable Axios response interceptor, false by default
	 */
	TS_SELENIUM_RESPONSE_INTERCEPTOR: process.env.TS_SELENIUM_RESPONSE_INTERCEPTOR === 'true',

	/**
	 * stop and remove workspace if a test fails.
	 */
	DELETE_WORKSPACE_ON_FAILED_TEST: process.env.DELETE_WORKSPACE_ON_FAILED_TEST === 'true',

	/**
	 * constant, which prolong timeout constants for local debug.
	 */
	TS_DEBUG_MODE: process.env.TS_DEBUG_MODE === 'true'
};
