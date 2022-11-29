/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import 'reflect-metadata';
import { inject, injectable } from 'inversify';
import { By, error } from 'selenium-webdriver';
import { Ide } from '../../pageobjects/ide/theia/Ide';
import { ProjectTree } from '../../pageobjects/ide/theia/ProjectTree';
import { OpenEditors } from '../../pageobjects/ide/theia/OpenEditors';
import { Editor } from '../../pageobjects/ide/theia/Editor';
import { TimeoutConstants } from '../../TimeoutConstants';
import { DriverHelper } from '../../utils/DriverHelper';
import { CLASSES } from '../../inversify.types';
import { Logger } from '../../utils/Logger';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';

@injectable()
export class ProjectAndFileTestsTheia {

    constructor(
        @inject(CLASSES.Ide) private readonly ide: Ide,
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.ProjectTree) private readonly projectTree: ProjectTree,
        @inject(CLASSES.OpenEditors) private readonly openEditors: OpenEditors,
        @inject(CLASSES.Editor) private readonly editor: Editor,
        @inject(CLASSES.BrowserTabsUtil) private readonly browserTabsUtil: BrowserTabsUtil) {}

        public switchBackToFirstOpenIdeTabFromLeftToRight() {
            test('WorkspaceHandlingTests.switchBackToIdeTab', async () => {
                let tabs = await this.driverHelper.getDriver().getAllWindowHandles();
                Logger.trace(`WorkspaceHandlingTests.switchBackToIdeTab Found ${tabs.length} window handles, iterating...`);
                for (let i = 0; i < tabs.length; i++) {
                    await this.browserTabsUtil.switchToWindow(tabs[i]);
                    try {
                        await this.ide.waitIde(TimeoutConstants.TS_IDE_LOAD_TIMEOUT);
                        Logger.debug(`WorkspaceHandlingTests.switchBackToIdeTab located and switched to IDE tab`);
                        return;
                    } catch (err) {
                        if (err instanceof error.TimeoutError) {
                            Logger.warn(`WorkspaceHandlingTests.switchBackToIdeTab Locator timed out, trying with another window handle.`);
                            continue;
                        }
                        Logger.error(`WorkspaceHandlingTests.switchBackToIdeTab Received unexpected exception while trying to locate IDE tab:${err}`);
                        throw err;
                    }
                }
                Logger.error(`WorkspaceHandlingTests.switchBackToIdeTab Failed to locate IDE tab, out of window handles.`);
            });
        }

    public waitWorkspaceReadiness(sampleName : string, folder: string, checkNotification: boolean = true, restartWorkspaceDialogIsExpected: boolean = false) {
        test('Wait for workspace readiness', async () => {
            await this.ide.waitIde(TimeoutConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);

            if (restartWorkspaceDialogIsExpected) {
                await this.ide.clickOnCancelDialogButton();
            }

            await this.projectTree.openProjectTreeContainer();
            if (!await this.openEditors.isExpansionToggleCollapsed()) {
                await this.openEditors.waitAndClickExpansionToggle();
            }

            await this.projectTree.waitProjectImported(sampleName, folder);
        });
    }

    public waitWorkspaceReadinessNoSubfolder(sampleName : string, checkNotification: boolean = true) {
        test('Wait for workspace readiness', async () => {
            await this.ide.waitIde(TimeoutConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
            if (checkNotification) {
                await this.ide.waitNotificationAndClickOnButton('Do you trust the authors of', 'Yes, I trust', 60_000);
            }
            await this.projectTree.openProjectTreeContainer();
            if (!await this.openEditors.isExpansionToggleCollapsed()) {
                await this.openEditors.waitAndClickExpansionToggle();
            }
            await this.projectTree.waitProjectImportedNoSubfolder(sampleName);
        });
    }

    public openFile(filePath: string, fileName: string) {
        test('Expand project and open file in editor', async () => {
            await this.projectTree.expandPathAndOpenFile(filePath, fileName);
            await this.editor.selectTab(fileName);
        });
    }

    public checkFilePresence(filePath: string, timeout: number) {
        test('Check that file is present in project', async () => {
            await this.projectTree.waitItem(filePath, timeout);
        });
    }

    public checkFileNotExists(filePath: string) {
        test('Check that file is not exist in project', async () => {
            await this.projectTree.waitItemDisappearance(filePath);
        });
    }

    public checkProjectBranchName(branchName: string) {
        test('Check branch name is ${}', async () => {
            await this.driverHelper.waitVisibility(By.xpath(`//div[@id='theia-statusBar']/div//span[text()=' ${branchName}']`));
        });
    }
}
