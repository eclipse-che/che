/*********************************************************************
 * Copyright (c) 2020-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
export const UITestConstants: any = {
    /**
     * Remote driver URL.
     */
    TS_SELENIUM_REMOTE_DRIVER_URL: process.env.TS_SELENIUM_REMOTE_DRIVER_URL || '',

    /**
     * Run browser in "Headless" (hidden) mode, "false" by default.
     */
    TS_SELENIUM_HEADLESS: process.env.TS_SELENIUM_HEADLESS === 'true',

    /**
     * Create instance of chromedriver, "true" by default. Should be "false" to run only API tests.
     */
    TS_USE_WEB_DRIVER_FOR_TEST: process.env.TS_USE_WEB_DRIVER_FOR_TEST !== 'false',

    /**
     * Run browser in "Fullscreen" (kiosk) mode.
     * Default to true if undefined
     */
    TS_SELENIUM_LAUNCH_FULLSCREEN: (process.env.TS_SELENIUM_LAUNCH_FULLSCREEN !== 'false'),

    /**
     * Run browser with an enabled or disabled W3C protocol (on Chrome  76 and upper, it is enabled by default), "true" by default.
     */
    TS_SELENIUM_W3C_CHROME_OPTION: process.env.TS_SELENIUM_W3C_CHROME_OPTION !== 'false',

    /**
     * Browser width resolution, "1920" by default.
     */
    TS_SELENIUM_RESOLUTION_WIDTH: Number(process.env.TS_SELENIUM_RESOLUTION_WIDTH) || 1920,

    /**
     * Browser height resolution, "1080" by default.
     */
    TS_SELENIUM_RESOLUTION_HEIGHT: Number(process.env.TS_SELENIUM_RESOLUTION_HEIGHT) || 1080,

    /**
     * Base version of VSCode editor for monaco-page-objects, "1.37.0" by default.
     */
    TS_SELENIUM_MONACO_PAGE_OBJECTS_BASE_VERSION: process.env.TS_SELENIUM_MONACO_PAGE_OBJECTS_BASE_VERSION || '1.37.0',

    /**
     * Latest compatible version to be used, based on versions available in
     * https://github.com/redhat-developer/vscode-extension-tester/tree/master/locators/lib ,
     * "1.73.0" by default.
     */
    TS_SELENIUM_MONACO_PAGE_OBJECTS_USE_VERSION: process.env.TS_SELENIUM_MONACO_PAGE_OBJECTS_USE_VERSION || '1.73.0',
};
