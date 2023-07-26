/*********************************************************************
 * Copyright (c) 2020-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

export const TimeoutConstants: any = {
  /**
   * Default amount of tries, "5" by default.
   */
  TS_SELENIUM_DEFAULT_ATTEMPTS: Number(process.env.TS_SELENIUM_DEFAULT_ATTEMPTS) || 5,

  /**
   * Default delay in milliseconds between tries, "1000" by default.
   */
  TS_SELENIUM_DEFAULT_POLLING: Number(process.env.TS_SELENIUM_DEFAULT_POLLING) || 1000,

  // -------------------------------------------- INSTALLING AND STARTUP --------------------------------------------

  /**
   * Timeout waiting for url, "10 000" by default
   */
  TS_SELENIUM_WAIT_FOR_URL: Number(process.env.TS_SELENIUM_WAIT_FOR_URL) || 10_000,

  /**
   * Amount of tries for checking workspace status.
   */
  TS_SELENIUM_WORKSPACE_STATUS_ATTEMPTS: Number(process.env.TS_SELENIUM_WORKSPACE_STATUS_ATTEMPTS) || 90,

  /**
   * Delay in milliseconds between checking workspace status tries.
   */
  TS_SELENIUM_WORKSPACE_STATUS_POLLING: Number(process.env.TS_SELENIUM_WORKSPACE_STATUS_POLLING) || 10000,

  /**
   * Wait between workspace started and IDE ready to be used, "20 000" by default.
   */
  TS_IDE_LOAD_TIMEOUT: Number(process.env.TS_IDE_LOAD_TIMEOUT) || 20_000,

  /**
   * Timeout in milliseconds waiting for workspace start, "360 000" by default.
   */
  TS_SELENIUM_START_WORKSPACE_TIMEOUT: Number(process.env.TS_SELENIUM_START_WORKSPACE_TIMEOUT) || 360_000,

  /**
   * Timeout in milliseconds waiting for page load, "20 000" by default.
   */
  TS_SELENIUM_LOAD_PAGE_TIMEOUT: Number(process.env.TS_SELENIUM_LOAD_PAGE_TIMEOUT) || 20_000,

  /**
   * Wait for loader absence, "60 000" by default.
   */
  TS_WAIT_LOADER_ABSENCE_TIMEOUT: Number(process.env.TS_WAIT_LOADER_ABSENCE_TIMEOUT) || 60_000,

  /**
   * Wait for loader absence, "60 000" by default.
   */
  TS_WAIT_LOADER_PRESENCE_TIMEOUT: Number(process.env.TS_WAIT_LOADER_PRESENCE_TIMEOUT) || 60_000,

  // -------------------------------------------- DASHBOARD --------------------------------------------

  /**
   * Common timeout for dashboard items, "5 000" by default
   */
  TS_COMMON_DASHBOARD_WAIT_TIMEOUT: Number(process.env.TS_COMMON_DASHBOARD_WAIT_TIMEOUT) || 5_000,

  /**
   * Timeout for clicking on dashboard menu items, "2 000" by default
   */
  TS_CLICK_DASHBOARD_ITEM_TIMEOUT: Number(process.env.TS_CLICK_DASHBOARD_ITEM_TIMEOUT) || 2_000,

  /**
   * Timeout for workspace stopped status, "30 000" by default
   */
  TS_DASHBOARD_WORKSPACE_STOP_TIMEOUT: Number(process.env.TS_DASHBOARD_WORKSPACE_STOP_TIMEOUT) || 30_000,

  // -------------------------------------------- PROJECT TREE --------------------------------------------

  /**
   * Expand item in project tree, "8 000" by default.
   */
  TS_EXPAND_PROJECT_TREE_ITEM_TIMEOUT: Number(process.env.TS_EXPAND_PROJECT_TREE_ITEM_TIMEOUT) || 8_000,

  // -------------------------------------------- EDITOR --------------------------------------------

  /**
   * Timeout for inetractions with editor tab - wait, click, select, "8 000" by default.
   */
  TS_EDITOR_TAB_INTERACTION_TIMEOUT: Number(process.env.TS_OPEN_PROJECT_TREE_TIMEOUT) || 8_000,

  // -------------------------------------------- IDE --------------------------------------------

  /**
   * Timeout for context menu manipulation, "10 000" by default
   */
  TS_DIALOG_WINDOW_DEFAULT_TIMEOUT: Number(process.env.TS_DIALOG_WINDOW_DEFAULT_TIMEOUT) || 10_000,

  /**
   * Timeout for clicking on visible item, "5 000" by default
   */
  TS_SELENIUM_CLICK_ON_VISIBLE_ITEM: Number(process.env.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM) || 5_000,

  // ----------------------------------------- PLUGINS -----------------------------------------

  /**
   * Common timeout for plugins.
   */
  TS_COMMON_PLUGIN_TEST_TIMEOUT: Number(process.env.TS_COMMON_PLUGIN_TEST_TIMEOUT) || 30_000
};
