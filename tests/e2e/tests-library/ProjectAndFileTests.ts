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
import { DriverHelper } from '../utils/DriverHelper';
import { CLASSES } from '../configs/inversify.types';
import { Logger } from '../utils/Logger';
import { TIMEOUT_CONSTANTS } from '../constants/TIMEOUT_CONSTANTS';
import { CheCodeLocatorLoader } from '../pageobjects/ide/CheCodeLocatorLoader';
import { By, SideBarView, ViewContent, ViewItem, ViewSection, Workbench } from 'monaco-page-objects';
import { WorkspaceHandlingTests } from '../tests-library/WorkspaceHandlingTests';
import { RestrictedModeButton } from '../pageobjects/ide/RestrictedModeButton';

@injectable()
export class ProjectAndFileTests {
	private static BRANCH_NAME_XPATH: By = By.xpath('//a[contains(@aria-label,"Checkout Branch/Tag...")]');
	private static TRUST_PUBLISHER_BOX: By = By.xpath('//*[@class="dialog-message-text" and contains(text(), "publisher")]');

	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper,
		@inject(CLASSES.CheCodeLocatorLoader)
		private readonly cheCodeLocatorLoader: CheCodeLocatorLoader,
		@inject(CLASSES.WorkspaceHandlingTests)
		private readonly workspaceHandlingTests: WorkspaceHandlingTests,
		@inject(CLASSES.RestrictedModeButton)
		private readonly restrictedModeButton: RestrictedModeButton
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

			// assume that start workspace page is still opened
			await this.workspaceHandlingTests.logStartWorkspaceInfo();

			throw err;
		}
	}

	/**
	 * perform to 'trust author of the files' dialog box, when it appears
	 * manage to 'Trusted' Workspace Mode, when the 'trust author of the files' dialog does not appear
	 */
	async performTrustAuthorDialog(): Promise<void> {
		Logger.debug();
		// sometimes the trust dialog does not appear at first time, for avoiding this problem we send click event for activating
		const workbench: Workbench = new Workbench();

		try {
			await workbench.click();
			// add TS_IDE_LOAD_TIMEOUT timeout for waiting for reloading the IDE
			await this.driverHelper.wait(TIMEOUT_CONSTANTS.TS_IDE_LOAD_TIMEOUT);
			await this.driverHelper.waitVisibility(
				this.cheCodeLocatorLoader.webCheCodeLocators.WelcomeContent.text,
				TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM
			);
			await this.driverHelper.waitAndClick(
				this.cheCodeLocatorLoader.webCheCodeLocators.WelcomeContent.button,
				TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM
			);
			await this.restrictedModeButton.isRestrictedModeButtonDisappearance();
		} catch (e) {
			Logger.info(
				'"Do you trust authors of the files in this workspace?" dialog box was not shown, or Restricted Mode is still active'
			);

			try {
				await this.restrictedModeButton.clickOnRestrictedModeButton();
				await this.driverHelper.waitAndClick(
					(this.cheCodeLocatorLoader.webCheCodeLocators.Workbench as any).workspaceTrustButton,
					TIMEOUT_CONSTANTS.TS_DIALOG_WINDOW_DEFAULT_TIMEOUT
				);
			} catch (e) {
				Logger.info('Restricted Mode button or Trusted Workspace box was not shown');
			}
		}
	}

	/**
	 * perform to 'Trust Publisher' dialog box, when it appears
	 */
	async performTrustPublisherDialog(): Promise<void> {
		Logger.debug();

		try {
			// add TS_IDE_LOAD_TIMEOUT timeout for waiting for appearing the box
			await this.driverHelper.wait(TIMEOUT_CONSTANTS.TS_IDE_LOAD_TIMEOUT);
			await this.driverHelper.waitVisibility(
				ProjectAndFileTests.TRUST_PUBLISHER_BOX,
				TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM
			);
			await this.driverHelper.waitAndClick(
				this.cheCodeLocatorLoader.webCheCodeLocators.WelcomeContent.button,
				TIMEOUT_CONSTANTS.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM
			);
		} catch (e) {
			Logger.info('"Do you trust the publisher?" dialog box was not shown');
		}
	}

	/**
	 * perform to 'Trust' dialog boxes, when they appear
	 */
	async performTrustDialogs(): Promise<void> {
		Logger.debug();
		await this.performTrustAuthorDialog();
		await this.performTrustPublisherDialog();
		await this.performTrustAuthorDialog();
	}

	/**
	 * find an ViewSection with project tree.
	 * @returns Promise resolving to ViewSection object
	 */
	async getProjectViewSession(): Promise<ViewSection> {
		Logger.debug();

		await this.driverHelper.waitVisibility(
			this.cheCodeLocatorLoader.webCheCodeLocators.DefaultTreeSection.itemRow,
			TIMEOUT_CONSTANTS.TS_EXPAND_PROJECT_TREE_ITEM_TIMEOUT
		);

		const viewContent: ViewContent = new SideBarView().getContent();
		const [projectSection]: ViewSection[] = await viewContent.getSections();
		return projectSection;
	}

	/**
	 * find an item in this view section by label. Does not perform recursive search through the whole tree.
	 * Does however scroll through all the expanded content. Will find items beyond the current scroll range.
	 * @param projectSection ViewSection with project tree files.
	 * @param label Label of the item to search for.
	 * @param itemLevel Shows how deep the algorithm should look into expanded folders to find item,
	 * default - 2 means first level is project directory and files inside it is the second level, unlimited 0
	 * @returns Promise resolving to ViewItem object is such item exists, undefined otherwise
	 */
	async getProjectTreeItem(projectSection: ViewSection, label: string, itemLevel: number = 2): Promise<ViewItem | undefined> {
		Logger.debug(`${label}`);

		let projectTreeItem: ViewItem | undefined;
		await this.driverHelper.waitVisibility(
			this.cheCodeLocatorLoader.webCheCodeLocators.ScmView.itemLevel(itemLevel),
			TIMEOUT_CONSTANTS.TS_EXPAND_PROJECT_TREE_ITEM_TIMEOUT
		);

		try {
			projectTreeItem = await projectSection.findItem(label, itemLevel);
			if (!projectTreeItem) {
				try {
					await projectSection.collapse();
					projectTreeItem = await projectSection.findItem(label, itemLevel);
				} catch (e) {
					Logger.warn(JSON.stringify(e));
				}
			}
		} catch (e) {
			Logger.warn(JSON.stringify(e));
		}

		return projectTreeItem;
	}

	/**
	 * @returns {string} Branch name of cloned repository
	 */

	async getBranchName(): Promise<string> {
		Logger.debug();

		await this.driverHelper.waitVisibility(ProjectAndFileTests.BRANCH_NAME_XPATH, TIMEOUT_CONSTANTS.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
		const output: string = await this.driverHelper.waitAndGetText(ProjectAndFileTests.BRANCH_NAME_XPATH);

		return output.trimStart();
	}
}
