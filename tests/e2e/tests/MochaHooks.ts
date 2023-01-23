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
import { CLASSES } from '../inversify.types';
import { EditorType, TestConstants } from '../TestConstants';
import { AskForConfirmationTypeTheia, PreferencesHandlerTheia, TerminalRendererTypeTheia } from '../utils/theia/PreferencesHandlerTheia';
import { CheApiRequestHandler } from '../utils/requestHandlers/CheApiRequestHandler';
import { TimeoutConstants } from '../TimeoutConstants';
import * as monacoPageObjects from 'monaco-page-objects';
import * as vscodeExtensionTesterLocators from 'vscode-extension-tester-locators';
import { e2eContainer } from '../inversify.config';
import { DriverHelper } from '../utils/DriverHelper';

const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const preferencesHandler: PreferencesHandlerTheia = e2eContainer.get(CLASSES.PreferencesHandlerTheia);

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
        async function setTheiaSpecificPreferences() {
            if (TestConstants.TS_SELENIUM_EDITOR === EditorType.THEIA) {
                await preferencesHandler.setConfirmExit(AskForConfirmationTypeTheia.never);
                await preferencesHandler.setTerminalType(TerminalRendererTypeTheia.dom);
            }
        },
        async function initMonacoPageObjects() {
            if (TestConstants.TS_SELENIUM_EDITOR === EditorType.CHE_CODE) {
                // init vscode-extension-tester monaco-page-objects
                monacoPageObjects.initPageObjects(TestConstants.TS_SELENIUM_MONACO_PAGE_OBJECTS_USE_VERSION, TestConstants.TS_SELENIUM_MONACO_PAGE_OBJECTS_BASE_VERSION, vscodeExtensionTesterLocators.getLocatorsPath(), driverHelper.getDriver(), 'google-chrome');
            }
        },
        async function prolongTimeoutConstantsInDebugMode() {
            if (TestConstants.TS_DEBUG_MODE === true) {
                for (let [timeout, seconds] of Object.entries(TimeoutConstants)) {
                    Object.defineProperty(TimeoutConstants, timeout, {value: seconds * 100});
                }
            }
        },
    ]
};
