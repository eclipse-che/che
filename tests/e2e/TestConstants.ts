/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

function getBaseUrl(): string {
    const baseUrl: string | undefined = process.env.TS_SELENIUM_BASE_URL;
    if (!baseUrl) {
        return 'http://sample-url';
    }

    return baseUrl.replace(/\/$/, '');
}

export const TestConstants = {
    /**
     * Base URL of the application which should be checked
     */
    TS_SELENIUM_BASE_URL: getBaseUrl(),

    /**
     * Base URl of web console OpenShift which uses to test OperatorHub.
     */
    TS_SELENIUM_WEB_CONSOLE_OCP_URL: process.env.TS_SELENIUM_WEB_CONSOLE_OCP_URL || 'https://console-openshift-console.apps.',

    /**
     * Run browser in "Headless" (hiden) mode, "false" by default.
     */
    TS_SELENIUM_HEADLESS: process.env.TS_SELENIUM_HEADLESS === 'true',

    /**
     * Run browser in "Fullscreen" (kiosk) mode, "false" by default.
     */
    TS_SELENIUM_LAUNCH_FULLSCREEN: process.env.TS_SELENIUM_LAUNCH_FULLSCREEN === 'true',

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
     * Default ammount of tries, "5" by default.
     */
    TS_SELENIUM_DEFAULT_ATTEMPTS: Number(process.env.TS_SELENIUM_DEFAULT_ATTEMPTS) || 5,

    /**
     * Default delay in milliseconds between tries, "1000" by default.
     */
    TS_SELENIUM_DEFAULT_POLLING: Number(process.env.TS_SELENIUM_DEFAULT_POLLING) || 1000,

    /**
     * Amount of tries for checking workspace status.
     */
    TS_SELENIUM_WORKSPACE_STATUS_ATTEMPTS: Number(process.env.TS_SELENIUM_WORKSPACE_STATUS_ATTEMPTS) || 90,

    /**
     * Delay in milliseconds between checking workspace status tries.
     */
    TS_SELENIUM_WORKSPACE_STATUS_POLLING: Number(process.env.TS_SELENIUM_WORKSPACE_STATUS_POLLING) || 10000,

    /**
     * Amount of tries for checking plugin precence.
     */
    TS_SELENIUM_PLUGIN_PRECENCE_ATTEMPTS: Number(process.env.TS_SELENIUM_PLUGIN_PRECENCE_ATTEMPTS) || 20,

    /**
     * Name of workspace created for 'Happy Path' scenario validation.
     */
    TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME: process.env.TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME || 'petclinic-dev-environment',

    /**
     * Using 'single-host' strategy, "false" by default.
     */
    TS_SELENIUM_SINGLE_HOST: process.env.TS_SELENIUM_SINGLE_HOST === 'true',

    /**
     * Value of OpenShift oAuth property determines how to login in installed application,
     * if 'false' as an user of application, if 'true' as a regular user of OCP.
     */
    TS_SELENIUM_VALUE_OPENSHIFT_OAUTH: process.env.TS_SELENIUM_VALUE_OPENSHIFT_OAUTH || 'false',

    /**
     * Username used to log in MultiUser Che.
     */
    TS_SELENIUM_USERNAME: process.env.TS_SELENIUM_USERNAME || 'che',

    /**
     * Password used to log in MultiUser Che.
     */
    TS_SELENIUM_PASSWORD: process.env.TS_SELENIUM_PASSWORD || '',

    /**
     * Log into OCP by using appropriate provider title.
     */
    TS_OCP_LOGIN_PAGE_PROVIDER_TITLE: process.env.TS_OCP_LOGIN_PAGE_PROVIDER_TITLE || '',

    /**
     * Path to folder with load tests execution report.
     */
    TS_SELENIUM_LOAD_TEST_REPORT_FOLDER: process.env.TS_SELENIUM_LOAD_TEST_REPORT_FOLDER || './load-test-folder',

    /**
     * Regular username used to login in OCP.
     */
    TS_SELENIUM_OCP_USERNAME: process.env.TS_SELENIUM_OCP_USERNAME || '',

    /**
     * Password regular user used to login in OCP.
     */
    TS_SELENIUM_OCP_PASSWORD: process.env.TS_SELENIUM_OCP_PASSWORD || '',

    /**
     * Email of regular user OpenShift to login CHE.
     */
    TS_SELENIUM_EMAIL_USER: process.env.TS_SELENIUM_EMAIL_USER || 'test@test.com',

    /**
     * First name of regular user OpenShift to login CHE.
     */
    TS_SELENIUM_FIRST_NAME: process.env.TS_SELENIUM_FIRST_NAME || 'qa',

    /**
     * Last name of regular user Openshift to login CHE.
     */
    TS_SELENIUM_LAST_NAME: process.env.TS_SELENIUM_LAST_NAME || 'test',

    /**
     * Delay between screenshots catching in the milliseconds for the execution screencast.
     */
    TS_SELENIUM_DELAY_BETWEEN_SCREENSHOTS: Number(process.env.TS_SELENIUM_DELAY_BETWEEN_SCREENSHOTS) || 1000,

    /**
     * Path to folder with tests execution report.
     */
    TS_SELENIUM_REPORT_FOLDER: process.env.TS_SELENIUM_REPORT_FOLDER || './report',

    /**
     * Enable or disable storing of execution screencast, "false" by default.
     */
    TS_SELENIUM_EXECUTION_SCREENCAST: process.env.TS_SELENIUM_EXECUTION_SCREENCAST === 'true',

    /**
     * Delete screencast after execution if all tests passed, "true" by default.
     */
    DELETE_SCREENCAST_IF_TEST_PASS: process.env.DELETE_SCREENCAST_IF_TEST_PASS !== 'false',

    /**
     * Remote driver URL.
     */
    TS_SELENIUM_REMOTE_DRIVER_URL: process.env.TS_SELENIUM_REMOTE_DRIVER_URL || '',

    /**
     * Stop and remove workspace if a test fails.
     */
    DELETE_WORKSPACE_ON_FAILED_TEST: process.env.DELETE_WORKSPACE_ON_FAILED_TEST === 'true',

    /**
     * Log level settings, possible variants: 'INFO' (by default), 'DEBUG', 'TRACE'.
     */
    TS_SELENIUM_LOG_LEVEL: process.env.TS_SELENIUM_LOG_LEVEL || 'INFO',

    /**
     * Running test suite - possible variants can be found in package.json scripts part.
     */
    TEST_SUITE: process.env.TEST_SUITE || 'test-happy-path',

    /**
     * The repo (with README.md in root) and access token are needed for to run test-git-ssh
     */
    TS_GITHUB_TEST_REPO: process.env.TS_GITHUB_TEST_REPO || '',

    /**
     * Token for a github repository with permissions which allow add the ssh keys
     */
    TS_GITHUB_TEST_REPO_ACCESS_TOKEN: process.env.TS_GITHUB_TEST_REPO_ACCESS_TOKEN || '',

    /**
     * Login for a user whom has been created in the test Openshift cluster. Need for Openshift connector test
     */
    TS_TEST_OPENSHIFT_PLUGIN_USERNAME: process.env.TS_TEST_OPENSHIFT_PLUGIN_USERNAME || '',

    /**
     * Password for a user whom has been created in the test Openshift cluster. Need for Openshift connector test
     */
    TS_TEST_OPENSHIFT_PLUGIN_PASSWORD: process.env.TS_TEST_OPENSHIFT_PLUGIN_PASSWORD || '',

    /**
     * The name of project in the Openshift plugin tree
     */
    TS_TEST_OPENSHIFT_PLUGIN_PROJECT: process.env.TS_TEST_OPENSHIFT_PLUGIN_PROJECT || '',

    /**
     * The name of the Openshift connector plugin component type
     */
    TS_TEST_OPENSHIFT_PLUGIN_COMPONENT_TYPE: process.env.TS_TEST_OPENSHIFT_PLUGIN_COMPONENT_TYPE || 'nodejs (s2i)',


    /**
     * The name of the Openshift connector plugin component version
     */
    TS_TEST_OPENSHIFT_PLUGIN_COMPONENT_VERSION: process.env.TS_TEST_OPENSHIFT_PLUGIN_COMPONENT_VERSION || 'latest',

    /**
     * Print all timeout variables when tests launch, defaulte to false
     */
    TS_SELENIUM_PRINT_TIMEOUT_VARIABLES: process.env.TS_SELENIUM_PRINT_TIMEOUT_VARIABLES || false,

    /**
     * URL of the workspace created by devworkspace-controller
     */
    TS_SELENIUM_DEVWORKSPACE_URL: process.env.TS_SELENIUM_DEVWORKSPACE_URL,

    /**
     * This variable specifies that run test is used for load testing and that all artifacts will be sent to ftp client.
     */
    TS_LOAD_TESTS: process.env.TS_LOAD_TESTS || 'false',

    /**
     * This variable determines whether to delete the workspace after the test or leave the workspace running.
     */
    TS_DELETE_PLUGINS_TEST_WORKSPACE: process.env.TS_DELETE_PLUGINS_TEST_WORKSPACE || 'true'

};
