import { TestConstants } from '../TestConstants';

/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

export abstract class Logger {
    /**
     * Uses for logging of the public methods of the pageobjects.
     * @param text log text
     */
    public static debug(text: string) {
        if (TestConstants.TS_SELENIUM_LOG_LEVEL === 'INFO') {
            return;
        }

        console.log(`        ▼ ${text}`);
    }

    /**
     * Uses for logging of the public methods of the {@link DriverHelper} or
     * private methods inside of pageobjects.
     * @param text log text
     */
    public static trace(text: string) {
        if (TestConstants.TS_SELENIUM_LOG_LEVEL === 'INFO') {
            return;
        }

        if (TestConstants.TS_SELENIUM_LOG_LEVEL === 'DEBUG') {
            return;
        }

        console.log(`            ‣ ${text}`);
    }

}
