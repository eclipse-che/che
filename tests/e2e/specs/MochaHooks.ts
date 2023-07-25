/** *******************************************************************
 * copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import 'reflect-metadata';
import { CLASSES, TYPES } from '../configs/inversify.types';
import { CheApiRequestHandler } from '../utils/request-handlers/CheApiRequestHandler';
import { TIMEOUT_CONSTANTS } from '../constants/TIMEOUT_CONSTANTS';
import * as monacoPageObjects from 'monaco-page-objects';
import * as vscodeExtensionTesterLocators from 'vscode-extension-tester-locators';
import { e2eContainer } from '../configs/inversify.config';
import { DriverHelper } from '../utils/DriverHelper';
import { ITestWorkspaceUtil } from '../utils/workspace/ITestWorkspaceUtil';
import { Logger } from '../utils/Logger';
import { BASE_TEST_CONSTANTS } from '../constants/BASE_TEST_CONSTANTS';
import { CHROME_DRIVER_CONSTANTS } from '../constants/CHROME_DRIVER_CONSTANTS';
import { MONACO_CONSTANTS } from '../constants/MONACO_CONSTANTS';

const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);

let latestWorkspace: string = '';

export function registerRunningWorkspace(workspaceName: string): void {
	Logger.debug(`with workspaceName:${workspaceName}`);
	latestWorkspace = workspaceName;
}

exports.mochaHooks = {
	beforeAll: [
		function enableRequestInterceptor(): void {
			if (BASE_TEST_CONSTANTS.TS_SELENIUM_REQUEST_INTERCEPTOR) {
				CheApiRequestHandler.enableRequestInterceptor();
			}
		},
		function enableResponseInterceptor(): void {
			if (BASE_TEST_CONSTANTS.TS_SELENIUM_RESPONSE_INTERCEPTOR) {
				CheApiRequestHandler.enableResponseInterceptor();
			}
		},
		function initMonacoPageObjects(): void {
			// init vscode-extension-tester monaco-page-objects
			monacoPageObjects.initPageObjects(
				MONACO_CONSTANTS.TS_SELENIUM_MONACO_PAGE_OBJECTS_USE_VERSION,
				MONACO_CONSTANTS.TS_SELENIUM_MONACO_PAGE_OBJECTS_BASE_VERSION,
				vscodeExtensionTesterLocators.getLocatorsPath(),
				driverHelper.getDriver(),
				'google-chrome'
			);
		},
		function prolongTimeoutConstantsInDebugMode(): void {
			if (BASE_TEST_CONSTANTS.TS_DEBUG_MODE) {
				for (const [timeout, seconds] of Object.entries(TIMEOUT_CONSTANTS)) {
					Object.defineProperty(TIMEOUT_CONSTANTS, timeout, {
						value: seconds * 100
					});
				}
			}
		}
	],
	afterEach: [
		// stop and remove running workspace
		function (this: Mocha.Context): void {
			if (this.currentTest?.state === 'failed') {
				if (BASE_TEST_CONSTANTS.DELETE_WORKSPACE_ON_FAILED_TEST) {
					Logger.info('Property DELETE_WORKSPACE_ON_FAILED_TEST is true - trying to stop and delete running workspace with API.');
					// await
					testWorkspaceUtil.stopAndDeleteWorkspaceByName(latestWorkspace);
				}
			}
		}
	],
	afterAll: [
		async function stopTheDriver(): Promise<void> {
			if (!BASE_TEST_CONSTANTS.TS_DEBUG_MODE && CHROME_DRIVER_CONSTANTS.TS_USE_WEB_DRIVER_FOR_TEST) {
				await driverHelper.getDriver().quit();
				Logger.info('Chrome driver session stopped.');
			}
		}
	]
};
