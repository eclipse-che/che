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
import { Ide } from '../pageobjects/ide/Ide';
import { Terminal } from '../pageobjects/ide/Terminal';
import { TopMenu } from '../pageobjects/ide/TopMenu';
import { DialogWindow } from '../pageobjects/ide/DialogWindow';
import { DriverHelper } from '../utils/DriverHelper';
import { Key } from 'selenium-webdriver';
import Axios from 'axios';
import { CLASSES } from '../inversify.types';

@injectable()
export class CodeExecutionTests {

    constructor(
        @inject(CLASSES.Terminal) private readonly terminal: Terminal,
        @inject(CLASSES.TopMenu) private readonly topMenu: TopMenu,
        @inject(CLASSES.Ide) private readonly ide: Ide,
        @inject(CLASSES.DialogWindow) private readonly dialogWindow: DialogWindow,
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) {}

    public runTask(taskName: string, timeout: number) {
        test(`Run command '${taskName}'`, async () => {
            await this.topMenu.runTask(taskName);
            await this.terminal.waitIconSuccess(taskName, timeout);
        });
    }

    public runTaskInputText(taskName: string, waitedText: string, inputText: string, timeout: number) {
        test(`Run command '${taskName}' expecting dialog shell`, async () => {
            await this.topMenu.runTask(taskName);
            await this.terminal.waitText(taskName, waitedText, timeout);
            await this.terminal.clickOnTab(taskName);
            await this.terminal.type(taskName, inputText);
            await this.terminal.type(taskName, Key.ENTER);
            await this.terminal.waitIconSuccess(taskName, timeout);
        });
    }

    public runTaskWithDialogShellAndOpenLink(taskName: string, expectedDialogText: string, timeout: number) {
        test(`Run command '${taskName}' expecting dialog shell`, async () => {
            await this.topMenu.runTask(taskName);
            await this.dialogWindow.waitDialogAndOpenLink(expectedDialogText, timeout);
        });
    }

    public runTaskWithDialogShellDjangoWorkaround(taskName: string, expectedDialogText: string, urlSubPath: string, timeout: number) {
        test(`Run command '${taskName}' expecting dialog shell`, async () => {
            await this.topMenu.runTask(taskName);
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
            await this.topMenu.runTask(taskName);
            await this.dialogWindow.waitDialog(expectedDialogText, timeout);
            await this.dialogWindow.closeDialog();
            await this.dialogWindow.waitDialogDissappearance();
        });
    }

    public runTaskWithNotification(taskName: string, notificationText: string, timeout: number) {
        test(`Run command '${taskName}' expecting notification pops up`, async () => {
            await this.topMenu.runTask(taskName);
            await this.ide.waitNotification(notificationText, timeout);
        });
    }

    public closeTerminal(taskName: string) {
        test('Close the terminal tasks', async () => {
            await this.ide.closeAllNotifications();
            await this.terminal.closeTerminalTab(taskName);
        });
    }
}
