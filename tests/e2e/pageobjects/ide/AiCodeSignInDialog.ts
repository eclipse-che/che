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
import { By } from 'monaco-page-objects';

@injectable()
export class AiCodeSignInDialog {
	private static readonly OVERLAY_LOCATOR: By = By.css('div.onboarding-a-overlay.visible');
	private static readonly SKIP_BUTTON_LOCATOR: By = By.css('div.onboarding-a-overlay button.onboarding-a-btn-ghost');

	constructor(
		@inject(CLASSES.DriverHelper)
		private readonly driverHelper: DriverHelper
	) {}

	async isDialogVisible(): Promise<boolean> {
		Logger.debug();

		return await this.driverHelper.waitVisibilityBoolean(AiCodeSignInDialog.OVERLAY_LOCATOR);
	}

	async closeDialog(): Promise<void> {
		Logger.debug();

		await this.driverHelper.waitAndClick(AiCodeSignInDialog.SKIP_BUTTON_LOCATOR);
		await this.driverHelper.waitDisappearance(AiCodeSignInDialog.OVERLAY_LOCATOR);
		Logger.debug('AI-powered development sign in dialog skipped');
	}
}
