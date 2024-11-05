/** *******************************************************************
 * copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import {
	ActivityBar,
	EditorView,
	ExtensionsViewItem,
	ExtensionsViewSection,
	Locators,
	SideBarView,
	TextEditor,
	ViewSection
} from 'monaco-page-objects';
import { registerRunningWorkspace } from '../MochaHooks';
import { LoginTests } from '../../tests-library/LoginTests';
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES, TYPES } from '../../configs/inversify.types';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { Logger } from '../../utils/Logger';
import { DriverHelper } from '../../utils/DriverHelper';
import { CheCodeLocatorLoader } from '../../pageobjects/ide/CheCodeLocatorLoader';
import { expect } from 'chai';
import { TIMEOUT_CONSTANTS } from '../../constants/TIMEOUT_CONSTANTS';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { PLUGIN_TEST_CONSTANTS } from '../../constants/PLUGIN_TEST_CONSTANTS';
import { BASE_TEST_CONSTANTS } from '../../constants/BASE_TEST_CONSTANTS';
import { ITestWorkspaceUtil } from '../../utils/workspace/ITestWorkspaceUtil';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';

const samples: string[] = PLUGIN_TEST_CONSTANTS.TS_SAMPLE_LIST.split(',');

// get visible items from Extension view, transform this from array to sorted string and compares it with existed recommended extensions
async function getVisibleFilteredItemsAndCompareWithRecommended(recommendations: string[]): Promise<boolean> {
	const extensionsView: SideBarView | undefined = await (await new ActivityBar().getViewControl('Extensions'))?.openView();
	const [marketplaceSection]: ExtensionsViewSection[] = (await extensionsView?.getContent().getSections()) as ExtensionsViewSection[];
	const sections: ViewSection[] | undefined = await extensionsView?.getContent().getSections();

	// if we have a big recommender extension list it can be overlapped by other recommendations panel,
	// in this case we need to collapse it for instance: it is actual for Quarkus example
	if (sections !== undefined) {
		for (let i: number = 0; i < sections.length; i++) {
			const currentSection: ViewSection = sections[i];
			const isOtherRecommendedSectionPresent: boolean = (await currentSection.getTitle()) === 'Other Recommendations';
			const isOtherRecommendationExpanded: boolean = await sections[i].isExpanded();
			if (isOtherRecommendedSectionPresent && isOtherRecommendationExpanded) {
				await currentSection.collapse();
			}
		}
	}
	Logger.debug('marketplaceSection.getVisibleItems()');
	const allFoundRecommendedItems: ExtensionsViewItem[] = await marketplaceSection.getVisibleItems();
	const allFoundRecommendedAuthors: string[] = await Promise.all(
		allFoundRecommendedItems.map(async (item: ExtensionsViewItem): Promise<string> => await item.getAuthor())
	);

	const allFoundAuthorsAsSortedString: string = allFoundRecommendedAuthors.sort().toString();
	const allPublisherNamesAsSorString: string = recommendations.sort().toString();
	return allFoundAuthorsAsSortedString === allPublisherNamesAsSorString;
}
// get visible items from Extension view, transform this from array to sorted string and compares it with existed installed extensions
async function getVisibleFilteredItemsAndCompareWithInstalled(recommendations: string[]): Promise<boolean> {
	const extensionsView: SideBarView | undefined = await (await new ActivityBar().getViewControl('Extensions'))?.openView();
	const [marketplaceSection]: ExtensionsViewSection[] = (await extensionsView?.getContent().getSections()) as ExtensionsViewSection[];
	Logger.debug('marketplaceSection.getVisibleItems()');
	const allFoundRecommendedItems: ExtensionsViewItem[] = await marketplaceSection.getVisibleItems();
	const allFoundRecommendedAuthors: string[] = await Promise.all(
		allFoundRecommendedItems.map(async (item: ExtensionsViewItem): Promise<string> => await item.getAuthor())
	);

	const allFoundAuthorsAsSortedString: string = allFoundRecommendedAuthors.sort().toString();
	const allPublisherNamesAsSortString: string = recommendations.sort().toString();
	// in some cases we can have installed not only recommended extensions with some samples (for example .Net)
	return allFoundAuthorsAsSortedString.includes(allPublisherNamesAsSortString);
}
for (const sample of samples) {
	suite(`Check if recommended extensions installed for ${sample} ${BASE_TEST_CONSTANTS.TEST_ENVIRONMENT}`, function (): void {
		const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
		const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
		const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
		const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
		const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
		const cheCodeLocatorLoader: CheCodeLocatorLoader = e2eContainer.get(CLASSES.CheCodeLocatorLoader);
		const webCheCodeLocators: Locators = cheCodeLocatorLoader.webCheCodeLocators;
		const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);
		const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
		let projectSection: ViewSection;
		let extensionSection: ExtensionsViewSection;
		let extensionsView: SideBarView | undefined;
		let publisherNames: string[];

		const [pathToExtensionsListFileName, extensionsListFileName]: string[] = ['.vscode', 'extensions.json'];

		let vsCodeFolderItemLevel: number = 2;

		let recommendedExtensions: any = {
			recommendations: []
		};

		let parsedRecommendations: Array<{ name: string; publisher: string }>;
		suiteSetup('Login', async function (): Promise<void> {
			await loginTests.loginIntoChe();
		});

		test(`Create and open new workspace, stack:${sample}`, async function (): Promise<void> {
			await workspaceHandlingTests.createAndOpenWorkspace(sample);
		});
		test('Obtain workspace name from workspace loader page', async function (): Promise<void> {
			await workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
			expect(WorkspaceHandlingTests.getWorkspaceName(), 'Workspace name was not fetched from the loading page').not.undefined;
		});

		test('Registering the running workspace', function (): void {
			registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
		});

		test('Wait workspace readiness', async function (): Promise<void> {
			await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
		});

		test('Accept the project as a trusted one', async function (): Promise<void> {
			await projectAndFileTests.performTrustAuthorDialog();
		});

		test('Check the project files were imported', async function (): Promise<void> {
			// add TS_IDE_LOAD_TIMEOUT timeout for waiting for finishing animation of all IDE parts (Welcome parts. bottom widgets. etc.)
			// using TS_IDE_LOAD_TIMEOUT easier than performing of finishing animation  all elements
			await driverHelper.wait(TIMEOUT_CONSTANTS.TS_IDE_LOAD_TIMEOUT);
			projectSection = await projectAndFileTests.getProjectViewSession();
			try {
				// try using project level 2, as in samples from https://github.com/devspaces-samples/
				expect(
					await projectAndFileTests.getProjectTreeItem(projectSection, pathToExtensionsListFileName, vsCodeFolderItemLevel),
					'Files not imported'
				).not.undefined;
			} catch (err) {
				Logger.debug(
					'Try using project level 1, as in samples with a defined metadata.projectType in the devfile.yaml, such as JBoss EAP.'
				);
				vsCodeFolderItemLevel = vsCodeFolderItemLevel - 1;
				expect(
					await projectAndFileTests.getProjectTreeItem(projectSection, pathToExtensionsListFileName, vsCodeFolderItemLevel),
					'Files not imported'
				).not.undefined;
			}
		});

		test(`Get recommended extensions list from ${extensionsListFileName}`, async function (): Promise<void> {
			// sometimes the Trust Dialog does not appear as expected - as result we need to execute "projectAndFileTests.performManageWorkspaceTrustBox()" method. In this case.
			try {
				await (
					await projectAndFileTests.getProjectTreeItem(projectSection, pathToExtensionsListFileName, vsCodeFolderItemLevel)
				)?.select();
			} catch (err) {
				await projectAndFileTests.performManageWorkspaceTrustBox();
				await (
					await projectAndFileTests.getProjectTreeItem(projectSection, pathToExtensionsListFileName, vsCodeFolderItemLevel)
				)?.select();
			}
			await (
				await projectAndFileTests.getProjectTreeItem(projectSection, extensionsListFileName, vsCodeFolderItemLevel + 1)
			)?.select();
			Logger.debug(`EditorView().openEditor(${extensionsListFileName})`);
			const editor: TextEditor = (await new EditorView().openEditor(extensionsListFileName)) as TextEditor;
			await driverHelper.waitVisibility(webCheCodeLocators.Editor.inputArea);
			Logger.debug('editor.getText(): get recommended extensions as text from editor, delete comments and parse to object.');
			recommendedExtensions = JSON.parse((await editor.getText()).replace(/\/\*[\s\S]*?\*\/|(?<=[^:])\/\/.*|^\/\/.*/g, '').trim());
			Logger.debug('recommendedExtensions.recommendations: Get recommendations clear names using map().');
			parsedRecommendations = recommendedExtensions.recommendations.map((rec: string): { name: string; publisher: string } => {
				const [publisher, name] = rec.split('.');
				return { publisher, name };
			});
			Logger.debug(`Recommended extension for this workspace:\n${JSON.stringify(parsedRecommendations)}.`);

			publisherNames = parsedRecommendations.map((rec: { name: string; publisher: string }): string => rec.publisher);
			expect(parsedRecommendations, 'Recommendations not found').not.empty;
		});

		test('Open "Extensions" view section', async function (): Promise<void> {
			Logger.debug('ActivityBar().getViewControl("Extensions"))?.openView(): open Extensions view.');
			// sometimes the Trust Dialog does not appear as expected - as result we need to execute "projectAndFileTests.performManageWorkspaceTrustBox()" method. In this case.
			try {
				extensionsView = await (await new ActivityBar().getViewControl('Extensions'))?.openView();
			} catch (err) {
				await projectAndFileTests.performManageWorkspaceTrustBox();
				extensionsView = await (await new ActivityBar().getViewControl('Extensions'))?.openView();
			}
			expect(extensionsView, 'Can`t find Extension section').not.undefined;
		});

		test('Let extensions complete installation', async function (): Promise<void> {
			this.test?.retries(0);
			Logger.debug(
				`Time for extensions installation TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT=${TIMEOUT_CONSTANTS.TS_COMMON_PLUGIN_TEST_TIMEOUT}`
			);
			await driverHelper.wait(TIMEOUT_CONSTANTS.TS_COMMON_PLUGIN_TEST_TIMEOUT);
		});

		test('Check if extensions are installed and enabled', async function (): Promise<void> {
			// timeout 15 seconds per extensions
			this.timeout(TIMEOUT_CONSTANTS.TS_FIND_EXTENSION_TEST_TIMEOUT * parsedRecommendations.length);
			Logger.debug('ActivityBar().getViewControl("Extensions"))?.openView(): open Extensions view.');
			extensionsView = await (await new ActivityBar().getViewControl('Extensions'))?.openView();

			Logger.debug('extensionsView?.getContent().getSections(): get current section.');
			[extensionSection] = (await extensionsView?.getContent().getSections()) as ExtensionsViewSection[];
			expect(extensionSection, 'Can`t find Extension section').not.undefined;
			await driverHelper.waitVisibility(
				webCheCodeLocators.ExtensionsViewSection.itemTitle,
				TIMEOUT_CONSTANTS.TS_EDITOR_TAB_INTERACTION_TIMEOUT
			);

			Logger.debug('extensionSection.findItem by @recommended filter');
			try {
				await extensionSection.findItem('@recommended');
			} catch (err) {
				await driverHelper.wait(TIMEOUT_CONSTANTS.TS_EXPAND_PROJECT_TREE_ITEM_TIMEOUT);
				await extensionSection.findItem('@recommended');
			}
			const isReloadRequired: boolean = await driverHelper.isVisible(
				(webCheCodeLocators.ExtensionsViewSection as any).requireReloadButton
			);
			Logger.debug(`Is extensions require reload the editor: ${isReloadRequired}`);

			if (isReloadRequired) {
				Logger.debug('Refreshing the page..');
				await browserTabsUtil.refreshPage();
				await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
				await driverHelper.waitVisibility(
					webCheCodeLocators.ActivityBar.viewContainer,
					TIMEOUT_CONSTANTS.TS_EDITOR_TAB_INTERACTION_TIMEOUT
				);
				Logger.debug('ActivityBar().getViewControl("Extensions"))?.openView(): reopen Extensions view.');
				extensionsView = await (await new ActivityBar().getViewControl('Extensions'))?.openView();
				await driverHelper.waitVisibility(
					webCheCodeLocators.ExtensionsViewSection.itemTitle,
					TIMEOUT_CONSTANTS.TS_EDITOR_TAB_INTERACTION_TIMEOUT
				);
				expect(extensionsView, 'Can`t find Extension View section').not.undefined;
				[extensionSection] = (await extensionsView?.getContent().getSections()) as ExtensionsViewSection[];
				expect(extensionSection, 'Can`t find Extension section').not.undefined;
				await extensionSection.findItem('@recommended ');
			}

			Logger.debug('extensionSection.findItem by @recommended filter');
			expect(await getVisibleFilteredItemsAndCompareWithRecommended(publisherNames)).to.be.true;
			Logger.debug(`All recommended extensions were found by  @recommended filter: ---- ${publisherNames} ----`);

			Logger.debug('extensionSection.findItem by @installed filter');
			try {
				await extensionSection.findItem('@installed ');
			} catch (err) {
				await driverHelper.wait(TIMEOUT_CONSTANTS.TS_EXPAND_PROJECT_TREE_ITEM_TIMEOUT);
				await extensionSection.findItem('@installed ');
			}
			expect(await getVisibleFilteredItemsAndCompareWithInstalled(publisherNames)).to.be.true;
			Logger.debug(`All recommended extensions were found by  @installed filter: ---- ${publisherNames} ----`);
		});

		suiteTeardown('Open dashboard and close all other tabs', async function (): Promise<void> {
			await dashboard.openDashboard();
			await browserTabsUtil.closeAllTabsExceptCurrent();
		});

		suiteTeardown('Stop and delete the workspace by API', async function (): Promise<void> {
			await testWorkspaceUtil.stopAndDeleteWorkspaceByName(WorkspaceHandlingTests.getWorkspaceName());
		});

		suiteTeardown('Unregister running workspace', function (): void {
			registerRunningWorkspace('');
		});
	});
}
