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
import { TestConstants } from '../../TestConstants';

@injectable()
export class OpenWorkspaceWidget {
    private static readonly OPEN_WORKSPACE_MAIN_VIEW_XPATH = '//div[@class=\'dialogTitle\']/div[text()=\'Open Workspace\']';
    private static readonly OPEN_WORKSPACE_OPEN_BTN_CSS = 'div.dialogControl>button.main';
    private static readonly THEIA_LOCATION_LIST_CSS = 'select.theia-LocationList';

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) {
    }

    async waitOpenWorkspaceWidget(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitVisibility(By.xpath(OpenWorkspaceWidget.OPEN_WORKSPACE_MAIN_VIEW_XPATH), timeout);
    }

    async waitWidgetIsClosed(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitDisappearance(By.xpath(OpenWorkspaceWidget.OPEN_WORKSPACE_MAIN_VIEW_XPATH), timeout);
    }

    async selectItemInTree(pathToItem: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitAndClick(By.id(pathToItem), timeout);
    }

    async clickOnOpenButton(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitAndClick(By.css(OpenWorkspaceWidget.OPEN_WORKSPACE_OPEN_BTN_CSS), timeout);
    }

    async selectItemInTreeAndOpenWorkspace(item: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.selectItemInTree(item, timeout);
        await this.clickOnOpenButton();
        await this.waitWidgetIsClosed();
    }

    async expandTreeToPath(path: string) {
        const pathNodes: string[] = path.split('/');
        for (let currentPath of pathNodes) {
            await this.driverHelper.waitAndClick(By.id(`/${currentPath}`));
        }
    }

    async selectRootWorkspaceItemInDropDawn(rootProject: string) {
        await this.driverHelper.waitAndClick(By.css(OpenWorkspaceWidget.THEIA_LOCATION_LIST_CSS));
        await this.driverHelper.waitAndClick(By.css(`option[value=\'file:///${rootProject}']`));
    }


}
