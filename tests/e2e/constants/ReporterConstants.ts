/*********************************************************************
 * Copyright (c) 2020-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
export const ReporterConstants: any = {
    /**
     * Path to folder with load tests execution report.
     */
    TS_SELENIUM_LOAD_TEST_REPORT_FOLDER: process.env.TS_SELENIUM_LOAD_TEST_REPORT_FOLDER || './load-test-folder',

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
     * Log level settings, possible variants: 'INFO' (by default), 'DEBUG', 'TRACE'.
     */
    TS_SELENIUM_LOG_LEVEL: process.env.TS_SELENIUM_LOG_LEVEL || 'INFO',

    /**
     * Print all timeout variables when tests launch, default to false
     */
    TS_SELENIUM_PRINT_TIMEOUT_VARIABLES: process.env.TS_SELENIUM_PRINT_TIMEOUT_VARIABLES || false,
};
