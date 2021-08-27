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
import { Ide, LeftToolbarButton } from '../Ide';
import { Logger } from '../../../utils/Logger';
import { By } from 'selenium-webdriver';
import { ContextMenu } from '../ContextMenu';
import { TimeoutConstants } from '../../../TimeoutConstants';

export enum OpenshiftAppExplorerToolbar {
    ReportExtensionIssueOnGitHub = 'Report Extension Issue on GitHub',
    RefreshView = 'Refresh View',
    SwitchContexts = 'Switch Contexts',
    LogIntoCluster = 'Login into Cluster'
}

export enum OpenshiftContextMenuItems {
    NewComponent = 'New Component',
    Push = 'Push'
}

@injectable()
export class OpenshiftPlugin {

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.Ide) private readonly ide: Ide,
        @inject(CLASSES.ContextMenu) private readonly contextMenu: ContextMenu
    ) {
    }

    async clickOnOpenshiftToollBarIcon(timeout: number = TimeoutConstants.TS_WAIT_OPENSHIFT_CONNECTOR_TREE_TIMEOUT) {
        Logger.debug(`OpenshiftPlugin.clickOnOpenshiftTollBar`);
        await this.ide.waitAndClickLeftToolbarButton(LeftToolbarButton.Openshift, timeout);
    }

    async waitOpenshiftConnectorTree(timeout: number = TimeoutConstants.TS_WAIT_OPENSHIFT_CONNECTOR_TREE_TIMEOUT) {
        Logger.debug(`OpenshiftPlugin.waitOpenshiftConnectorTree`);
        await this.driverHelper.waitPresence(By.id('openshiftProjectExplorer'), timeout);
    }

    async clickOnOpenshiftConnectorTree(timeout: number = TimeoutConstants.TS_WAIT_OPENSHIFT_CONNECTOR_TREE_TIMEOUT) {
        Logger.debug(`OpenshiftPlugin.clickOnOpenshiftConnectorTree`);
        await this.driverHelper.waitAndClick(By.id('plugin-view:openshiftProjectExplorer'), timeout);
    }

    async clickOnApplicationToolbarItem(item: OpenshiftAppExplorerToolbar, timeout: number = TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM) {
        Logger.debug(`OpenshiftPlugin.clickOnApplicationToolbarItem`);
        await this.driverHelper.waitAndClick(By.css(`div [title='${item}']`), timeout);
    }

    async getClusterIP(timeout: number = TimeoutConstants.TS_GET_CLUSTER_IP_TIMEOUT): Promise<string> {
        Logger.debug(`OpenshiftPlugin.getClusterIP`);
        return await this.driverHelper.waitAndGetText(By.xpath('//div[@id=\'openshiftProjectExplorer\']//div[@title [contains(text(), https)]]'), timeout);
    }

    async waitItemInTree(item: string, timeout: number = TimeoutConstants.TS_SELENIUM_TERMINAL_DEFAULT_TIMEOUT) {
        Logger.debug(`OpenshiftPlugin.waitItemInTree`);
        await this.driverHelper.waitPresence(By.xpath(`//div[contains(@id, ':${item}')]`), timeout);
    }

    async clickOnItemInTree(item: string, timeout: number = TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM) {
        Logger.debug(`OpenshiftPlugin.clickOnItemInTree`);
        await this.driverHelper.waitAndClick(By.xpath(`//div[contains(@id, ':${item}')]`), timeout);
    }

    async invokeContextMenuOnItem(treeItem: string) {
        Logger.debug(`OpenshiftPlugin.invokeContextMenuOnItem`);
        await this.contextMenu.invokeContextMenuOnTheElementWithMouse(By.xpath(`//div[contains(@id, ':${treeItem}')]`));
    }

    async invokeContextMenuCommandOnItem(treeItem: string, menuItem: OpenshiftContextMenuItems) {
        Logger.debug(`OpenshiftPlugin.clickOnItemInTree`);
        await this.contextMenu.invokeContextMenuOnTheElementWithMouse(By.xpath(`//div[contains(@id, ':${treeItem}')]`));
        await this.contextMenu.waitContextMenuAndClickOnItem(menuItem);
    }
}

