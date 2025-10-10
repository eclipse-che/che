/** *******************************************************************
 * copyright (c) 2019-2025 Red Hat, Inc.
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
	Key,
	Locators,
	SideBarView,
	until,
	ViewSection,
	WebElement
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
// get text from editor
async function getText(): Promise<string> {
	const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
	Logger.debug('Select and copy all text in the editor');
	await driverHelper.getDriver().actions().keyDown(Key.CONTROL).sendKeys('a').keyUp(Key.CONTROL).perform();
	await driverHelper.getDriver().actions().keyDown(Key.CONTROL).sendKeys('c').keyUp(Key.CONTROL).perform();

	Logger.debug('Read the text');
	Logger.info('Create a hidden buffer');
	await driverHelper.getDriver().executeScript(`
		let input = document.createElement('textarea');
		input.setAttribute('id', 'clipboard-buffer');
		document.body.appendChild(input);
		input.focus();
	`);
	Logger.info('Paste the text to the buffer');
	await driverHelper.getDriver().actions().keyDown(Key.CONTROL).sendKeys('v').keyUp(Key.CONTROL).perform();
	Logger.info('Get the text from the buffer');
	const text: string = await driverHelper.getDriver().executeScript(`
		let input = document.getElementById('clipboard-buffer');
		let text = input.value;
		input.remove();
		return text;
	`);
	console.log('Raw text:', text);
	return text;
}
// helper function to determine section based on category
function getSectionForCategory(title: string): string {
	const category: string = title.split(' ')[0].toLowerCase();
	switch (category) {
		case '@disabled':
			return 'Disabled';
		case '@enabled':
			return 'Enabled';
		case '@installed':
			return 'Installed';
		case '@outdated':
			return 'Outdated';
		case '@recommended':
			return 'WORKSPACE RECOMMENDATIONS';
		default:
			return 'Marketplace';
	}
}
// search for an extension by title
async function findItem(extSection: ExtensionsViewSection, title: string): Promise<ExtensionsViewItem | undefined> {
	const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
	Logger.debug(`Finding extension item with filter: "${title}"`);

	const enclosingItem: WebElement = extSection.getEnclosingElement();
	const progress: WebElement = await enclosingItem.findElement((extSection as any).constructor.locators.ViewContent.progress);
	const searchField: WebElement = await enclosingItem.findElement(
		(extSection as any).constructor.locators.ExtensionsViewSection.searchBox
	);

	Logger.debug('Clearing search field with Ctrl+A');
	await driverHelper.getDriver().actions().click(searchField).keyDown(Key.CONTROL).sendKeys('a').keyUp(Key.CONTROL).perform();
	await driverHelper.wait(500);
	Logger.debug('Deleting selected text');
	await driverHelper.getDriver().actions().sendKeys(Key.DELETE).perform();
	await driverHelper.wait(500);
	Logger.debug(`Entering search text: "${title}"`);
	await driverHelper.getDriver().actions().click(searchField).sendKeys(title).perform();

	try {
		Logger.debug('Waiting for progress indicator to appear');
		await driverHelper.getDriver().wait(until.elementIsVisible(progress), 3000);
	} catch (err: any) {
		if (err.name !== 'TimeoutError') {
			throw err;
		}
	}
	Logger.debug('Waiting for progress indicator to disappear');
	await driverHelper.getDriver().wait(until.elementIsNotVisible(progress));

	const sectionTitle: string = getSectionForCategory(title);
	Logger.debug(`Looking for section with title: "${sectionTitle}"`);

	const sections: WebElement[] = await enclosingItem.findElements((extSection as any).constructor.locators.ViewContent.section);
	Logger.debug(`Found ${sections.length} sections`);

	// debug: Log all available section titles
	const availableSections: string[] = [];
	for (const sec of sections) {
		const titleElement: WebElement = await sec.findElement((extSection as any).constructor.locators.ViewSection.title);
		const sectionTitleText: string = await titleElement.getText();
		availableSections.push(sectionTitleText);
	}
	Logger.debug(`Available sections: ${availableSections.join(', ')}`);

	let targetSection: WebElement | undefined;
	for (const sec of sections) {
		const titleElement: WebElement = await sec.findElement((extSection as any).constructor.locators.ViewSection.title);
		const sectionTitleText: string = await titleElement.getText();
		if (sectionTitleText === sectionTitle) {
			targetSection = sec;
			Logger.debug(`Found target section: "${sectionTitle}"`);
			break;
		}
	}

	if (!targetSection) {
		Logger.debug(`Section "${sectionTitle}" not found. Available sections: ${availableSections.join(', ')}`);
		return undefined;
	}

	const titleParts: string[] = title.split(' ');
	let searchTitle: string = title;
	if (titleParts[0].startsWith('@')) {
		searchTitle = titleParts.slice(1).join(' ');
	}
	Logger.debug(`Searching for extension with title: "${searchTitle}"`);

	const extensionRows: WebElement[] = await targetSection.findElements(
		(extSection as any).constructor.locators.ExtensionsViewSection.itemRow
	);
	Logger.debug(`Found ${extensionRows.length} extension rows in section`);

	for (const row of extensionRows) {
		const extension: ExtensionsViewItem = new ExtensionsViewItem(row, extSection as any);
		const extensionTitle: string = await extension.getTitle();
		if (extensionTitle === searchTitle) {
			Logger.debug(`Found matching extension: "${extensionTitle}"`);
			return extension;
		}
	}

	Logger.debug(`Extension with title "${searchTitle}" not found in section "${sectionTitle}"`);
	return undefined;
}

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

	// debug: Log all found recommended items
	try {
		const allFoundRecommendedItems: ExtensionsViewItem[] = await marketplaceSection.getVisibleItems();
		const itemTitles: string[] = await Promise.all(
			allFoundRecommendedItems.map(async (item: ExtensionsViewItem): Promise<string> => await item.getTitle())
		);

		const allFoundRecommendedAuthors: string[] = await Promise.all(
			allFoundRecommendedItems.map(async (item: ExtensionsViewItem): Promise<string> => await item.getAuthor())
		);

		Logger.debug(`Found ${allFoundRecommendedItems.length} recommended items: ${itemTitles.join(', ')}`);
		Logger.debug(`Found authors: ${allFoundRecommendedAuthors.join(', ')}`);
		Logger.debug(`Expected authors: ${recommendations.join(', ')}`);

		const allFoundAuthorsAsSortedString: string = allFoundRecommendedAuthors.sort().toString();
		const allPublisherNamesAsSortedString: string = recommendations.sort().toString();

		Logger.debug(`Sorted found authors: ${allFoundAuthorsAsSortedString}`);
		Logger.debug(`Sorted expected authors: ${allPublisherNamesAsSortedString}`);

		const result: boolean = allFoundAuthorsAsSortedString === allPublisherNamesAsSortedString;
		Logger.debug(`Authors match: ${result}`);

		return result;
	} catch (error) {
		Logger.error(`Error getting item titles: ${error}`);
		return false;
	}
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
		const skipSuite: boolean = false;

		const [pathToExtensionsListFileName, extensionsListFileName]: string[] = ['.vscode', 'extensions.json'];

		let vsCodeFolderItemLevel: number = 2;

		let recommendedExtensions: any = {
			recommendations: []
		};

		let parsedRecommendations: Array<{ name: string; publisher: string }>;
		suiteSetup('Login', async function (): Promise<void> {
			await loginTests.loginIntoChe();
		});

		setup('Skip follow-up tests if flagged', function (): void {
			if (skipSuite) {
				this.skip();
			}
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
			await projectAndFileTests.performTrustDialogs();
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
			await (
				await projectAndFileTests.getProjectTreeItem(projectSection, pathToExtensionsListFileName, vsCodeFolderItemLevel)
			)?.select();

			await (
				await projectAndFileTests.getProjectTreeItem(projectSection, extensionsListFileName, vsCodeFolderItemLevel + 1)
			)?.select();
			Logger.debug(`EditorView().openEditor(${extensionsListFileName})`);
			await new EditorView().openEditor(extensionsListFileName);
			await driverHelper.waitVisibility(webCheCodeLocators.Editor.inputArea);

			Logger.debug('Select and copy all text in the editor');
			const text: string = await getText();
			recommendedExtensions = JSON.parse(text.replace(/\/\*[\s\S]*?\*\/|(?<=[^:])\/\/.*|^\/\/.*/g, '').trim());

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
			extensionsView = await (await new ActivityBar().getViewControl('Extensions'))?.openView();
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
				await findItem(extensionSection, '@recommended');
			} catch (err) {
				await driverHelper.wait(TIMEOUT_CONSTANTS.TS_EXPAND_PROJECT_TREE_ITEM_TIMEOUT);
				await findItem(extensionSection, '@recommended');
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
				await findItem(extensionSection, '@recommended ');
			}

			Logger.debug('extensionSection.findItem by @recommended filter');
			try {
				Logger.debug(`Expected publisher names: ${publisherNames.join(', ')}`);
				const comparisonResult: boolean = await getVisibleFilteredItemsAndCompareWithRecommended(publisherNames);
				Logger.debug(`Comparison result: ${comparisonResult}`);
				expect(comparisonResult).to.be.true;
				Logger.debug(`All recommended extensions were found by  @recommended filter: ---- ${publisherNames} ----`);
			} catch (error) {
				Logger.error(`Error in getVisibleFilteredItemsAndCompareWithRecommended: ${error}`);
				throw error;
			}

			Logger.debug('extensionSection.findItem by @installed filter');
			try {
				await findItem(extensionSection, '@installed ');
			} catch (err) {
				await driverHelper.wait(TIMEOUT_CONSTANTS.TS_EXPAND_PROJECT_TREE_ITEM_TIMEOUT);
				await findItem(extensionSection, '@installed ');
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
