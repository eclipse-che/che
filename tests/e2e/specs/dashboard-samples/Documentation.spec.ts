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
import { Logger } from '../../utils/Logger';

// suit works for DevSpaces
suite(`Check links to documentation page in Dashboard ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
	this.timeout(180000);
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
	const majorMinorVersion: string = BASE_TEST_CONSTANTS.TESTING_APPLICATION_VERSION.split('.').slice(0, 2).join('.'); // extract major.minor version from full version
	let parentGUID: string = '';
	let docs: any, links: any, productVersion: any;
	let webSocketTroubleshooting: any, workspace: any, devfile: any, general: any, storageTypes: any;

	suiteSetup('Login into OC client and apply default DevFile', function (): void {
		kubernetesCommandLineToolsExecutor.loginToOcp();
		kubernetesCommandLineToolsExecutor.applyYamlConfigurationAsFile(pathToSampleFile);
		shellExecutor.wait(5);
	});

	suiteSetup('Get links from product version github branch', function (): void {
		try {
			({ docs, links, productVersion } = JSON.parse(
				shellExecutor.executeCommand(
					'oc exec deploy/devspaces-dashboard -n openshift-devspaces -- cat /public/dashboard/assets/branding/product.json'
				)
			));
		} catch (e) {
			Logger.error('Cannot fetch documentation links');
			throw e;
		}
		({ webSocketTroubleshooting, workspace, devfile, general, storageTypes } = docs);
	});

	suiteSetup('Login', async function (): Promise<void> {
		await loginTests.loginIntoChe();
	});

	test('Check if documentation section "About" present on Dashboard', async function (): Promise<void> {
		await dashboard.openAboutMenu();
	});

	test('Check if documentation section "About" in top menu has required links', async function (): Promise<void> {
		parentGUID = await browserTabsUtil.getCurrentWindowHandle();
		for (const link of links) {
			await dashboard.selectAboutMenuItem(link.text);
			await browserTabsUtil.waitAndSwitchToAnotherWindow(parentGUID);
			const currentUrl: string = await browserTabsUtil.getCurrentUrl();

			Logger.trace(`Current URL: ${currentUrl}`);

			expect(link.href, `${link.href} not includes ${currentUrl}`).oneOf([currentUrl, currentUrl + '/']);
			await browserTabsUtil.switchToWindow(parentGUID);
			await browserTabsUtil.closeAllTabsExceptCurrent();
		}
	});

	test('Check if "About" dialog menu contains correct application version and username', async function (): Promise<void> {
		await dashboard.selectAboutMenuItem('About');
		await dashboard.waitAboutDialogWindowMenuElements();
		expect(await dashboard.getApplicationVersionFromAboutDialogWindow(), 'Wrong product version').eqls(productVersion);
		expect(await dashboard.getUsernameFromAboutDialogWindow(), 'Wrong username').eqls(OAUTH_CONSTANTS.TS_SELENIUM_OCP_USERNAME);
		await dashboard.closeAboutDialogWindow();
	});

	test('Check if Workspaces page contains "Learn More" documentation link', async function (): Promise<void> {
		await dashboard.clickWorkspacesButton();
		await workspaces.waitPage();
		expect(await workspaces.getLearnMoreDocumentationLink(), '"Learn More" doc link is broken').eqls(workspace);
	});

	test('Check if Workspace Details page contains "Storage types" documentation link', async function (): Promise<void> {
		await workspaces.clickWorkspaceListItemLink(workspaceName);
		await workspaceDetails.waitWorkspaceTitle(workspaceName);
		await workspaceDetails.clickStorageTypeInfo();
		expect(await workspaceDetails.getOpenStorageTypeDocumentationLink(), '"Storage types" doc link is broken').eqls(storageTypes);
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
					expect(responseData.status, `Wrong status code ${responseData.status}`).eqls(200);
				}
			}
		});
	}

	suiteTeardown('Open dashboard and close all other tabs', async function (): Promise<void> {
		await dashboard.openDashboard();
		await browserTabsUtil.closeAllTabsExceptCurrent();
	});

	suiteTeardown('Delete default DevWorkspace', function (): void {
		kubernetesCommandLineToolsExecutor.deleteDevWorkspace();
	});
});
