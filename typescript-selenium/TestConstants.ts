/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

function readEnvAndSetValue(envProperty: any, defaultValue: string | number | boolean): any {
    const propertyValue = envProperty
    if (!propertyValue) {
        return defaultValue;
    }

    return propertyValue
}



export const TestConstants = {
    TS_SELENIUM_HEADLESS: readEnvAndSetValue(process.env.TS_SELENIUM_HEADLESS, false),

    TS_SELENIUM_START_WORKSPACE_TIMEOUT: readEnvAndSetValue(process.env.TS_SELENIUM_START_WORKSPACE_TIMEOUT, 240000),
    TS_SELENIUM_LOAD_PAGE_TIMEOUT: readEnvAndSetValue(process.env.TS_SELENIUM_LOAD_PAGE_TIMEOUT, 120000),
    TS_SELENIUM_LANGUAGE_SERVER_START_TIMEOUT: readEnvAndSetValue(process.env.TS_SELENIUM_LANGUAGE_SERVER_START_TIMEOUT, 180000),

    TS_SELENIUM_DEFAULT_TIMEOUT: readEnvAndSetValue(process.env.TS_SELENIUM_DEFAULT_TIMEOUT, 20000),
    TS_SELENIUM_DEFAULT_ATTEMPTS: readEnvAndSetValue(process.env.TS_SELENIUM_DEFAULT_ATTEMPTS, 5),
    TS_SELENIUM_DEFAULT_POLLING: readEnvAndSetValue(process.env.TS_SELENIUM_DEFAULT_POLLING, 1000),

    TS_SELENIUM_WORKSPACE_STATUS_ATTEMPTS: readEnvAndSetValue(process.env.TS_SELENIUM_WORKSPACE_STATUS_ATTEMPTS, 90),
    TS_SELENIUM_WORKSPACE_STATUS_POLLING: readEnvAndSetValue(process.env.TS_SELENIUM_WORKSPACE_STATUS_POLLING, 10000),

    TS_SELENIUM_BASE_URL: readEnvAndSetValue(process.env.TS_SELENIUM_BASE_URL, "http://che-che.192.168.99.100.nip.io")
}
