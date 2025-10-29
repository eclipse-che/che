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
import { DriverHelper } from '../../utils/DriverHelper';
import { Logger } from '../../utils/Logger';
import { Key } from 'selenium-webdriver';

@injectable()
export class SourceControlView {
	constructor(
		@inject(CLASSES.DriverHelper)
		readonly driverHelper: DriverHelper
	) {}

	/**
	 * type the commit message and press Ctrl+Enter to commit the changes.
	 * @param textCommit Text to commit.
	 */
	async typeCommitMessage(textCommit: string): Promise<void> {
		Logger.debug(`Type commit text: "Commit ${textCommit}"`);

		await this.driverHelper.getDriver().actions().sendKeys(textCommit).perform();
		Logger.debug('Press Enter to commit the changes');
		await this.driverHelper.getDriver().actions().keyDown(Key.CONTROL).sendKeys(Key.ENTER).keyUp(Key.CONTROL).perform();
	}
}
