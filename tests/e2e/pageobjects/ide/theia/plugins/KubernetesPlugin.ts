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
import { injectable, inject } from 'inversify';
import { CLASSES } from '../../../../inversify.types';
import { DriverHelper } from '../../../../utils/DriverHelper';
import { Logger } from '../../../../utils/Logger';
import { By } from 'selenium-webdriver';
import { TimeoutConstants } from '../../../../TimeoutConstants';
import { LeftToolBar } from '../LeftToolBar';

@injectable()
export class KubernetesPlugin {
    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.LeftToolBar) private readonly leftToolbar: LeftToolBar) { }


    async openView(timeout: number = TimeoutConstants.TS_PROJECT_TREE_TIMEOUT) {
        Logger.debug(`KubernetesPlugin.openView`);

        await this.leftToolbar.selectView('Kubernetes', timeout);
    }

    async clickToSection(sectionTitle: string, timeout: number = TimeoutConstants.TS_PROJECT_TREE_TIMEOUT) {
        Logger.debug(`KubernetesPlugin.clickToSection  ${sectionTitle}`);

        const sectionLocator: By = By.xpath(this.getSectionLocator(sectionTitle));

        await this.driverHelper.waitAndClick(sectionLocator, timeout);
    }

    async isSectionExpanded(sectionTitle: string): Promise<boolean> {
        Logger.debug(`KubernetesPlugin.isSectionExpanded  ${sectionTitle}`);

        const expandedsectionLocator: By = this.getExpandedSectionLocator(sectionTitle);

        return await this.driverHelper.isVisible(expandedsectionLocator);
    }

    async waitSectionExpanded(sectionTitle: string, timeout: number = TimeoutConstants.TS_PROJECT_TREE_TIMEOUT) {
        Logger.debug(`KubernetesPlugin.waitSectionExpanded  ${sectionTitle}`);

        await this.driverHelper.waitVisibility(this.getExpandedSectionLocator(sectionTitle), timeout);
    }

    async waitSectionCollapsed(sectionTitle: string, timeout: number = TimeoutConstants.TS_PROJECT_TREE_TIMEOUT) {
        Logger.debug(`KubernetesPlugin.waitSectionCollapsed  ${sectionTitle}`);

        await this.driverHelper.waitVisibility(this.getCollapsedSectionLocator(sectionTitle), timeout);
    }

    async expandSection(sectionTitle: string, timeout: number = TimeoutConstants.TS_PROJECT_TREE_TIMEOUT) {
        Logger.debug(`KubernetesPlugin.expandSection  ${sectionTitle}`);

        if (await this.isSectionExpanded(sectionTitle)) {
            return;
        }

        await this.clickToSection(sectionTitle, timeout);
        await this.waitSectionExpanded(sectionTitle, timeout);
    }

    async clickToRefreshButton(sectionTitle: string, timeout: number = TimeoutConstants.TS_PROJECT_TREE_TIMEOUT) {
        Logger.debug(`KubernetesPlugin.clickToRefreshButton`);

        const refreshButtonLocator: By = By.xpath(`//div[contains(@class, 'theia-header')]//div[contains(@class, 'theia-view-container-part-title')]//div[@title='Refresh']`);
        const sectionTitleLocator: By = By.xpath(this.getSectionLocator(sectionTitle));

        await this.driverHelper.scrollTo(sectionTitleLocator, timeout);
        await this.driverHelper.waitAndClick(refreshButtonLocator, timeout);
    }

    async waitListItemContains(partialText: string, timeout: number = TimeoutConstants.TS_PROJECT_TREE_TIMEOUT) {
        Logger.debug(`KubernetesPlugin.waitListItemContains  ${partialText}`);

        await this.driverHelper.waitVisibility(this.getListItemPartialTextLocator(partialText), timeout);
    }

    async waitListItem(expectedText: string, timeout: number = TimeoutConstants.TS_PROJECT_TREE_TIMEOUT) {
        Logger.debug(`KubernetesPlugin.waitListItem  ${expectedText}`);

        await this.driverHelper.waitVisibility(this.getListItemTextLocator(expectedText), timeout);
    }

    private getSectionLocator(sectionTitle: string): string {
        return `//div[contains(@class, 'theia-header')]//span[@title='${sectionTitle}']`;
    }

    private getExpandedSectionLocator(sectionTitle: string): By {
        return By.xpath(`${this.getSectionLocator(sectionTitle)}/parent::div/span[contains(@class, 'theia-ExpansionToggle') and not(contains(@class, 'theia-mod-collapsed'))]`);
    }

    private getCollapsedSectionLocator(sectionTitle: string): By {
        return By.xpath(`${this.getSectionLocator(sectionTitle)}/parent::div/span[contains(@class, 'theia-ExpansionToggle') and contains(@class, 'theia-mod-collapsed')]`);
    }

    private getListItemPartialTextLocator(partialText: string): By {
        return By.xpath(`//div[@id='extension.vsKubernetesExplorer']//div[contains(text(), "${partialText}")]`);
    }

    private getListItemTextLocator(expectedText: string): By {
        return By.xpath(`//div[contains(@class, 'body')]//div[@class='theia-TreeContainer']//div[@class='theia-TreeNodeContent']//span[text()='${expectedText}']`);
    }

}
