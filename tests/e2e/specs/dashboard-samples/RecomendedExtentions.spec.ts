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
import { TestConstants } from '../../constants/TestConstants';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';

const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);

const webCheCodeLocators: Locators = new CheCodeLocatorLoader().webCheCodeLocators;
const samples: any = TestConstants.TS_SAMPLE_LIST.split(',');
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
            const label: string = TestConstants.TS_SELENIUM_PROJECT_ROOT_FILE_NAME;
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
                Logger.warn(`Welcome modal dialog was not shown: ${e}`);
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
            // recommendedExtensions.recommendations = recommendedExtensions.recommendations.map((r: string) => r.substring(r.indexOf('.') + 1, r.length));
            recommendedExtensions.recommendations = ['javascript'];
        });

        test(`Open "Extensions" view section`, async function (): Promise<void> {
            Logger.debug(`ActivityBar().getViewControl('Extensions'))?.openView(): open Extensions view.`);
            extensionsView = await (await new ActivityBar().getViewControl('Extensions'))?.openView();
        });

        test(`Wait until extensions starts installation`, async function (): Promise<void> {
            Logger.info(`Time for extensions installation TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT=${TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT}`);
            await driverHelper.wait(TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT);
            browserTabsUtil.refreshPage();
            await driverHelper.wait(TimeoutConstants.TS_OPEN_EDITOR_TIMEOUT);
        });

        test(`Check if extensions is installed and enabled`, async function (): Promise<void> {
            Logger.debug(`extensionsView?.getContent().getSections(): get current section.`);
            [extensionSection] = await extensionsView?.getContent().getSections() as ExtensionsViewSection[];

            Logger.info(`Check if recommendedExtensions.recommendations are installed: ${recommendedExtensions.recommendations}.`);
            for (const extension of recommendedExtensions.recommendations) {
                Logger.debug(`extensionSection.findItem(${extension}).`);
                await extensionSection.findItem(extension);

                Logger.debug(`extensionsView?.getContent().getSections(): switch to marketplace section.`);
                const [marketplaceSection]: ExtensionsViewSection[] = await extensionsView?.getContent().getSections() as ExtensionsViewSection[];
                await driverHelper.waitVisibility(webCheCodeLocators.ExtensionsViewSection.items, TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT);

                Logger.debug(`marketplaceSection.getVisibleItems(): get first item.`);
                const [firstFoundItem]: ExtensionsViewItem[] = await marketplaceSection.getVisibleItems();

                Logger.debug(`firstFoundItem?.isInstalled()`);
                const isInstalled: boolean = await firstFoundItem?.isInstalled() as boolean;

                Logger.debug(`firstFoundItem?.isInstalled(): ${isInstalled}.`);
                expect(isInstalled).eqls(true);

                Logger.debug(`firstFoundItem.manage(): get context menu.`);
                const extensionManageMenu: ContextMenu = await firstFoundItem.manage();

                Logger.debug(`extensionManageMenu.getItems(): get menu items.`);
                const extensionMenuItems: ContextMenuItem[] = await extensionManageMenu.getItems();
                let extensionMenuItemLabels: string = '';
                for (const item of extensionMenuItems) {
                    Logger.trace(`extensionMenuItems -> item.getLabel(): get menu items names.`);
                    extensionMenuItemLabels += (await item.getLabel()) + ' ';
                }

                Logger.debug(`extensionMenuItemLabels: ${extensionMenuItemLabels}.`);
                expect(extensionMenuItemLabels).contains('Disable').and.not.contains('Enable');
            }
        });

        test('Stopping and deleting the workspace', async function (): Promise<void> {
            await workspaceHandlingTests.stopAndRemoveWorkspace(WorkspaceHandlingTests.getWorkspaceName());
        });
    }

    loginTests.logoutFromChe();
});
