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
import { CLASSES } from '../../../configs/inversify.types';
import { Logger } from '../../../utils/Logger';
import { ICheLoginPage } from '../interfaces/ICheLoginPage';
import { DexLoginPage } from './DexLoginPage';
import { OAUTH_CONSTANTS } from '../../../constants/OAUTH_CONSTANTS';

@injectable()
export class KubernetesLoginPage implements ICheLoginPage {
	constructor(
		@inject(CLASSES.DexLoginPage)
		private readonly dexLoginPage: DexLoginPage
	) {}

	async login(): Promise<void> {
		Logger.debug();

		await this.dexLoginPage.waitDexLoginPage();
		await this.dexLoginPage.enterUserNameKubernetes(OAUTH_CONSTANTS.TS_SELENIUM_K8S_USERNAME);
		await this.dexLoginPage.enterPasswordKubernetes(OAUTH_CONSTANTS.TS_SELENIUM_K8S_PASSWORD);
		await this.dexLoginPage.clickOnLoginButton();
		await this.dexLoginPage.waitDexLoginPageDisappearance();
	}
}
