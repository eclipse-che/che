/** *******************************************************************
 * copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { CLASSES } from '../../configs/inversify.types';
import { e2eContainer } from '../../configs/inversify.config';
import { LoginTests } from '../../tests-library/LoginTests';
import { OcpMainPage } from '../../pageobjects/openshift/OcpMainPage';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';

suite(`Login to Openshift console and start WebTerminal ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const ocpMainPage: OcpMainPage = e2eContainer.get(CLASSES.OcpMainPage);

	loginTests.loginIntoOcpConsole();

	test('Open Web Terminal', async function (): Promise<void> {
		await ocpMainPage.waitOpenMainPage();
		await ocpMainPage.openWebTerminal();
	});
});
