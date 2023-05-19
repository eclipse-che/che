/*********************************************************************
 * Copyright (c) 2019-2023 Red Hat, Inc.
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

export enum GitProviderType {
    GITHUB = 'github',
    GITLAB = 'gitlab',
    BITBUCKET = 'bitbucket'
}

export enum KubernetesCommandLineTool {
    OC = 'oc',
    KUBECTL = 'kubectl',
}

export const TestConstants: any = {
    /**
     * Base URL of the application which should be checked
     */
    TS_SELENIUM_BASE_URL: getBaseUrl(),

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
     * Editor the tests are running against, "code" by default.
     * Possible values: "che-code"
     */
    TS_SELENIUM_EDITOR: process.env.TS_SELENIUM_EDITOR || 'che-code',

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

    /**
     * Default amount of tries, "5" by default.
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
     * Name of workspace created for 'Happy Path' scenario validation.
     */
    TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME: process.env.TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME || 'EmptyWorkspace',

    /**
     * Value of OpenShift oAuth property determines how to login in installed application,
     * if 'false' as an user of application, if 'true' as a regular user of OCP.
     */
    TS_SELENIUM_VALUE_OPENSHIFT_OAUTH: process.env.TS_SELENIUM_VALUE_OPENSHIFT_OAUTH === 'true',

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
     * Enable Axios request interceptor, false by default
     */
    TS_SELENIUM_REQUEST_INTERCEPTOR: process.env.TS_SELENIUM_REQUEST_INTERCEPTOR === 'true',

    /**
     * Enable Axios response interceptor, false by default
     */
    TS_SELENIUM_RESPONSE_INTERCEPTOR: process.env.TS_SELENIUM_RESPONSE_INTERCEPTOR === 'true',

    /**
     * Running test suite - possible variants can be found in package.json scripts part.
     */
    TEST_SUITE: process.env.TEST_SUITE || 'userstory',

    /**
     * Print all timeout variables when tests launch, default to false
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
     * Constant, which prolong timeout constants for local debug.
     */
    TS_DEBUG_MODE: process.env.TS_DEBUG_MODE === 'true',

    E2E_OCP_CLUSTER_VERSION: process.env.E2E_OCP_CLUSTER_VERSION || '4.x',

    TS_SAMPLE_LIST: process.env.TS_SAMPLE_LIST || 'Node.js MongoDB,Node.js Express',

    /* -------------------------------------------
    |  The factory tests related constants
    ----------------------------------------------*/
    TS_SELENIUM_FACTORY_GIT_PROVIDER: process.env.TS_SELENIUM_FACTORY_GIT_PROVIDER || GitProviderType.GITHUB,

    TS_SELENIUM_FACTORY_GIT_REPO_URL: process.env.TS_SELENIUM_FACTORY_GIT_REPO_URL || '',

    TS_SELENIUM_IS_PRIVATE_FACTORY_GIT_REPO: process.env.TS_SELENIUM_IS_PRIVATE_FACTORY_GIT_REPO === 'true',

    TS_SELENIUM_FACTORY_GIT_REPO_BRANCH: process.env.TS_SELENIUM_FACTORY_GIT_REPO_BRANCH || 'master',

    TS_SELENIUM_FACTORY_URL(): string {
        return process.env.TS_SELENIUM_FACTORY_URL || TestConstants.TS_SELENIUM_BASE_URL + '/dashboard/#/' + this.TS_SELENIUM_FACTORY_GIT_REPO_URL;
    },

    TS_SELENIUM_GIT_PROVIDER_USERNAME: process.env.TS_SELENIUM_GIT_PROVIDER_USERNAME || '',

    TS_SELENIUM_GIT_PROVIDER_PASSWORD: process.env.TS_SELENIUM_GIT_PROVIDER_PASSWORD || '',

    TS_SELENIUM_GIT_PROVIDER_IS_LDAP_LOGIN: process.env.TS_SELENIUM_GIT_PROVIDER_IS_LDAP_LOGIN === 'true',

    TS_SELENIUM_GIT_PROVIDER_OAUTH: process.env.TS_SELENIUM_GIT_PROVIDER_OAUTH === 'true',

    TS_SELENIUM_PROJECT_ROOT_FILE_NAME: process.env.TS_SELENIUM_PROJECT_ROOT_FILE_NAME || 'devfile.yaml',

    /* -------------------------------------------
   |  The api tests related constants
   ----------------------------------------------*/

    TS_API_TEST_KUBERNETES_COMMAND_LINE_TOOL: process.env.TS_API_TEST_KUBERNETES_COMMAND_LINE_TOOL || KubernetesCommandLineTool.OC,

    // 'quay.io/devfile/universal-developer-image:latest'
    // is default assigned by DevWorkspaceConfigurationHelper.generateDevfileContext() using @eclipse-che/che-devworkspace-generator
    TS_API_TEST_UDI_IMAGE: process.env.TS_API_TEST_UDI_IMAGE || undefined,

    // https://eclipse-che.github.io/che-plugin-registry/main/v3/plugins/che-incubator/che-code/latest/devfile.yaml
    // is default assigned by DevWorkspaceConfigurationHelper.generateDevfileContext() using @eclipse-che/che-devworkspace-generator
    TS_API_TEST_CHE_CODE_EDITOR_DEVFILE_URI: process.env.TS_API_TEST_CHE_CODE_EDITOR_DEVFILE_URI || undefined,

    // https://eclipse-che.github.io/che-plugin-registry/main/v3
    // is default assigned by DevWorkspaceConfigurationHelper.generateDevfileContext() using @eclipse-che/che-devworkspace-generator
    TS_API_TEST_PLUGIN_REGISTRY_URL: process.env.TS_API_TEST_PLUGIN_REGISTRY_URL || undefined,

    TS_API_TEST_NAMESPACE: process.env.TS_API_TEST_NAMESPACE || undefined,

    // choose from repo https://github.com/eclipse-che/che-devfile-registry/tree/main/devfiles file as raw
    TS_API_TEST_LINK_TO_META_YAML: process.env.TS_API_TEST_LINK_TO_META_YAML || 'https://raw.githubusercontent.com/eclipse-che/che-devfile-registry/main/devfiles/java-web-spring/meta.yaml'
};
