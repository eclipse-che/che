/** *******************************************************************
 * copyright (c) 2025 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { inject, injectable } from 'inversify';
import 'reflect-metadata';
import { CLASSES } from '../../configs/inversify.types';
import { By } from 'selenium-webdriver';
import { DriverHelper } from '../../utils/DriverHelper';
import { Logger } from '../../utils/Logger';

@injectable()
export class RestrictedModeButton {
	private static readonly RESTRICTED_MODE_BUTTON: By = By.id('status.workspaceTrust');

	constructor(
		@inject(CLASSES.DriverHelper)
		readonly driverHelper: DriverHelper
	) {}

	async clickOnRestrictedModeButton(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(RestrictedModeButton.RESTRICTED_MODE_BUTTON);
	}

	async isRestrictedModeButtonDisappearance(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitDisappearance(RestrictedModeButton.RESTRICTED_MODE_BUTTON);
	}
}
