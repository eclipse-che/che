/** *******************************************************************
 * copyright (c) 2020-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
export const CHROME_DRIVER_CONSTANTS: {
	TS_SELENIUM_W3C_CHROME_OPTION: boolean;
	TS_SELENIUM_HEADLESS: boolean;
	TS_USE_WEB_DRIVER_FOR_TEST: boolean;
	TS_SELENIUM_LAUNCH_FULLSCREEN: boolean;
	TS_SELENIUM_REMOTE_DRIVER_URL: string;
} = {
	/**
	 * remote driver URL.
	 */
	TS_SELENIUM_REMOTE_DRIVER_URL: process.env.TS_SELENIUM_REMOTE_DRIVER_URL || '',

	/**
	 * run browser in "Headless" (hidden) mode, "false" by default.
	 */
	TS_SELENIUM_HEADLESS: process.env.TS_SELENIUM_HEADLESS === 'true',

	/**
	 * create instance of chromedriver, "true" by default. Should be "false" to run only API tests.
	 */
	TS_USE_WEB_DRIVER_FOR_TEST: process.env.TS_USE_WEB_DRIVER_FOR_TEST !== 'false',

	/**
	 * run browser in "Fullscreen" (kiosk) mode.
	 * Default to true if undefined
	 */
	TS_SELENIUM_LAUNCH_FULLSCREEN: process.env.TS_SELENIUM_LAUNCH_FULLSCREEN !== 'false',

	/**
	 * run browser with an enabled or disabled W3C protocol (on Chrome  76 and upper, it is enabled by default), "true" by default.
	 */
	TS_SELENIUM_W3C_CHROME_OPTION: process.env.TS_SELENIUM_W3C_CHROME_OPTION !== 'false'
};
