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
import { By, Key, WebElement } from 'selenium-webdriver';
import { DriverHelper } from '../../utils/DriverHelper';
import { Logger } from '../../utils/Logger';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';

@injectable()
export class RestartWorkspaceDialog {
	private static readonly RESTART_BUTTON: By = By.xpath('//div[@class="dialog-buttons"]//a[text()="Restart"]');
	private static readonly RESTART_WORKSPACE_BUTTON: By = By.xpath('//div[@class="dialog-buttons"]//a[text()="Restart your workspace"]');
	private static readonly RESTART_WITH_DEFAULT_DEVFILE_BUTTON: By = By.xpath('//span[text()="Restart with default devfile"]');
	private static readonly ERROR_DIALOG_TEXT: By = By.xpath('//*[@class="dialog-message-text"]');
	private static readonly ERROR_DIALOG_DETAIL: By = By.xpath('//*[@class="dialog-message-detail"]');

	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper
	) {}

	async restartFromLocalDevfile(projectName: string): Promise<void> {
		Logger.debug();

		await this.driverHelper.getDriver().actions().keyDown(Key.CONTROL).sendKeys('p').keyUp(Key.CONTROL).perform();
		await this.driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);

		await this.driverHelper.getDriver().actions().sendKeys('>Dev Spaces: Restart Workspace from Local Devfile').perform();
		await this.driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);
		await this.driverHelper.getDriver().actions().sendKeys(Key.ENTER).perform();
		await this.driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);

		await this.driverHelper.getDriver().actions().sendKeys(`/projects/${projectName}/devfile.yaml`).perform();
		await this.driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);
		await this.driverHelper.getDriver().actions().sendKeys(Key.ENTER).perform();
		await this.driverHelper.wait(TIMEOUT_CONSTANTS.TS_SELENIUM_DEFAULT_POLLING);

		await this.driverHelper.waitAndClick(RestartWorkspaceDialog.RESTART_BUTTON);
	}

	async confirmRestartWorkspace(timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_PLUGIN_TEST_TIMEOUT): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(RestartWorkspaceDialog.RESTART_WORKSPACE_BUTTON, timeout);
	}

	async clickRestartWithDefaultDevfile(timeout: number = TIMEOUT_CONSTANTS.TS_SELENIUM_START_WORKSPACE_TIMEOUT): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(RestartWorkspaceDialog.RESTART_WITH_DEFAULT_DEVFILE_BUTTON, timeout);
	}

	async getErrorDialogText(timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_PLUGIN_TEST_TIMEOUT): Promise<string> {
		Logger.debug();

		const element: WebElement = await this.driverHelper.waitVisibility(RestartWorkspaceDialog.ERROR_DIALOG_TEXT, timeout);
		return element.getText();
	}

	async getErrorDetailText(timeout: number = TIMEOUT_CONSTANTS.TS_COMMON_PLUGIN_TEST_TIMEOUT): Promise<string> {
		Logger.debug();

		const element: WebElement = await this.driverHelper.waitVisibility(RestartWorkspaceDialog.ERROR_DIALOG_DETAIL, timeout);
		return element.getText();
	}
}
