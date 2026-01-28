/** *******************************************************************
 * copyright (c) 2019-2025 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import 'reflect-metadata';
import { inject, injectable } from 'inversify';
import { DriverHelper } from '../../utils/DriverHelper';
import { CLASSES } from '../../configs/inversify.types';
import { By } from 'selenium-webdriver';
import { Logger } from '../../utils/Logger';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';
import { OcpImportFromGitPage } from './OcpImportFromGitPage';
import { e2eContainer } from '../../configs/inversify.config';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { satisfies } from 'compare-versions';

@injectable()
export class OcpMainPage {
	private static readonly MAIN_PAGE_HEADER: By = By.id('page-main-header');
	private static readonly SELECT_ROLE_BUTTON: By = By.xpath('//*[@data-test-id="perspective-switcher-toggle"]');
	private static readonly ADD_BUTTON: By = By.xpath('//*[@data-test-id="+Add-header"]');
	private static readonly IMPORT_FROM_GIT_ITEM: By = By.xpath('//*[@data-test="item import-from-git"]');
	private static readonly SELECT_PROJECT_DROPDOWN: By = By.xpath('//div[@class="co-namespace-dropdown"]//button');
	private static readonly PROJECT_FILTER_INPUT: By = By.xpath('//*[@data-test="dropdown-text-filter"]');
	private static readonly SKIP_TOUR_BUTTON: By = By.xpath('//*[text()="Skip tour"]');
	private static readonly APP_LAUNCHER_BUTTON: By = By.css(
		'nav[data-test-id="application-launcher"], button[data-test-id="application-launcher"], button[aria-label="Application launcher"]'
	);
	private static readonly DEVSPACES_MENU_ITEM: By = By.xpath('//span[contains(.,"Red Hat OpenShift Dev Spaces")]');

	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper,
		@inject(CLASSES.BrowserTabsUtil)
		private readonly browserTabsUtil: BrowserTabsUtil
	) {}

	async waitOpenMainPage(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitVisibility(OcpMainPage.MAIN_PAGE_HEADER, TIMEOUT_CONSTANTS.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
	}

	async clickOnSelectRoleButton(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(OcpMainPage.SELECT_ROLE_BUTTON);
	}

	async clickAddToProjectButton(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(OcpMainPage.ADD_BUTTON, TIMEOUT_CONSTANTS.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
	}

	async selectDeveloperRole(): Promise<void> {
		Logger.debug();

		await this.waitOpenMainPage();
		await this.tryToSkipWebTour();
		await this.clickOnSelectRoleButton();
		await this.selectRole('Developer');
		await this.tryToSkipWebTour();
	}

	async selectImportFromGitMethod(): Promise<OcpImportFromGitPage> {
		Logger.debug();

		await this.driverHelper.waitAndClick(OcpMainPage.IMPORT_FROM_GIT_ITEM, TIMEOUT_CONSTANTS.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
		return e2eContainer.get(CLASSES.OcpImportFromGitPage);
	}

	async openImportFromGitPage(): Promise<OcpImportFromGitPage> {
		Logger.debug();

		await this.clickAddToProjectButton();
		return await this.selectImportFromGitMethod();
	}

	async selectProject(projectName: string): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(OcpMainPage.SELECT_PROJECT_DROPDOWN);
		await this.driverHelper.enterValue(OcpMainPage.PROJECT_FILTER_INPUT, projectName);
		await this.driverHelper.waitAndClick(this.getProjectDropdownItemLocator(projectName));
	}

	async clickOnAppLauncherAndDevSpaceItem(): Promise<void> {
		Logger.debug('click on app launcher menu');
		const parentGUID: string = await this.browserTabsUtil.getCurrentWindowHandle();
		await this.driverHelper.waitAndClick(OcpMainPage.APP_LAUNCHER_BUTTON);
		await this.driverHelper.waitAndClick(OcpMainPage.DEVSPACES_MENU_ITEM);
		await this.browserTabsUtil.waitAndSwitchToAnotherWindow(parentGUID, TIMEOUT_CONSTANTS.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
	}

	private getRoleLocator(role: string): By {
		if (BASE_TEST_CONSTANTS.OCP_VERSION && satisfies(BASE_TEST_CONSTANTS.OCP_VERSION, '>=4.18')) {
			return By.xpath(
				`//button[@role='option'][.//h2[@data-test-id='perspective-switcher-menu-option' and contains(text(), '${role}')]]`
			);
		}
		return By.xpath(`//a//*[text()="${role}"]`);
	}

	private getProjectDropdownItemLocator(projectName: string): By {
		return By.xpath(`//button//*[text()="${projectName}"]`);
	}

	private async selectRole(role: string): Promise<void> {
		Logger.debug(`selecting role ${role}`);

		await this.driverHelper.waitAndClick(this.getRoleLocator(role));
	}
	private async tryToSkipWebTour(): Promise<void> {
		Logger.debug();

		if (await this.driverHelper.isVisible(OcpMainPage.SKIP_TOUR_BUTTON)) {
			await this.driverHelper.waitAndClick(OcpMainPage.SKIP_TOUR_BUTTON);

			Logger.debug('welcome tour modal dialog was located and skipped');
		} else {
			Logger.debug('welcome tour modal dialog was not located');
		}
	}
}
