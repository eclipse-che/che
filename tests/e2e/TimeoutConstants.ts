/*********************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

export const TimeoutConstants = {
    // -------------------------------------------- INSTALLING AND STARTUP --------------------------------------------

    /**
     * Timeout in milliseconds waiting for install Eclipse Che by OperatorHub UI, "600 000" by default.
     */
    TS_SELENIUM_INSTALL_ECLIPSE_CHE_TIMEOUT: Number(process.env.TS_SELENIUM_START_WORKSPACE_TIMEOUT) || 600_000,

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

    /**
     * Timeout waiting for URL, "5 000" by default.
     */
    TS_WAIT_URL_TIMEOUT: Number(process.env.TS_WAIT_URL_TIMEOUT) || 5_000,

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

    // -------------------------------------------- LANGUAGE SERVER VALIDATION --------------------------------------------

    /**
     * Timeout in milliseconds waiting for language server initialization, "180 000" by default.
     */
    TS_SELENIUM_LANGUAGE_SERVER_START_TIMEOUT: Number(process.env.TS_SELENIUM_LANGUAGE_SERVER_START_TIMEOUT) || 180_000,

    /**
     * Timeout for suggestion invoking, "30 000" by default.
     */
    TS_SUGGESTION_TIMEOUT: Number(process.env.TS_OPEN_PROJECT_TREE_TIMEOUT) || 30_000,

    /**
     * Timeout for error highlighting presence, "120 000" by default
     */
    TS_ERROR_HIGHLIGHTING_TIMEOUT: Number(process.env.TS_OPEN_PROJECT_TREE_TIMEOUT) || 90_000,


    // -------------------------------------------- PROJECT TREE --------------------------------------------

    /**
     * Wait for IDE showing project tree tab, "20 000" by default.
     */
    TS_PROJECT_TREE_TIMEOUT: Number(process.env.TS_OPEN_PROJECT_TREE_TIMEOUT) || 20_000,

    /**
     * Click on item timeout (project tree), "10 000" by default.
     */
    TS_PROJECT_TREE_CLICK_ON_ITEM_TIMEOUT: Number(process.env.TS_PROJECT_TREE_CLICK_ON_ITEM_TIMEOUT) || 10_000,

    /**
     * Expand item in project tree, "5 000" by default.
     */
    TS_EXPAND_PROJECT_TREE_ITEM_TIMEOUT: Number(process.env.TS_EXPAND_PROJECT_TREE_ITEM_TIMEOUT) || 5_000,

    /**
     * Timeout for waiting workspace sample import, 50_000 by default.
     */
    TS_IMPORT_PROJECT_DEFAULT_POLLING: Number(process.env.TS_SELENIUM_DEFAULT_POLLING) || 50_000,

    // -------------------------------------------- EDITOR --------------------------------------------

    /**
     * Timeout for inetractions with editor tab - wait, click, select, "5 000" by default.
     */
    TS_EDITOR_TAB_INTERACTION_TIMEOUT: Number(process.env.TS_OPEN_PROJECT_TREE_TIMEOUT) || 5_000,

    /**
     * Wait for file to be opened in editor, "30 000" by default.
     */
    TS_OPEN_EDITOR_TIMEOUT: Number(process.env.TS_OPEN_PROJECT_TREE_TIMEOUT) || 30_000,

    /**
     * Wait for suggestion container closure, "3 000" by default.
     */
    TS_CLOSE_SUGGESTION_CONTAINER_TIMEOUT: Number(process.env.TS_CLOSE_SUGGESTION_CONTAINER_TIMEOUT) || 3_000,


    // -------------------------------------------- IDE --------------------------------------------

    /**
     * Timeout for context menu manipulation, "10 000" by default
     */
    TS_CONTEXT_MENU_TIMEOUT: Number(process.env.TS_CONTEXT_MENU_TIMEOUT) || 10_000,

    /**
     * Timeout for interactions with Notification center - open, close, "10 000" by default.
     */
    TS_NOTIFICATION_CENTER_TIMEOUT: Number(process.env.TS_OPEN_PROJECT_TREE_TIMEOUT) || 10_000,

    /**
     * Timeout for debugger to connect, "60 000" by default
     */
    TS_DEBUGGER_CONNECTION_TIMEOUT: Number(process.env.TS_DEBUGGER_CONNECTION_TIMEOUT) || 60_000,

    /**
     * Timeout for context menu manipulation, "10 000" by default
     */
    TS_DIALOG_WINDOW_DEFAULT_TIMEOUT: Number(process.env.TS_DIALOG_WINDOW_DEFAULT_TIMEOUT) || 10_000,

    /**
     * Timeout for breakpoint interactions, "20 000" by default
     */
    TS_BREAKPOINT_DEFAULT_TIMEOUT: Number(process.env.TS_BREAKPOINT_DEFAULT_TIMEOUT) || 20_000,

    /**
     * Timeout for interactions with Git Plugin container, "20 000" by default
     */
    TS_GIT_CONAINER_INTERACTION_TIMEOUT: Number(process.env.TS_GIT_CONAINER_INTERACTION_TIMEOUT) || 20_000,

    /**
     * Timeout for toolbars interaction, "20 000" by default
     */
    TS_SELENIUM_TOOLBAR_TIMEOUT: Number(process.env.TS_SELENIUM_TOOLBAR_TIMEOUT) || 20_000,

    /**
     * Timeout for clicking on visible item, "3 000" by default
     */
    TS_SELENIUM_CLICK_ON_VISIBLE_ITEM: Number(process.env.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM) || 3_000,

    /**
     * Timeout for OpenDialogWidget class, "5 000" by default
     */
    TS_SELENIUM_DIALOG_WIDGET_TIMEOUT: Number(process.env.TS_SELENIUM_DIALOG_WIDGET_TIMEOUT) || 5_000,

    /**
     * Default timeout for interaction with terminal, "3 000" by default
     */
    TS_SELENIUM_TERMINAL_DEFAULT_TIMEOUT: Number(process.env.TS_SELENIUM_TERMINAL_DEFAULT_TIMEOUT) || 5_000,

    /**
     * Default timeout for preview widget, "10 000" by default
     */
    TS_SELENIUM_PREVIEW_WIDGET_DEFAULT_TIMEOUT: Number(process.env.TS_SELENIUM_PREVIEW_WIDGET_DEFAULT_TIMEOUT) || 10_000,

    /**
     * Timeout for opening quick menu from top panel, "10 000" by default
     */
    TS_SELENIUM_TOP_MENU_QUICK_CONTAINER_TIMEOUT: Number(process.env.TS_SELENIUM_TOP_MENU_QUICK_CONTAINER_TIMEOUT) || 10_000,

    /**
     * Timeout waiting for url, "10 000" by default
     */
    TS_SELENIUM_WAIT_FOR_URL: Number(process.env.TS_SELENIUM_WAIT_FOR_URL) || 10_000,

    /**
     * Timeout waiting for an executed task to finish, waiting stops automatically if any exit code notification is present, "300 000" by default
     */
    TS_SELENIUM_WAIT_TASK_EXIT_CODE_TIMEOUT: Number(process.env.TS_SELENIUM_WAIT_TASK_EXIT_CODE_TIMEOUT) || 300_000,


    // ------------------------------------ OPENSHIFT RELATED ------------------------------------

    /**
     * Timeout for obtaining cluster IP, "10 000" by default.
     */
    TS_GET_CLUSTER_IP_TIMEOUT: Number(process.env.TS_GET_CLUSTER_IP_TIMEOUT) || 60_000,

    /**
     * Timeout for waiting fr openshift connector tree, "10 000" by default.
     */
    TS_WAIT_OPENSHIFT_CONNECTOR_TREE_TIMEOUT: Number(process.env.TS_WAIT_OPENSHIFT_CONNECTOR_TREE_TIMEOUT) || 40_000,

};
