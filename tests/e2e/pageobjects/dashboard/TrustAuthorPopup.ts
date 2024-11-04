/** *******************************************************************
 * copyright (c) 2019-2023 Red Hat, Inc.
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
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';

@injectable()
export class TrustAuthorPopup {
	private static readonly CONTINUE_BUTTON: By = By.xpath('//button[text()="Continue"]');
	private static readonly TRUST_AUTHOR_POPUP_PAGE: By = By.xpath(
		'//span[contains(text(), "Do you trust the authors of this repository?")]'
	);

	constructor(
		@inject(CLASSES.DriverHelper)
		readonly driverHelper: DriverHelper
	) {}

	async clickContinue(timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM): Promise<void> {
		Logger.debug();

		await this.waitPopupIsOpened();
		await this.driverHelper.waitAndClick(TrustAuthorPopup.CONTINUE_BUTTON, timeout);
	}

	async waitPopupIsOpened(timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitVisibility(TrustAuthorPopup.TRUST_AUTHOR_POPUP_PAGE, timeout);
	}
}
