import 'reflect-metadata';
import { injectable, inject } from 'inversify';
import { CLASSES } from '../../../inversify.types';
import { DriverHelper } from '../../../utils/DriverHelper';
import { Logger } from '../../../utils/Logger';
import { WebElement, Button, By } from 'selenium-webdriver';
import { TimeoutConstants } from '../../../TimeoutConstants';
import { LeftToolBar } from '../LeftToolBar';
import { TestConstants } from '../../../TestConstants';



@injectable()
export class KubernetesPlugin {
    private static readonly SUGGESTION_WIDGET_BODY_CSS: string = 'ul.p-Menu-content';

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.LeftToolBar) private readonly leftToolbar: LeftToolBar) { }


    async openView(timeout: number = TimeoutConstants.TS_PROJECT_TREE_TIMEOUT) {
        Logger.debug(`LeftToolBar.openView`);

        await this.leftToolbar.selectView('Kubernetes', timeout);
    }

    async clickToSection(sectionTitle: string, timeout: number = TimeoutConstants.TS_PROJECT_TREE_TIMEOUT) {
        Logger.debug(`LeftToolBar.clickToSection  ${sectionTitle}`);

        const sectionLocator: By = By.xpath(this.getSectionLocator(sectionTitle));

        await this.driverHelper.waitAndClick(sectionLocator, timeout);
    }

    async isSectionExpanded(sectionTitle: string): Promise<boolean> {
        Logger.debug(`LeftToolBar.isSectionExpanded  ${sectionTitle}`);

        const expandedsectionLocator: By = this.getExpandedSectionLocator(sectionTitle);

        return await this.driverHelper.isVisible(expandedsectionLocator);
    }

    async waitSectionExpanded(sectionTitle: string, timeout: number = TimeoutConstants.TS_PROJECT_TREE_TIMEOUT) {
        Logger.debug(`LeftToolBar.waitSectionExpanded  ${sectionTitle}`);

        await this.driverHelper.waitVisibility(this.getExpandedSectionLocator(sectionTitle), timeout);
    }

    async expandSection(sectionTitle: string, timeout: number = TimeoutConstants.TS_PROJECT_TREE_TIMEOUT) {
        Logger.debug(`LeftToolBar.expandSection  ${sectionTitle}`);

        if (await this.isSectionExpanded(sectionTitle)) {
            return;
        }

        await this.clickToSection(sectionTitle, timeout);
        await this.waitSectionExpanded(sectionTitle, timeout);
    }

    async clickToRefreshButton(sectionTitle: string, timeout: number = TimeoutConstants.TS_PROJECT_TREE_TIMEOUT) {
        Logger.debug(`LeftToolBar.clickToRefreshButton`);

        const refreshButtonLocator: By = By.xpath(`//div[contains(@class, 'theia-header')]//div[contains(@class, 'theia-view-container-part-title')]//div[@title='Refresh']`);
        const sectionTitleLocator: By = By.xpath(this.getSectionLocator(sectionTitle));

        await this.driverHelper.scrollTo(sectionTitleLocator, timeout);
        await this.driverHelper.waitAndClick(refreshButtonLocator, timeout);
    }

    async waitListItemContains(partialText: string, timeout: number = TimeoutConstants.TS_PROJECT_TREE_TIMEOUT) {
        Logger.debug(`LeftToolBar.waitListItemContains  ${partialText}`);

        await this.driverHelper.waitVisibility(this.getListItemPartialTextLocator(partialText), timeout);
    }

    async waitListItem(expectedText: string, timeout: number = TimeoutConstants.TS_PROJECT_TREE_TIMEOUT) {
        Logger.debug(`LeftToolBar.waitListItem  ${expectedText}`);

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
        return By.xpath(`//div[contains(@class, 'body')]//div[@class='theia-TreeContainer']//div[@class='theia-TreeNodeContent']//span[contains(text(), '${partialText}')]`)
    }

    private getListItemTextLocator(expectedText: string): By {
        return By.xpath(`//div[contains(@class, 'body')]//div[@class='theia-TreeContainer']//div[@class='theia-TreeNodeContent']//span[text()='${expectedText}']`)
    }


}
