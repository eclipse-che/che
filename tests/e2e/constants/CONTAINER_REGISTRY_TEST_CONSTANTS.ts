/** *******************************************************************
 * copyright (c) 2026 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

export const CONTAINER_REGISTRY_TEST_CONSTANTS: {
	TS_SELENIUM_CONTAINER_REGISTRY_IMAGE: string;
	TS_SELENIUM_CONTAINER_REGISTRY_PASSWORD: string;
	TS_SELENIUM_CONTAINER_REGISTRY_URL: string;
	TS_SELENIUM_CONTAINER_REGISTRY_USERNAME: string;
} = {
	/**
	 * registry host added in User Preferences.
	 */
	TS_SELENIUM_CONTAINER_REGISTRY_URL: process.env.TS_SELENIUM_CONTAINER_REGISTRY_URL || 'quay.io',

	/**
	 * username for the private registry. A Quay robot-account username is also supported.
	 */
	TS_SELENIUM_CONTAINER_REGISTRY_USERNAME: process.env.TS_SELENIUM_CONTAINER_REGISTRY_USERNAME || '',

	/**
	 * password or token for the private registry.
	 */
	TS_SELENIUM_CONTAINER_REGISTRY_PASSWORD: process.env.TS_SELENIUM_CONTAINER_REGISTRY_PASSWORD || '',

	/**
	 * full private image name, including a reusable test tag, for example
	 * quay.io/example/che-private-registry-test:latest.
	 */
	TS_SELENIUM_CONTAINER_REGISTRY_IMAGE: process.env.TS_SELENIUM_CONTAINER_REGISTRY_IMAGE || ''
};
