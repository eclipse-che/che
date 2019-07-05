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
import { CLASSES } from '../../inversify.types';
import { DriverHelper } from '../../utils/DriverHelper';
import { By } from 'selenium-webdriver';

@injectable()
export class WarningDialog {
    private static readonly DIALOG_BODY_XPATH_LOCATOR: string = '//div[@id=\'theia-dialog-shell\']//div[@class=\'dialogBlock\']';
    private static readonly CLOSE_BUTTON_XPATH_LOCATOR: string = `${WarningDialog.DIALOG_BODY_XPATH_LOCATOR}//button[text()='close']`;

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async dialogDisplayes(): Promise<boolean> {

        return await this.driverHelper.isVisible(By.xpath(WarningDialog.DIALOG_BODY_XPATH_LOCATOR));
    }

    async waitAndCloseIfAppear() {
        const dialogDisplayes: boolean = await this.driverHelper.waitVisibilityBoolean(By.xpath(WarningDialog.DIALOG_BODY_XPATH_LOCATOR));

        if (dialogDisplayes) {
            await this.closeDialog();
            await this.waitDialogDissappearance();
        }

    }

    async closeDialog() {
        await this.driverHelper.waitAndClick(By.xpath(WarningDialog.CLOSE_BUTTON_XPATH_LOCATOR));
    }

    async waitDialogDissappearance() {
        await this.driverHelper.waitDisappearanceWithTimeout(By.xpath(WarningDialog.CLOSE_BUTTON_XPATH_LOCATOR));
    }

}
