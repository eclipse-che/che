import { TestConstants } from '../constants/TestConstants';

/*********************************************************************
 * Copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

export abstract class Logger {

    /**
     * Uses for logging of fatal errors.
     * @param text log text
     */
    public static error(text: string, indentLevel: number = 1) {
        this.logText(indentLevel, `[ERROR] ${text}`);
    }

    /**
     * Uses for logging of recoverable errors and general warnings.
     * @param text log text
     */
    public static warn(text: string, indentLevel: number = 1) {
        if (TestConstants.TS_SELENIUM_LOG_LEVEL === 'ERROR') {
            return;
        }
        this.logText(indentLevel, `[WARN] ${text}`);
    }

    /**
     * Uses for logging of the public methods of the pageobjects.
     * @param text log text
     */
    public static info(text: string, indentLevel: number = 3) {
        if (TestConstants.TS_SELENIUM_LOG_LEVEL === 'ERROR' ||
            TestConstants.TS_SELENIUM_LOG_LEVEL === 'WARN') {
            return;
        }
        this.logText(indentLevel, `• ${text}`);
    }

    /**
     * Uses for logging of the public methods of the pageobjects.
     * @param text log text
     */
    public static debug(text: string, indentLevel: number = 5) {
        if (TestConstants.TS_SELENIUM_LOG_LEVEL === 'ERROR' ||
            TestConstants.TS_SELENIUM_LOG_LEVEL === 'WARN' ||
            TestConstants.TS_SELENIUM_LOG_LEVEL === 'INFO') {
            return;
        }
        this.logText(indentLevel, `▼ ${text}`);
    }

    /**
     * Uses for logging of the public methods of the {@link DriverHelper} or
     * private methods inside of pageobjects.
     * @param text log text
     */
    public static trace(text: string, indentLevel: number = 6) {
        if (TestConstants.TS_SELENIUM_LOG_LEVEL === 'ERROR' ||
            TestConstants.TS_SELENIUM_LOG_LEVEL === 'WARN' ||
            TestConstants.TS_SELENIUM_LOG_LEVEL === 'INFO' ||
            TestConstants.TS_SELENIUM_LOG_LEVEL === 'DEBUG') {
            return;
        }
        this.logText(indentLevel, `‣ ${text}`);
    }

    private static logText(messageIndentationLevel: number, text: string) {
        // start group for every level
        for (let i = 0; i < messageIndentationLevel; i++) {
            console.group();
        }
        // print the trimmed text
        // if multiline, the message should be properly padded
        console.log(text);
        // end group for every level
        for (let i = 0; i < messageIndentationLevel; i++) {
            console.groupEnd();
        }
    }
}
