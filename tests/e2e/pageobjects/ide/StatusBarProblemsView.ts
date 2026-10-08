/** *******************************************************************
 * copyright (c) 2026 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { inject, injectable } from 'inversify';
import { CLASSES } from '../../configs/inversify.types';
import { By } from 'selenium-webdriver';
import { DriverHelper } from '../../utils/DriverHelper';
import { Logger } from '../../utils/Logger';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';

@injectable()
export class StatusBarProblemsView {
	private static readonly PROBLEMS_STATUS_BAR_ITEM: By = By.id('status.problems');
	private static readonly PROBLEMS_PANEL: By = By.id('workbench.parts.panel');
	private static readonly PROBLEMS_PANEL_BODY: By = By.xpath('//div[@id="workbench.parts.panel"]/div[contains(@class, "content")]');

	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper
	) {}

	async openProblemsView(timeout: number = TIMEOUT_CONSTANTS.TS_IDE_LOAD_TIMEOUT): Promise<void> {
		Logger.debug();

		// the status bar item may still be re-created while the IDE loads after a restart;
		// waitAndClick re-finds the element on every attempt within the timeout, handling that race
		await this.driverHelper.waitAndClick(StatusBarProblemsView.PROBLEMS_STATUS_BAR_ITEM, timeout);
		await this.driverHelper.waitVisibility(StatusBarProblemsView.PROBLEMS_PANEL, timeout);
	}

	/**
	 * opens the "Problems" panel from the status bar and returns the text shown in it,
	 * e.g. "No problems have been detected in the workspace." when there are no problems.
	 */
	async openProblemsViewAndGetText(timeout: number = TIMEOUT_CONSTANTS.TS_IDE_LOAD_TIMEOUT): Promise<string> {
		Logger.debug();

		await this.openProblemsView(timeout);

		return this.driverHelper.waitAndGetText(StatusBarProblemsView.PROBLEMS_PANEL_BODY, timeout);
	}
}
