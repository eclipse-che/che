/*********************************************************************
 * Copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import {
    ActivityBar,
    ContextMenu,
    ContextMenuItem,
    EditorView,
    ExtensionsViewItem,
    ExtensionsViewSection,
    Locators,
    ModalDialog,
    SideBarView,
    TextEditor,
    ViewItem,
    ViewSection
} from 'monaco-page-objects';
import { registerRunningWorkspace } from '../MochaHooks';
import { LoginTests } from '../../tests-library/LoginTests';
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES } from '../../configs/inversify.types';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { ProjectAndFileTests } from '../../tests-library/ProjectAndFileTests';
import { Logger } from '../../utils/Logger';
import { DriverHelper } from '../../utils/DriverHelper';
import { CheCodeLocatorLoader } from '../../pageobjects/ide/CheCodeLocatorLoader';
import { expect } from 'chai';
import { TimeoutConstants } from '../../constants/TimeoutConstants';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { PluginsTestConstants } from '../../constants/PluginsTestConstants';
import { BaseTestConstants } from '../../constants/BaseTestConstants';

const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);

const webCheCodeLocators: Locators = new CheCodeLocatorLoader().webCheCodeLocators;
const samples: string[] = PluginsTestConstants.TS_SAMPLE_LIST.split(',');
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);

suite(`Check if recommended extensions installed for ${samples}`, async function (): Promise<void> {
    let projectSection: ViewSection;
    let extensionsView: SideBarView | undefined;
    let extensionSection: ExtensionsViewSection;

    const extensionsListFileName: string = 'extensions.json';
    let recommendedExtensions: any = {
        recommendations: []
    };

    loginTests.loginIntoChe();

    for (const sample of samples) {
        workspaceHandlingTests.createAndOpenWorkspace(sample);
        workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();

        test('Registering the running workspace', async function (): Promise<void> {
            registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
        });

        test('Wait workspace readiness', async function (): Promise<void> {
            await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
        });

        test('Wait until the project will be imported and accept it as trusted one', async function (): Promise<void> {
            [projectSection] = await new SideBarView().getContent().getSections();
            const label: string = BaseTestConstants.TS_SELENIUM_PROJECT_ROOT_FILE_NAME;
            Logger.debug(`projectSection.findItem: find ${label}`);
            const isFileImported: ViewItem | undefined = await projectSection.findItem(label);
            expect(isFileImported).not.eqls(undefined);
            try {
                const buttonYesITrustTheAuthors: string = `Yes, I trust the authors`;
                await driverHelper.waitVisibility(webCheCodeLocators.WelcomeContent.button, TimeoutConstants.TS_DIALOG_WINDOW_DEFAULT_TIMEOUT);
                const trustedProjectDialog: ModalDialog = new ModalDialog();
                Logger.debug(`trustedProjectDialog.pushButton: "${buttonYesITrustTheAuthors}"`);
                await trustedProjectDialog.pushButton(buttonYesITrustTheAuthors);
            } catch (e) {
                Logger.debug(`Welcome modal dialog was not shown: ${e}`);
            }
        });

        test(`Get recommended extensions list from ${extensionsListFileName}`, async function (): Promise<void> {
            Logger.debug(`projectSection.findItem(item))?.select(): expand .vscode folder and open extensions.json.`);
            await (await projectSection.findItem('.vscode'))?.select();
            // time to expand project tree
            await driverHelper.wait(TimeoutConstants.TS_EXPAND_PROJECT_TREE_ITEM_TIMEOUT);
            await (await projectSection.findItem(extensionsListFileName))?.select();
            Logger.debug(`EditorView().openEditor(${extensionsListFileName})`);
            const editor: TextEditor = await new EditorView().openEditor(extensionsListFileName) as TextEditor;
            await driverHelper.waitVisibility(webCheCodeLocators.Editor.inputArea);
            Logger.debug(`editor.getText(): get recommended extensions as text from editor, delete comments and parse to object.`);
            recommendedExtensions = JSON.parse((await editor.getText()).replace(/\/\*[\s\S]*?\*\/|(?<=[^:])\/\/.*|^\/\/.*/g, '').trim());
            Logger.debug(`recommendedExtensions.recommendations: Get recommendations clear names using map().`);
            recommendedExtensions.recommendations = recommendedExtensions.recommendations.map((r: { split: (arg: string) => [any, any]; }) => {
                const [publisher, name] = r.split('.');
                return {publisher, name};
            });
            Logger.info(`Recommended extension for this workspace:\n${JSON.stringify(recommendedExtensions.recommendations)}.`);
        });

        test(`Open "Extensions" view section`, async function (): Promise<void> {
            Logger.debug(`ActivityBar().getViewControl('Extensions'))?.openView(): open Extensions view.`);
            extensionsView = await (await new ActivityBar().getViewControl('Extensions'))?.openView();
        });

        test(`Let extensions complete installation`, async function (): Promise<void> {
            Logger.info(`Time for extensions installation TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT=${TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT}`);
            await driverHelper.wait(TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT);
        });

        test(`Check if extensions is installed and enabled`, async function (): Promise<void> {
            this.retries(10);
            Logger.debug(`ActivityBar().getViewControl('Extensions'))?.openView(): open Extensions view.`);
            extensionsView = await (await new ActivityBar().getViewControl('Extensions'))?.openView();

            Logger.debug(`extensionsView?.getContent().getSections(): get current section.`);
            [extensionSection] = await extensionsView?.getContent().getSections() as ExtensionsViewSection[];
            await driverHelper.waitAllPresence(webCheCodeLocators.ExtensionsViewSection.itemTitle, TimeoutConstants.TS_EDITOR_TAB_INTERACTION_TIMEOUT);

            for (const extension of recommendedExtensions.recommendations) {
                Logger.info(`Check if ${JSON.stringify(extension)} are installed.`);

                Logger.debug(`extensionSection.findItem(${extension.name}).`);
                await extensionSection.findItem(extension.name);

                // check if extension require reload the page
                if (await driverHelper.isVisible((webCheCodeLocators.ExtensionsViewSection as any).requireReloadButton)) {
                    Logger.debug(`Extension require reload the editor. Refreshing the page..`);
                    await browserTabsUtil.refreshPage();
                    await projectAndFileTests.waitWorkspaceReadinessForCheCodeEditor();
                    Logger.debug(`ActivityBar().getViewControl('Extensions'))?.openView(): open Extensions view.`);
                    extensionsView = await (await new ActivityBar().getViewControl('Extensions'))?.openView();
                    Logger.debug(`extensionsView?.getContent().getSections(): get current section.`);
                    [extensionSection] = await extensionsView?.getContent().getSections() as ExtensionsViewSection[];
                    await driverHelper.waitAllPresence(webCheCodeLocators.ExtensionsViewSection.itemTitle, TimeoutConstants.TS_EDITOR_TAB_INTERACTION_TIMEOUT);
                    Logger.debug(`extensionSection.findItem(${extension.name}).`);
                    await extensionSection.findItem(extension.name);
                }

                Logger.debug(`extensionsView?.getContent().getSections(): switch to marketplace section.`);
                const [marketplaceSection]: ExtensionsViewSection[] = await extensionsView?.getContent().getSections() as ExtensionsViewSection[];
                await driverHelper.waitVisibility(webCheCodeLocators.ExtensionsViewSection.items, TimeoutConstants.TS_EDITOR_TAB_INTERACTION_TIMEOUT);

                Logger.debug(`marketplaceSection.getVisibleItems(): get all found items.`);
                const allFinedItems: ExtensionsViewItem[] = await marketplaceSection.getVisibleItems();

                let itemWithRightNameAndPublisher: ExtensionsViewItem | undefined;
                for (const item of allFinedItems) {
                    Logger.debug(`Try to find extension published by ${extension.publisher}.`);
                    if (await item.getAuthor() === extension.publisher) {
                        itemWithRightNameAndPublisher = item;
                        Logger.debug(`Extension was found: ${await itemWithRightNameAndPublisher?.getTitle()}`);
                        break;
                    }
                    if (itemWithRightNameAndPublisher === undefined) {
                        Logger.error(`Extension with publisher as ${extension.publisher} was not found.`);
                    }
                }

                Logger.debug(`itemWithRightNameAndPublisher?.isInstalled()`);
                const isInstalled: boolean = await itemWithRightNameAndPublisher?.isInstalled() as boolean;

                Logger.debug(`itemWithRightNameAndPublisher?.isInstalled(): ${isInstalled}.`);
                expect(isInstalled).eqls(true);

                Logger.debug(`itemWithRightNameAndPublisher.manage(): get context menu.`);
                const extensionManageMenu: ContextMenu | undefined = await itemWithRightNameAndPublisher?.manage();

                Logger.debug(`extensionManageMenu.getItems(): get menu items.`);
                const extensionMenuItems: ContextMenuItem[] | undefined = await extensionManageMenu?.getItems();
                let extensionMenuItemLabels: string = '';
                for (const item of extensionMenuItems as ContextMenuItem[]) {
                    Logger.trace(`extensionMenuItems -> item.getLabel(): get menu items names.`);
                    extensionMenuItemLabels += (await item.getLabel()) + ' ';
                }

                Logger.debug(`extensionMenuItemLabels: ${extensionMenuItemLabels}.`);
                expect(extensionMenuItemLabels).contains('Disable').and.not.contains('Enable');
            }
        });

        test('Stop the workspace', async function (): Promise<void> {
            await workspaceHandlingTests.stopWorkspace(WorkspaceHandlingTests.getWorkspaceName());
            await browserTabsUtil.closeAllTabsExceptCurrent();
        });

        test('Delete the workspace', async function (): Promise<void> {
            await workspaceHandlingTests.removeWorkspace(WorkspaceHandlingTests.getWorkspaceName());
        });
    }

    loginTests.logoutFromChe();
});
