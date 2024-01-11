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
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { UserPreferences } from '../../pageobjects/dashboard/UserPreferences';

suite(`"Check User Preferences page" test ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const userPreferences: UserPreferences = e2eContainer.get(CLASSES.UserPreferences);

	suiteSetup('Login', async function (): Promise<void> {
		await loginTests.loginIntoChe();
	});

	test('Check user preferences page', async function (): Promise<void> {
		await userPreferences.openUserPreferencesPage();
		await userPreferences.checkTabsAvailability();

		await userPreferences.openGitServicesTab();
		await userPreferences.revokeGitService('GitHub');
	});
});
