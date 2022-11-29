/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { injectable, inject } from 'inversify';
import { CLASSES } from '../../../inversify.types';
import { DriverHelper } from '../../../utils/DriverHelper';
import { By } from 'selenium-webdriver';
import { Logger } from '../../../utils/Logger';
import { Ide } from './Ide';
import { TimeoutConstants } from '../../../TimeoutConstants';

@injectable()
export class DialogWindow {
    private static readonly DIALOG_BODY_XPATH_LOCATOR: string = '//div[@id=\'theia-dialog-shell\']//div[@class=\'dialogBlock\']';
    private static readonly CLOSE_BUTTON_XPATH_LOCATOR: string = `${DialogWindow.DIALOG_BODY_XPATH_LOCATOR}//button[text()='close']`;

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.Ide) private readonly ide: Ide) { }

    async dialogDisplayes(): Promise<boolean> {
        Logger.debug('DialogWindow.dialogDisplayes');
        return await this.driverHelper.isVisible(By.xpath(DialogWindow.DIALOG_BODY_XPATH_LOCATOR));
    }

    async waitAndCloseIfAppear() {
        Logger.debug('DialogWindow.waitAndCloseIfAppear');

        const dialogDisplayes: boolean = await this.driverHelper.waitVisibilityBoolean(By.xpath(DialogWindow.DIALOG_BODY_XPATH_LOCATOR));

        if (dialogDisplayes) {
            await this.closeDialog();
            await this.waitDialogDissappearance();
        }

    }

    async clickToButton(buttonText: string) {
        Logger.debug('DialogWindow.clickToButton');
        const buttonLocator: By = By.xpath(`${DialogWindow.DIALOG_BODY_XPATH_LOCATOR}//button[text()='${buttonText}']`);
        await this.driverHelper.waitAndClick(buttonLocator, TimeoutConstants.TS_DIALOG_WINDOW_DEFAULT_TIMEOUT);
    }

    async closeDialog() {
        Logger.debug('DialogWindow.closeDialog');

        await this.clickToButton('close');
    }

    async clickToOpenLinkButton() {
        Logger.debug('DialogWindow.clickToOpenLinkButton');

        await this.clickToButton('Open Link');
    }

    async waitDialog(dialogText: string = '', timeout: number = TimeoutConstants.TS_DIALOG_WINDOW_DEFAULT_TIMEOUT) {
        Logger.debug('DialogWindow.waitDialog');

        // if dialog text is provided uses xpath with this text
        // if not uses window body xpath
        const dialogWithTextXpathLocator: string = `${DialogWindow.DIALOG_BODY_XPATH_LOCATOR}//*[contains(text(), '${dialogText}')]`;
        const dialogXpathLocator: string = (dialogText ? dialogWithTextXpathLocator : DialogWindow.DIALOG_BODY_XPATH_LOCATOR);

        await this.driverHelper.waitVisibility(By.xpath(dialogXpathLocator), timeout);
    }

    async waitDialogAndOpenLink(dialogText: string = '', applicationReaddyTimeout: number) {
        Logger.debug('DialogWindow.waitDialogAndOpenLink');

        await this.waitDialog(dialogText, applicationReaddyTimeout);
        await this.ide.waitApllicationIsReady(await this.getApplicationUrlFromDialog(dialogText), applicationReaddyTimeout);
        await this.clickToOpenLinkButton();
        await this.waitDialogDissappearance();
    }

    async waitDialogDissappearance() {
        Logger.debug('DialogWindow.waitDialogDissappearance');

        await this.driverHelper.waitDisappearanceWithTimeout(By.xpath(DialogWindow.CLOSE_BUTTON_XPATH_LOCATOR), TimeoutConstants.TS_DIALOG_WINDOW_DEFAULT_TIMEOUT);
    }

    async getApplicationUrlFromDialog(dialogWindowText: string) {
        const notificationTextLocator: By = By.xpath(`${DialogWindow.DIALOG_BODY_XPATH_LOCATOR}//*[contains(text(), '${dialogWindowText}')]`);

        let dialogWindow = await this.driverHelper.waitAndGetText(notificationTextLocator);
        let regexp: RegExp = new RegExp('^.*(https?://.*)$');

        if (!regexp.test(dialogWindow)) {
            throw new Error('Cannot obtaine url from notification message');
        }

        return dialogWindow.split(regexp)[1];
    }

}
