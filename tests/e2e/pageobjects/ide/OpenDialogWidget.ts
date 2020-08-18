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
import { Logger } from '../../utils/Logger';
import { DialogWindow } from '../ide/DialogWindow';
import { OpenWorkspaceWidget } from '../ide/OpenWorkspaceWidget';
import { By } from 'selenium-webdriver';
import { TimeoutConstants } from '../../TimeoutConstants';

export enum Locations {
    Theia = 'theia',
    Home = 'home',
    Root = '/'
}

export enum Buttons {
    Cancel = 'Cancel',
    AddContext = 'Add context folder for component in workspace.',
}

@injectable()
export class OpenDialogWidget {

    constructor(
        @inject(CLASSES.DialogWindow) private readonly dialogWindow: DialogWindow,
        @inject(CLASSES.OpenWorkspaceWidget) private readonly openWorkspaceWidget: OpenWorkspaceWidget,
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper
    ) { }

    async selectLocation(location: Locations, timeout: number = TimeoutConstants.TS_SELENIUM_DIALOG_WIDGET_TIMEOUT) {
        Logger.debug(`OpenDialogWidget.selectLocation`);
        await this.driverHelper.type(By.css('div.theia-NavigationPanel select'), location, timeout);
    }

    async selectItemInTree(pathToItem: string) {
        Logger.debug(`OpenDialogWidget.selectItemInTree "${pathToItem}"`);
        await this.openWorkspaceWidget.selectItemInTree(pathToItem);
    }


    async expandItemInTreeToPath(pathToItem: string, timeout: number = TimeoutConstants.TS_SELENIUM_DIALOG_WIDGET_TIMEOUT) {
        Logger.debug(`OpenDialogWidget.expandItemInTreeToPath "${pathToItem}"`);
        await this.openWorkspaceWidget.expandTreeToPath(pathToItem, timeout);
    }

    async clickOnButton(button: Buttons) {
        Logger.debug(`OpenDialogWidget.clickOnButton ${button}`);
        await this.dialogWindow.clickToButton(button);
    }

    async selectLocationAndAddContextFolder(location: Locations, path: string, button: Buttons) {
        await this.selectLocation(location);
        await this.expandItemInTreeToPath(path);
        await this.clickOnButton(button);
        await this.dialogWindow.waitDialogDissappearance();
    }

}
