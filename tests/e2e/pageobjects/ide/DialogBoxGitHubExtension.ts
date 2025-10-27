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
import { ModalDialog } from 'monaco-page-objects';
import { expect } from 'chai';
import { CheCodeLocatorLoader } from './CheCodeLocatorLoader';

@injectable()
export class DialogBoxGitHubExtension {
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
	async isDialogBoxGitHubExtensionVisible(): Promise<boolean> {
		Logger.debug();

		const dialogBoxGitHubExtension: boolean = await this.driverHelper.waitVisibilityBoolean(
			this.cheCodeLocatorLoader.webCheCodeLocators.Dialog.constructor
		);

		await this.waitDialogBoxGitHubExtensionMessage();
		return dialogBoxGitHubExtension;
	}

	/**
	 * wait for the dialog box with the message about the GitHub extension to be visible.
	 */
	async waitDialogBoxGitHubExtensionMessage(): Promise<void> {
		// prettier-ignore
		const dialogMessage: string = 'The extension \'GitHub\' wants to sign in using GitHub.';
		Logger.debug(`dialogMessage: "${dialogMessage}"`);

		const modalDialog: ModalDialog = new ModalDialog();
		const messageDetails: string = await modalDialog.getDetails();
		Logger.debug(`modalDialog.getDetails: "${messageDetails}"`);
		expect(messageDetails).to.contain(dialogMessage);
	}

	/**
	 * close the dialog box with the message about the GitHub extension.
	 */
	async closeDialogBoxGitHubExtension(): Promise<void> {
		Logger.debug();

		const modalDialog: ModalDialog = new ModalDialog();
		await modalDialog.close();
		await this.driverHelper.waitDisappearance(this.cheCodeLocatorLoader.webCheCodeLocators.Dialog.constructor);
		Logger.debug('Dialog box with the message about the GitHub extension closed');
	}
}
