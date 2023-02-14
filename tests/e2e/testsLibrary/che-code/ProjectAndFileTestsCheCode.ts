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
import { By, error, until, WebElement } from 'selenium-webdriver';
import { DriverHelper } from '../../utils/DriverHelper';
import { CLASSES } from '../../inversify.types';
import { Logger } from '../../utils/Logger';
import { TimeoutConstants } from '../../TimeoutConstants';
import { Workbench, SideBarView, ViewContent, ViewSection, ViewItem } from 'monaco-page-objects';
import { TestConstants } from '../../TestConstants';

@injectable()
export class ProjectAndFileTestsCheCode {

    private static readonly ENDPOINTS_TITLE_TEXT: string = 'endpoints';
    private static readonly TRUST_AUTHORS_DIALOG_BOX_TEXT: string = 'Do you trust the authors of the files in this folder?';
    private static readonly TRUST_AUTHORS_DIALOG_BUTTON_TEXT: string = 'Yes, I trust the authors';

    constructor(
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) {}

    public async waitWorkspaceReadinessForCheCodeEditor(): Promise<Workbench> {
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;
        try {
            await this.driverHelper.getDriver().wait(until.elementLocated(By.className('monaco-workbench')), TimeoutConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
            const workbench: Workbench = new Workbench();
            const sideBarView: SideBarView = workbench.getSideBar();
            const sidebarViewContent: ViewContent = sideBarView.getContent();
            let sidebarViewSections: ViewSection[];
            while (true) {
                sidebarViewSections = await sidebarViewContent.getSections();
                Logger.debug(`ProjectAndFileTestsCheCode.waitWorkspaceReadinessForCheCodeEditor - Waiting for ${ProjectAndFileTestsCheCode.ENDPOINTS_TITLE_TEXT} sidebar section:`);
                for (const section of sidebarViewSections) {
                    Logger.trace(`Sidebar section: ${await section.getTitle()}`);
                    if (await section.getTitle() === ProjectAndFileTestsCheCode.ENDPOINTS_TITLE_TEXT) {
                        return workbench;
                    }
                }
                await this.driverHelper.getDriver().sleep(polling);
            }
        } catch (err) {
            Logger.error(`ProjectAndFileTestsCheCode.waitWorkspaceReadinessForCheCodeEditor - waiting for workspace readiness failed: ${err}`);
            throw err;
        }
    }

    public async waitAndConfirmTrustAuthorsDialogBox(): Promise<void> {
        Logger.debug('ProjectAndFileTestsCheCode.waitAndConfirmTrustAuthorsDialogBox');
        try {
            Logger.trace('ProjectAndFileTestsCheCode.waitAndConfirmTrustAuthorsDialogBox - waiting for trust authors dialog box overlay');
            const dialog: WebElement = await this.driverHelper.waitPresence(By.id('monaco-dialog-message-text'));
            const dialogText: string = await dialog.getText();
            if (dialogText === ProjectAndFileTestsCheCode.TRUST_AUTHORS_DIALOG_BOX_TEXT) {
                const trustAuthorsDialogCheckbox: WebElement = await this.driverHelper.waitPresence(By.css('body > div > div.monaco-dialog-modal-block.dimmed > div > div > div.dialog-message-row > div.dialog-message-container > div.dialog-checkbox-row > div.monaco-custom-toggle.codicon.codicon-check.monaco-checkbox'));
                let isChecked: boolean = await trustAuthorsDialogCheckbox.getAttribute('aria-checked') === 'true';
                if (!isChecked) {
                    trustAuthorsDialogCheckbox.click();
                    isChecked = await trustAuthorsDialogCheckbox.getAttribute('aria-checked') === 'true';
                    Logger.trace(`Clicked thrust authors dialog checkbox. Is checked: ${isChecked}`);
                }
                const trustAuthorsDialogButton: WebElement = await this.driverHelper.waitPresence(By.css('body > div > div.monaco-dialog-modal-block.dimmed > div > div > div.dialog-buttons-row > div > div:nth-child(2) > a'));
                const trustAuthorsDialogButtonText: string = await trustAuthorsDialogButton.getText();
                if (trustAuthorsDialogButtonText === ProjectAndFileTestsCheCode.TRUST_AUTHORS_DIALOG_BUTTON_TEXT) {
                    trustAuthorsDialogButton.click();
                    Logger.trace(`Clicked trust authors dialog button.`);
                }
            }
        } catch (err) {
            if (err instanceof error.TimeoutError) {
                Logger.warn(`ProjectAndFileTestsCheCode.waitAndConfirmTrustAuthorsDialogBox - "Do you trust the authors" dialog not shown. Continuing...`);
            }
        }
    }

    public async waitForRootProjectPresence(workbench: Workbench, sectionTitle: string, rootFolderName: string): Promise<ViewItem> {
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;
        try {
            const sideBarView: SideBarView = workbench.getSideBar();
            const sidebarViewContent: ViewContent = sideBarView.getContent();
            let sidebarViewSections: ViewSection[];
            let projectSidebarViewSection: ViewSection;
            Logger.debug(`ProjectAndFileTestsCheCode.waitForRootProjectPresence - Waiting for ${sectionTitle} section in sidebar:`);
            while (true) {
                sidebarViewSections = await sidebarViewContent.getSections();
                for (const section of sidebarViewSections) {
                    Logger.trace(`\tSidebar section: ${await section.getTitle()}`);
                    if (await section.getTitle() === sectionTitle) {
                        projectSidebarViewSection = section;
                        const projectRootFolderElement: ViewItem | undefined = await projectSidebarViewSection.findItem(rootFolderName);
                        if (projectRootFolderElement === undefined) {
                            throw new Error(`ProjectAndFileTestsCheCode.waitForRootProjectPresence - ${sectionTitle} not found.`);
                        }
                        return projectRootFolderElement;
                    }
                }
                this.driverHelper.getDriver().sleep(polling);
            }
        } catch (err) {
            Logger.error(`ProjectAndFileTestsCheCode.waitForRootProjectPresence - waiting for ${sectionTitle} in sidebar section failed: ${err}`);
            throw err;
        }
    }
}
