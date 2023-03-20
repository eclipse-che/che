/*********************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import 'reflect-metadata';
import { CLASSES, TYPES } from '../configs/inversify.types';
import { TestConstants } from '../constants/TestConstants';
import { CheApiRequestHandler } from '../utils/request-handlers/CheApiRequestHandler';
import { TimeoutConstants } from '../constants/TimeoutConstants';
import * as monacoPageObjects from 'monaco-page-objects';
import * as vscodeExtensionTesterLocators from 'vscode-extension-tester-locators';
import { e2eContainer } from '../configs/inversify.config';
import { DriverHelper } from '../utils/DriverHelper';
import { ITestWorkspaceUtil } from '../utils/workspace/ITestWorkspaceUtil';
import { Logger } from '../utils/Logger';

const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);

let latestWorkspace: string = '';

export function registerRunningWorkspace(workspaceName: string) {
    Logger.debug(`MochaHooks.registerRunningWorkspace with workspaceName:${workspaceName}`);
    latestWorkspace = workspaceName;
}

exports.mochaHooks = {
    beforeAll: [
        async function enableRequestInterceptor() {
            if (TestConstants.TS_SELENIUM_REQUEST_INTERCEPTOR) {
                CheApiRequestHandler.enableRequestInteceptor();
            }
        },
        async function enableResponseInterceptor() {
            if (TestConstants.TS_SELENIUM_RESPONSE_INTERCEPTOR) {
                CheApiRequestHandler.enableResponseInterceptor();
            }
        },
        async function initMonacoPageObjects() {
            // init vscode-extension-tester monaco-page-objects
            monacoPageObjects.initPageObjects(TestConstants.TS_SELENIUM_MONACO_PAGE_OBJECTS_USE_VERSION, TestConstants.TS_SELENIUM_MONACO_PAGE_OBJECTS_BASE_VERSION, vscodeExtensionTesterLocators.getLocatorsPath(), driverHelper.getDriver(), 'google-chrome');
        },
        async function prolongTimeoutConstantsInDebugMode() {
            if (TestConstants.TS_DEBUG_MODE === 'true') {
                for (let [timeout, seconds] of Object.entries(TimeoutConstants)) {
                    Object.defineProperty(TimeoutConstants, timeout, {value: seconds * 100});
                }
            }
        },
    ],
    afterAll: [
        // stop and remove running workspace
        async () => {
            if (TestConstants.DELETE_WORKSPACE_ON_FAILED_TEST) {
                Logger.info('Property DELETE_WORKSPACE_ON_FAILED_TEST is true - trying to stop and delete running workspace with API.');
                testWorkspaceUtil.stopAndDeleteWorkspaceByName(latestWorkspace);
            }
        },
        async function stopTheDriver() {
            if (!TestConstants.TS_DEBUG_MODE) {
                await driverHelper.getDriver().quit();
                Logger.info('Chrome driver session stopped.');
            }
        },
    ]
};
