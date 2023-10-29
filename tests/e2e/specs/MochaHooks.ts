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
import { allure } from 'allure-mocha/runtime';
import { BASE_TEST_CONSTANTS } from '../constants/BASE_TEST_CONSTANTS';
import { MONACO_CONSTANTS } from '../constants/MONACO_CONSTANTS';
import { CHROME_DRIVER_CONSTANTS } from '../constants/CHROME_DRIVER_CONSTANTS';
import { decorate, injectable, unmanaged } from 'inversify';
import { Main } from '@eclipse-che/che-devworkspace-generator/lib/main';
import { LocatorLoader } from 'monaco-page-objects/out/locators/loader';
import { REPORTER_CONSTANTS } from '../constants/REPORTER_CONSTANTS';
import { WorkspaceHandlingTests } from '../tests-library/WorkspaceHandlingTests';

const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
let latestWorkspace: string = '';
export let rpApi: any = undefined;

export function registerRunningWorkspace(workspaceName: string): void {
	workspaceName !== ''
		? Logger.debug(`with workspaceName:${workspaceName}`)
		: ((): void => {
				Logger.debug('delete workspace name');
				WorkspaceHandlingTests.clearWorkspaceName();
		  })();

	latestWorkspace = workspaceName;
}

exports.mochaHooks = {
	beforeAll: [
		function initRPApi(): any {
			rpApi = require('@reportportal/agent-js-mocha/lib/publicReportingAPI.js');
		},

		function decorateExternalClasses(): void {
			decorate(injectable(), Main);
			decorate(injectable(), LocatorLoader);
			decorate(unmanaged(), LocatorLoader, 0);
			decorate(unmanaged(), LocatorLoader, 1);
			decorate(unmanaged(), LocatorLoader, 2);
		},

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
		// init vscode-extension-tester monaco-page-objects
		function initMonacoPageObjects(): void {
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
						value: seconds * 2
					});
				}
			}
		}
	],
	afterEach: [
		async function saveAllureAttachments(this: Mocha.Context): Promise<void> {
			if (REPORTER_CONSTANTS.SAVE_ALLURE_REPORT_DATA && this.currentTest?.state === 'failed') {
				try {
					const screenshot: string = await driverHelper.getDriver().takeScreenshot();
					allure.attachment('Screenshot', Buffer.from(screenshot, 'base64'), 'image/png');
				} catch (e) {
					allure.attachment('No screenshot', 'Could not take a screenshot', 'text/plain');
				}
			}
		},
		async function saveReportportalAttachments(this: Mocha.Context): Promise<void> {
			if (REPORTER_CONSTANTS.SAVE_RP_REPORT_DATA && this.currentTest?.state === 'failed') {
				try {
					const screenshot: string = await driverHelper.getDriver().takeScreenshot();
					const attachment: { name: string; type: string; content: string } = {
						name: 'screenshot.png',
						type: 'image/png',
						content: screenshot
					};
					rpApi.error('Screenshot on fail: ', attachment);
				} catch (e) {
					rpApi.error('Could not attach the screenshot');
				}
			}
		},
		// stop and remove running workspace
		function deleteWorkspaceOnFailedTest(this: Mocha.Context): void {
			if (this.currentTest?.state === 'failed') {
				if (BASE_TEST_CONSTANTS.DELETE_WORKSPACE_ON_FAILED_TEST) {
					Logger.info('Property DELETE_WORKSPACE_ON_FAILED_TEST is true - trying to stop and delete running workspace with API.');
					const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);
					testWorkspaceUtil.stopAndDeleteWorkspaceByName(latestWorkspace);
				}
			}
		}
	],
	afterAll: [
		// stop and remove running workspace
		function deleteAllWorkspacesOnFinish(): void {
			if (BASE_TEST_CONSTANTS.DELETE_ALL_WORKSPACES_ON_RUN_FINISH) {
				Logger.info(
					'Property DELETE_WORKSPACE_ON_FAILED_TEST is true - trying to stop and delete all running workspace after test run with API.'
				);
				const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);
				try {
					testWorkspaceUtil.stopAndDeleteAllRunningWorkspaces();
				} catch (e) {
					Logger.trace('Running workspaces not found');
				}
			}
		},
		async function stopTheDriver(): Promise<void> {
			if (!BASE_TEST_CONSTANTS.TS_DEBUG_MODE && CHROME_DRIVER_CONSTANTS.TS_USE_WEB_DRIVER_FOR_TEST) {
				// ensure that fired events done
				await driverHelper.wait(5000);
				await driverHelper.quit();
			}
		}
	]
};
