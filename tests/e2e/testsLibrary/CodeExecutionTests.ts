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
import Axios from 'axios';
import { CLASSES } from '../inversify.types';
import { inject, injectable } from 'inversify';
import { By, error, Key } from 'selenium-webdriver';
import { Ide } from '../pageobjects/ide/Ide';
import { Terminal } from '../pageobjects/ide/Terminal';
import { TopMenu } from '../pageobjects/ide/TopMenu';
import { DialogWindow } from '../pageobjects/ide/DialogWindow';
import { DriverHelper } from '../utils/DriverHelper';
import { Logger } from '../utils/Logger';
import { QuickOpenContainer } from '../pageobjects/ide/QuickOpenContainer';
import { BrowserTabsUtil } from '../utils/BrowserTabsUtil';
import { WorkspaceHandlingTests } from './WorkspaceHandlingTests';

@injectable()
export class CodeExecutionTests {

    private static lastApplicationUrl: string = '';

    constructor(
        @inject(CLASSES.Terminal) private readonly terminal: Terminal,
        @inject(CLASSES.TopMenu) private readonly topMenu: TopMenu,
        @inject(CLASSES.Ide) private readonly ide: Ide,
        @inject(CLASSES.DialogWindow) private readonly dialogWindow: DialogWindow,
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.QuickOpenContainer) private readonly quickOpenContainer: QuickOpenContainer,
        @inject(CLASSES.BrowserTabsUtil) private readonly browserTabsUtil: BrowserTabsUtil,
        @inject(CLASSES.WorkspaceHandlingTests) private readonly workspaceHandlingTests: WorkspaceHandlingTests) {}

    public runTask(taskName: string, timeout: number) {
        test(`Run command '${taskName}'`, async () => {
            await this.runTaskUsingQuickOpenContainer(taskName);
            await this.terminal.waitIconSuccess(taskName, timeout);
        });
    }

    public runTaskInputText(taskName: string, expectedQuery: string, inputText: string, timeout: number) {
        test(`Run command '${taskName}' expecting dialog shell`, async () => {
            await this.runTaskUsingQuickOpenContainer(taskName);
            await this.terminal.waitText(taskName, expectedQuery, timeout);
            await this.terminal.clickOnTab(taskName);
            await this.terminal.type(taskName, inputText);
            await this.terminal.type(taskName, Key.ENTER);
            await this.terminal.waitIconSuccess(taskName, timeout);
        });
    }

    public runTaskConsoleOutput(taskName: string, expectedText: string, timeout: number) {
        test(`Run command '${taskName}' expecting console putput: ${expectedText}`, async () => {
            await this.runTaskUsingQuickOpenContainer(taskName);
            await this.terminal.waitText(taskName, expectedText, timeout);
        });
    }

    public runTaskWithDialogShellAndOpenLink(taskName: string, expectedDialogText: string, timeout: number) {
        test(`Run command '${taskName}' expecting dialog shell`, async () => {
            await this.runTaskUsingQuickOpenContainer(taskName);
            await this.dialogWindow.waitDialogAndOpenLink(expectedDialogText, timeout);
        });
    }

    public runTaskWithDialogShellDjangoWorkaround(taskName: string, expectedDialogText: string, urlSubPath: string, timeout: number) {
        test(`Run command '${taskName}' expecting dialog shell`, async () => {
            await this.runTaskUsingQuickOpenContainer(taskName);
            await this.dialogWindow.waitDialog(expectedDialogText, timeout);
            const dialogRedirectUrl: string = await this.dialogWindow.getApplicationUrlFromDialog(expectedDialogText);
            const augmentedPreviewUrl: string = dialogRedirectUrl + urlSubPath;
            await this.dialogWindow.closeDialog();
            await this.dialogWindow.waitDialogDissappearance();
            await this.driverHelper.getDriver().wait(async () => {
                try {
                    const res = await Axios.get(augmentedPreviewUrl);
                    if (res.status === 200) { return true; }
                } catch (error) { await this.driverHelper.wait(1_000); }
            }, timeout);
        });
    }

    public runTaskWithDialogShellAndClose(taskName: string, expectedDialogText: string, timeout: number) {
        test(`Run command '${taskName}' expecting dialog shell`, async () => {
            await this.runTaskUsingQuickOpenContainer(taskName);
            await this.dialogWindow.waitDialog(expectedDialogText, timeout);
            await this.dialogWindow.closeDialog();
            await this.dialogWindow.waitDialogDissappearance();
        });
    }

    public runTaskWithNotification(taskName: string, notificationText: string, timeout: number) {
        test(`Run command '${taskName}' expecting notification pops up`, async () => {
            await this.runTaskUsingQuickOpenContainer(taskName);
            await this.ide.waitNotification(notificationText, timeout);
        });
    }

    public runTaskWithNotificationAndOpenLink(taskName: string, notificationText: string, buttonText: string, timeout: number) {
        test(`Run command '${taskName}' expecting notification`, async () => {
            await this.runTaskUsingQuickOpenContainer(taskName);
            await this.ide.waitNotification(notificationText, timeout);
            // the application can have delay before running. Sometimes it consume about  sec.
            // todo improve the check of avalibility test application
            await this.driverHelper.wait(10_000);
            await this.ide.clickOnNotificationButton(notificationText, buttonText);
            // delay for openning an app. window
            // todo may be improved.
            await this.driverHelper.wait(5_000);
            CodeExecutionTests.lastApplicationUrl = await this.driverHelper.getDriver().getCurrentUrl();
        });
    }

    /**
     * @deprecated This method should not be used, preview widget is no longer a valid page fragment.
     */
    public runTaskWithNotificationAndOpenLinkPreviewNoUrl(taskName: string, notificationText: string, timeout: number) {
        test(`Run command '${taskName}' expecting notification`, async () => {
            await this.runTaskUsingQuickOpenContainer(taskName);
            await this.ide.waitNotification(notificationText, timeout);
            this.workspaceHandlingTests.setWindowHandle(await this.browserTabsUtil.getCurrentWindowHandle());
            // the application can have delay before running. Sometimes it consume about  sec.
            // todo improve the check of avalibility test application
            await this.driverHelper.wait(10_000);
            await this.ide.clickOnNotificationButton(notificationText, 'Open In Preview');
            // delay for openning an app. window
            // todo may be improved.
            await this.driverHelper.wait(5_000);
            CodeExecutionTests.lastApplicationUrl = await this.driverHelper.getDriver().getCurrentUrl();
        });
    }

    public runTaskWithNotificationAndOpenLinkUnexposedPort(taskName: string, notificationText: string, portOpenText: string, timeout: number) {
        test(`Run command '${taskName}' expecting notification with unexposed port`, async () => {
            await this.runTaskUsingQuickOpenContainer(taskName);
            await this.ide.waitNotificationAndConfirm(notificationText, timeout);
            await this.ide.waitNotificationAndOpenLink(portOpenText, timeout);
            await this.driverHelper.wait(5_000);
            CodeExecutionTests.lastApplicationUrl = await this.driverHelper.getDriver().getCurrentUrl();
        });
    }

    public verifyRunningApplication(locator: By, applicationCheckTimeout: number, polling: number) {
        test(`Verify running application by locator: '${locator}'`, async () => {
            let timeout: number = applicationCheckTimeout / polling;
            let attempts: number = applicationCheckTimeout / timeout;
            for (let i = 0; i < attempts; i++) {
                try {
                    await this.driverHelper.waitPresence(locator, timeout);
                } catch (err) {
                    if (err instanceof error.TimeoutError) {
                        if (i === attempts - 1) {
                            Logger.error(`CodeExecutionTests.verifyRunningApplication out of retries to wait for ${locator} presence.`);
                            throw err;
                        } else {
                            Logger.trace(`CodeExecutionTests.verifyRunningApplication timed out waiting for ${locator} presence. Retrying ${i}.`);
                        }
                    } else {
                        Logger.error(`CodeExecutionTests.verifyRunningApplication failed with unexpected exception.`);
                        throw err;
                    }
                }
            }
        });
    }

    public getLastApplicationUrl(): string {
        return CodeExecutionTests.lastApplicationUrl;
    }

    /**
     * @deprecated This method should not be used, preview widget is no longer a valid page fragment.
     */
    public refreshPreviewWindow() {
        test('Refreshing preview widget', async () => {
            Logger.warn(`Method is deprecated.`);
        });
    }

    /**
     * @deprecated This method should not be used, preview widget is no longer a valid page fragment.
     */
    public closePreviewWidget() {
        test('Close preview widget', async () => {
            Logger.warn(`Method is deprecated.`);
        });
    }

    public waitTerminalPresent(terminalTabName: string, timeout: number) {
        test('Wait terminal presence', async () => {
            await this.terminal.waitTab(terminalTabName, timeout);
            await this.driverHelper.wait(5000);
        });
    }

    public closeTerminal(taskName: string) {
        test('Close the terminal tasks', async () => {
            await this.ide.closeAllNotifications();
            await this.terminal.rejectTerminalProcess(taskName);
            await this.terminal.closeTerminalTab(taskName);
            await this.dialogWindow.waitAndCloseIfAppear();
        });
    }

    private async runTaskUsingQuickOpenContainer(taskName: string) {
        await this.topMenu.selectOption('Terminal', 'Run Task...');
            try {
                await this.quickOpenContainer.waitContainer();
            } catch (err) {
                Logger.warn(`CodeExecutionTests.runTaskInputText quickOpenContainer failed to open, retrying.`);
                await this.topMenu.selectOption('Terminal', 'Run Task...');
                await this.quickOpenContainer.waitContainer();
            }
            await this.quickOpenContainer.type(`${taskName}${Key.ENTER}`);
    }
}
