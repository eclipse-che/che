/** *******************************************************************
 * copyright (c) 2020-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
export const TIMEOUT_CONSTANTS: {
	TS_FIND_EXTENSION_TEST_TIMEOUT: number;
	TS_SELENIUM_WORKSPACE_STATUS_POLLING: number;
	TS_COMMON_DASHBOARD_WAIT_TIMEOUT: number;
	TS_EXPAND_PROJECT_TREE_ITEM_TIMEOUT: number;
	TS_DIALOG_WINDOW_DEFAULT_TIMEOUT: number;
	TS_DASHBOARD_WORKSPACE_STOP_TIMEOUT: number;
	TS_SELENIUM_CLICK_ON_VISIBLE_ITEM: number;
	TS_SELENIUM_DEFAULT_ATTEMPTS: number;
	TS_SELENIUM_WORKSPACE_STATUS_ATTEMPTS: number;
	TS_SELENIUM_WAIT_FOR_URL: number;
	TS_SELENIUM_DEFAULT_POLLING: number;
	TS_IDE_LOAD_TIMEOUT: number;
	TS_WAIT_LOADER_ABSENCE_TIMEOUT: number;
	TS_WAIT_LOADER_PRESENCE_TIMEOUT: number;
	TS_SELENIUM_START_WORKSPACE_TIMEOUT: number;
	TS_SELENIUM_LOAD_PAGE_TIMEOUT: number;
	TS_CLICK_DASHBOARD_ITEM_TIMEOUT: number;
	TS_COMMON_PLUGIN_TEST_TIMEOUT: number;
	TS_EDITOR_TAB_INTERACTION_TIMEOUT: number;
} = {
	/**
	 * default amount of tries, "5" by default.
	 */
	TS_SELENIUM_DEFAULT_ATTEMPTS: Number(process.env.TS_SELENIUM_DEFAULT_ATTEMPTS) || 5,

	/**
	 * default delay in milliseconds between tries, "1000" by default.
	 */
	TS_SELENIUM_DEFAULT_POLLING: Number(process.env.TS_SELENIUM_DEFAULT_POLLING) || 1000,

	// -------------------------------------------- INSTALLING AND STARTUP --------------------------------------------

	/**
	 * timeout waiting for url, "10 000" by default
	 */
	TS_SELENIUM_WAIT_FOR_URL: Number(process.env.TS_SELENIUM_WAIT_FOR_URL) || 10_000,

	/**
	 * amount of tries for checking workspace status.
	 */
	TS_SELENIUM_WORKSPACE_STATUS_ATTEMPTS: Number(process.env.TS_SELENIUM_WORKSPACE_STATUS_ATTEMPTS) || 90,

	/**
	 * delay in milliseconds between checking workspace status tries.
	 */
	TS_SELENIUM_WORKSPACE_STATUS_POLLING: Number(process.env.TS_SELENIUM_WORKSPACE_STATUS_POLLING) || 10000,

	/**
	 * wait between workspace started and IDE ready to be used, "20 000" by default.
	 */
	TS_IDE_LOAD_TIMEOUT: Number(process.env.TS_IDE_LOAD_TIMEOUT) || 20_000,

	/**
	 * timeout in milliseconds waiting for workspace start, "360 000" by default.
	 */
	TS_SELENIUM_START_WORKSPACE_TIMEOUT: Number(process.env.TS_SELENIUM_START_WORKSPACE_TIMEOUT) || 360_000,

	/**
	 * timeout in milliseconds waiting for page load, "20 000" by default.
	 */
	TS_SELENIUM_LOAD_PAGE_TIMEOUT: Number(process.env.TS_SELENIUM_LOAD_PAGE_TIMEOUT) || 20_000,

	/**
	 * wait for loader absence, "60 000" by default.
	 */
	TS_WAIT_LOADER_ABSENCE_TIMEOUT: Number(process.env.TS_WAIT_LOADER_ABSENCE_TIMEOUT) || 60_000,

	/**
	 * wait for loader absence, "60 000" by default.
	 */
	TS_WAIT_LOADER_PRESENCE_TIMEOUT: Number(process.env.TS_WAIT_LOADER_PRESENCE_TIMEOUT) || 60_000,

	// -------------------------------------------- DASHBOARD --------------------------------------------

	/**
	 * common timeout for dashboard items, "5 000" by default
	 */
	TS_COMMON_DASHBOARD_WAIT_TIMEOUT: Number(process.env.TS_COMMON_DASHBOARD_WAIT_TIMEOUT) || 5_000,

	/**
	 * timeout for clicking on dashboard menu items, "2 000" by default
	 */
	TS_CLICK_DASHBOARD_ITEM_TIMEOUT: Number(process.env.TS_CLICK_DASHBOARD_ITEM_TIMEOUT) || 2_000,

	/**
	 * timeout for workspace stopped status, "30 000" by default
	 */
	TS_DASHBOARD_WORKSPACE_STOP_TIMEOUT: Number(process.env.TS_DASHBOARD_WORKSPACE_STOP_TIMEOUT) || 60_000,

	// -------------------------------------------- PROJECT TREE --------------------------------------------

	/**
	 * expand item in project tree, "8 000" by default.
	 */
	TS_EXPAND_PROJECT_TREE_ITEM_TIMEOUT: Number(process.env.TS_EXPAND_PROJECT_TREE_ITEM_TIMEOUT) || 8_000,

	// -------------------------------------------- EDITOR --------------------------------------------

	/**
	 * timeout for interactions with editor tab - wait, click, select, "8 000" by default.
	 */
	TS_EDITOR_TAB_INTERACTION_TIMEOUT: Number(process.env.TS_OPEN_PROJECT_TREE_TIMEOUT) || 8_000,

	// -------------------------------------------- IDE --------------------------------------------

	/**
	 * timeout for context menu manipulation, "10 000" by default
	 */
	TS_DIALOG_WINDOW_DEFAULT_TIMEOUT: Number(process.env.TS_DIALOG_WINDOW_DEFAULT_TIMEOUT) || 10_000,

	/**
	 * timeout for clicking on visible item, "5 000" by default
	 */
	TS_SELENIUM_CLICK_ON_VISIBLE_ITEM: Number(process.env.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM) || 5_000,

	// ----------------------------------------- PLUGINS -----------------------------------------

	/**
	 * common timeout for plugins.
	 */
	TS_COMMON_PLUGIN_TEST_TIMEOUT: Number(process.env.TS_COMMON_PLUGIN_TEST_TIMEOUT) || 30_000,

	/**
	 * timeout for searching extension in marketplace.
	 */
	TS_FIND_EXTENSION_TEST_TIMEOUT: Number(process.env.TS_FIND_EXTENSION_TEST_TIMEOUT) || 15_000
};
