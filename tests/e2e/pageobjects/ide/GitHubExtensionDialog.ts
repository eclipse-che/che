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
import { By, ModalDialog } from 'monaco-page-objects';
import { CheCodeLocatorLoader } from './CheCodeLocatorLoader';

@injectable()
export class GitHubExtensionDialog {
	private static readonly DIALOG_LOCATOR: By = By.xpath(
		'//div[@class="dialog-message-detail" and contains(., "The extension \'GitHub\' wants to sign in using GitHub.")]'
	);

	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper,
		@inject(CLASSES.CheCodeLocatorLoader)
		private readonly cheCodeLocatorLoader: CheCodeLocatorLoader
	) {}

	/**
	 * check if the dialog box with the message about the GitHub extension is visible.
	 * @returns Promise resolving to boolean.
	 */
	async isDialogVisible(): Promise<boolean> {
		Logger.debug();

		return await this.driverHelper.waitVisibilityBoolean(GitHubExtensionDialog.DIALOG_LOCATOR);
	}

	/**
	 * close the dialog box with the message about the GitHub extension.
	 */
	async closeDialog(): Promise<void> {
		Logger.debug();

		const modalDialog: ModalDialog = new ModalDialog();
		await modalDialog.close();
		await this.driverHelper.waitDisappearance(this.cheCodeLocatorLoader.webCheCodeLocators.Dialog.constructor);
		Logger.debug('Dialog box with the message about the GitHub extension closed');
	}
}
