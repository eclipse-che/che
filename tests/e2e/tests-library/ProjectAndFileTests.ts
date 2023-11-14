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
import { SideBarView, SingleScmProvider, ViewContent, ViewItem, ViewSection, Workbench } from 'monaco-page-objects';

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
		const workbench: Workbench = new Workbench();

		try {
			await workbench.click();
			await this.driverHelper.waitAndClick(
				this.cheCodeLocatorLoader.webCheCodeLocators.WelcomeContent.button,
				TIMEOUT_CONSTANTS.TS_DIALOG_WINDOW_DEFAULT_TIMEOUT
			);
		} catch (e) {
			Logger.info('Second welcome content dialog box was not shown');
		}
	}

	async manageWorkspaceTrust(scmProvider: SingleScmProvider): Promise<void> {
		Logger.debug();
		if (scmProvider === undefined) {
			try {
				await this.driverHelper.waitAndClick(
					(this.cheCodeLocatorLoader.webCheCodeLocators.ScmView as any).manageWorkspaceTrust,
					TIMEOUT_CONSTANTS.TS_DIALOG_WINDOW_DEFAULT_TIMEOUT
				);
				await this.driverHelper.waitAndClick(
					(this.cheCodeLocatorLoader.webCheCodeLocators.Workbench as any).workspaceTrustButton,
					TIMEOUT_CONSTANTS.TS_DIALOG_WINDOW_DEFAULT_TIMEOUT
				);
				await this.driverHelper.waitAndClick(
					(this.cheCodeLocatorLoader.webCheCodeLocators.ScmView as any).modifiedFile,
					TIMEOUT_CONSTANTS.TS_DIALOG_WINDOW_DEFAULT_TIMEOUT
				);
			} catch (err) {
				Logger.error(`Workspace trust dialog box was not shown: ${err}`);
				throw err;
			}
		}
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
}
