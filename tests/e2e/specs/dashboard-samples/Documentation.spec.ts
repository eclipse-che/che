/** *******************************************************************
 * copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import fs from 'fs';
import path from 'path';
import YAML from 'yaml';
import { expect } from 'chai';
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES } from '../../configs/inversify.types';
import { LoginTests } from '../../tests-library/LoginTests';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { Workspaces } from '../../pageobjects/dashboard/Workspaces';
import { ShellExecutor } from '../../utils/ShellExecutor';
import { WorkspaceDetails } from '../../pageobjects/dashboard/workspace-details/WorkspaceDetails';
import axios from 'axios';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { OAUTH_CONSTANTS } from '../../constants/OAUTH_CONSTANTS';

// suit works for DevSpaces
suite('Check links to documentation page in Dashboard.', function (): void {
	const pathToSampleFile: string = path.resolve('resources/default-devfile.yaml');
	const workspaceName: string = YAML.parse(fs.readFileSync(pathToSampleFile, 'utf8')).metadata.name;
	const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(
		CLASSES.KubernetesCommandLineToolsExecutor
	);
	kubernetesCommandLineToolsExecutor.workspaceName = workspaceName;
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const workspaces: Workspaces = e2eContainer.get(CLASSES.Workspaces);
	const workspaceDetails: WorkspaceDetails = e2eContainer.get(CLASSES.WorkspaceDetails);
	const shellExecutor: ShellExecutor = e2eContainer.get(CLASSES.ShellExecutor);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);

	const testingVersion: string = BASE_TEST_CONSTANTS.TESTING_APPLICATION_VERSION;
	let parentGUID: string = '';

	// get links from product version github branch
	const { docs, links, productVersion }: any = JSON.parse(
		shellExecutor.curl(
			`https://raw.githubusercontent.com/redhat-developer/devspaces-images/devspaces-${testingVersion}-rhel-8/devspaces-dashboard/packages/dashboard-frontend/assets/branding/product.json`
		)
	);
	const { webSocketTroubleshooting, workspace, devfile, general, storageTypes } = docs;

	suiteSetup('Login into OC client and apply default DevFile', function (): void {
		kubernetesCommandLineToolsExecutor.loginToOcp();
		kubernetesCommandLineToolsExecutor.applyYamlConfigurationAsFile(pathToSampleFile);
		shellExecutor.wait(5);
	});

	suiteTeardown('Delete default DevWorkspace', function (): void {
		kubernetesCommandLineToolsExecutor.deleteDevWorkspace();
	});

	test('Check if product.json config contains correct application version', function (): void {
		[productVersion, links[1].href, devfile, workspace, general, storageTypes, webSocketTroubleshooting].forEach((e): void => {
			expect(e).contains(testingVersion);
		});
	});

	loginTests.loginIntoChe();

	test('Check if documentation section "About" present on Dashboard', async function (): Promise<void> {
		await dashboard.openAboutMenu();
	});

	test('Check if documentation section "About" in top menu has required links', async function (): Promise<void> {
		parentGUID = await browserTabsUtil.getCurrentWindowHandle();
		for (const link of links) {
			await dashboard.selectAboutMenuItem(link.text);
			await browserTabsUtil.waitAndSwitchToAnotherWindow(parentGUID);
			const currentUrl: string = await browserTabsUtil.getCurrentUrl();
			expect(link.href).oneOf([currentUrl, currentUrl + '/']);
			await browserTabsUtil.switchToWindow(parentGUID);
			await browserTabsUtil.closeAllTabsExceptCurrent();
		}
	});

	test('Check if "About" dialog menu contains correct application version and username', async function (): Promise<void> {
		await dashboard.selectAboutMenuItem('About');
		await dashboard.waitAboutDialogWindowMenuElements();
		expect(await dashboard.getApplicationVersionFromAboutDialogWindow()).eqls(productVersion);
		expect(await dashboard.getUsernameFromAboutDialogWindow()).eqls(OAUTH_CONSTANTS.TS_SELENIUM_OCP_USERNAME);
		await dashboard.closeAboutDialogWindow();
	});

	test('Check if Workspaces page contains "Learn More" documentation link', async function (): Promise<void> {
		await dashboard.clickWorkspacesButton();
		await workspaces.waitPage();
		expect(await workspaces.getLearnMoreDocumentationLink()).eqls(workspace);
	});

	test('Check if Workspace Details page contains "Storage types" documentation link', async function (): Promise<void> {
		await workspaces.clickWorkspaceListItemLink(workspaceName);
		await workspaceDetails.waitWorkspaceTitle(workspaceName);
		await workspaceDetails.clickStorageTypeInfo();
		expect(await workspaceDetails.getOpenStorageTypeDocumentationLink()).eqls(storageTypes);
	});

	test('Check if Workspace Details page contains "Devfile" documentation link', async function (): Promise<void> {
		await workspaceDetails.closeStorageTypeInfo();
		await workspaceDetails.selectTab('Devfile');
		expect(await workspaceDetails.getDevfileDocumentationLink()).eqls(devfile);
	});

	if (BASE_TEST_CONSTANTS.IS_PRODUCT_DOCUMENTATION_RELEASED) {
		test('Check if product.json documentation links returns status code 200', async function (): Promise<void> {
			const documentationLinks: string[] = [devfile, workspace, general, storageTypes, webSocketTroubleshooting];
			links.forEach((link: any): void => {
				documentationLinks.push(link.href);
			});
			for (const e of documentationLinks) {
				let responseData: any;
				try {
					responseData = await axios.get(e);
				} catch (e) {
					responseData = e;
				} finally {
					expect(responseData.status).eqls(200);
				}
			}
		});
	}

	loginTests.loginIntoChe();
});
