/** *******************************************************************
 * copyright (c) 2020-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
export const OAUTH_CONSTANTS: {
	TS_SELENIUM_K8S_PASSWORD: string;
	TS_SELENIUM_GIT_PROVIDER_PASSWORD: string;
	TS_OCP_LOGIN_PAGE_PROVIDER_TITLE: string;
	TS_SELENIUM_K8S_USERNAME: string;
	TS_SELENIUM_GIT_PROVIDER_OAUTH: boolean;
	TS_SELENIUM_OCP_USERNAME: string;
	TS_SELENIUM_OCP_PASSWORD: string;
	TS_SELENIUM_VALUE_OPENSHIFT_OAUTH: boolean;
	TS_SELENIUM_GIT_PROVIDER_USERNAME: string;
} = {
	/**
	 * value of OpenShift oAuth property determines how to login in installed application,
	 * if 'false' as an user of application, if 'true' as a regular user of OCP.
	 */
	TS_SELENIUM_VALUE_OPENSHIFT_OAUTH: process.env.TS_SELENIUM_VALUE_OPENSHIFT_OAUTH === 'true',

	/**
	 * log into OCP by using appropriate provider title.
	 */
	TS_OCP_LOGIN_PAGE_PROVIDER_TITLE: process.env.TS_OCP_LOGIN_PAGE_PROVIDER_TITLE || '',

	/**
	 * regular username used to login in OCP.
	 */
	TS_SELENIUM_OCP_USERNAME: process.env.TS_SELENIUM_OCP_USERNAME || '',

	/**
	 * password regular user used to login in OCP.
	 */
	TS_SELENIUM_OCP_PASSWORD: process.env.TS_SELENIUM_OCP_PASSWORD || '',

	/**
	 * regular username used to login in Kubernetes.
	 */
	TS_SELENIUM_K8S_USERNAME: process.env.TS_SELENIUM_K8S_USERNAME || '',

	/**
	 * password regular user used to login in Kubernetes.
	 */
	TS_SELENIUM_K8S_PASSWORD: process.env.TS_SELENIUM_K8S_PASSWORD || '',

	/**
	 * for login via github for example on https://che-dogfooding.apps.che-dev.x6e0.p1.openshiftapps.com
	 * For factory tests
	 */
	TS_SELENIUM_GIT_PROVIDER_OAUTH: process.env.TS_SELENIUM_GIT_PROVIDER_OAUTH === 'true',

	/**
	 * git repository username
	 */
	TS_SELENIUM_GIT_PROVIDER_USERNAME: process.env.TS_SELENIUM_GIT_PROVIDER_USERNAME || '',

	/**
	 * git repository password
	 */
	TS_SELENIUM_GIT_PROVIDER_PASSWORD: process.env.TS_SELENIUM_GIT_PROVIDER_PASSWORD || ''
};
