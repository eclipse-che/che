/** *******************************************************************
 * copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import 'reflect-metadata';
import { inject, injectable } from 'inversify';
import { DriverHelper } from '../utils/DriverHelper';
import { CLASSES } from '../configs/inversify.types';
import { Logger } from '../utils/Logger';
import { TIMEOUT_CONSTANTS } from '../constants/TIMEOUT_CONSTANTS';
import { CheCodeLocatorLoader } from '../pageobjects/ide/CheCodeLocatorLoader';
import { Workbench } from 'monaco-page-objects';

@injectable()
export class ProjectAndFileTests {
	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper,
		@inject(CLASSES.CheCodeLocatorLoader)
		private readonly cheCodeLocatorLoader: CheCodeLocatorLoader
	) {}

	async waitWorkspaceReadinessForCheCodeEditor(): Promise<void> {
		Logger.debug('waiting for editor.');
		try {
			const start: number = new Date().getTime();
			await this.driverHelper.waitVisibility(
				this.cheCodeLocatorLoader.webCheCodeLocators.Workbench.constructor,
				TIMEOUT_CONSTANTS.TS_SELENIUM_START_WORKSPACE_TIMEOUT
			);
			const end: number = new Date().getTime();
			Logger.debug(`editor was opened in ${end - start} seconds.`);
		} catch (err) {
			Logger.error(`waiting for workspace readiness failed: ${err}`);
			throw err;
		}
	}

	async performTrustAuthorDialog(): Promise<void> {
		Logger.debug();
		// sometimes the trust dialog does not appear at first time, for avoiding this problem we send click event for activating
		await new Workbench().click();
		await this.driverHelper.waitAndClick(
			this.cheCodeLocatorLoader.webCheCodeLocators.WelcomeContent.button,
			TIMEOUT_CONSTANTS.TS_DIALOG_WINDOW_DEFAULT_TIMEOUT
		);

		try {
			await this.driverHelper.waitAndClick(
				this.cheCodeLocatorLoader.webCheCodeLocators.WelcomeContent.button,
				TIMEOUT_CONSTANTS.TS_DIALOG_WINDOW_DEFAULT_TIMEOUT
			);
		} catch (e) {
			Logger.info('Second welcome content dialog box was not shown');
		}
	}
}
