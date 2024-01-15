/** *******************************************************************
 * copyright (c) 2020-2024 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES } from '../../configs/inversify.types';
import { LoginTests } from '../../tests-library/LoginTests';
import { UserPreferences } from '../../pageobjects/dashboard/UserPreferences';
import { FACTORY_TEST_CONSTANTS } from '../../constants/FACTORY_TEST_CONSTANTS';

suite('"Revoke OAuth" test', function (): void {
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const userPreferences: UserPreferences = e2eContainer.get(CLASSES.UserPreferences);
	const gitService: string = FACTORY_TEST_CONSTANTS.TS_SELENIUM_FACTORY_GIT_PROVIDER || 'github';

	suiteSetup('Login', async function (): Promise<void> {
		await loginTests.loginIntoChe();
	});

	test('Revoke OAuth test', async function (): Promise<void> {
		await userPreferences.openUserPreferencesPage();
		await userPreferences.checkTabsAvailability();

		await userPreferences.openGitServicesTab();

		const selectedService: string = userPreferences.getServiceConfig(gitService);
		await userPreferences.revokeGitService(selectedService);
	});
});
