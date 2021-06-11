import 'reflect-metadata';
import { injectable, inject } from 'inversify';
import { CLASSES } from '../../../inversify.types';
import { DriverHelper } from '../../../utils/DriverHelper';
import { Logger } from '../../../utils/Logger';
import { By, error } from 'selenium-webdriver';
import { TimeoutConstants } from '../../../TimeoutConstants';
import { LeftToolBar } from '../LeftToolBar';
import { TestConstants } from '../../../TestConstants';
import { TopMenu } from '../TopMenu';
import { QuickOpenContainer } from '../QuickOpenContainer';

@injectable()
export class GitHubPullRequestPlugin {
    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.LeftToolBar) private readonly leftToolbar: LeftToolBar,
        @inject(CLASSES.TopMenu) private readonly topMenu: TopMenu,
        @inject(CLASSES.QuickOpenContainer) private readonly quickOpenContainer: QuickOpenContainer
    ) { }

    async openView(timeout: number = TimeoutConstants.TS_PROJECT_TREE_TIMEOUT) {
        Logger.debug(`GithubPullRequestPlugin.openView`);

        await this.leftToolbar.selectView('GitHub', timeout);
    }

    async waitViewIcon(timeout: number = TimeoutConstants.TS_PROJECT_TREE_TIMEOUT) {
        Logger.debug(`GithubPullRequestPlugin.waitViewIcon`);

        await this.leftToolbar.waitToolIcon('GitHub', timeout);
    }

    async clickTreeItem(itemTitle: string, timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug(`GithubPullRequestPlugin.clickTreeItem`);

        await this.driverHelper.waitAndClick(this.getTreeItemLocator(itemTitle), timeout);
    }

    async waitTreeItem(itemTitle: string, timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug(`GithubPullRequestPlugin.waitTreeItem`);

        await this.driverHelper.waitVisibility(this.getTreeItemLocator(itemTitle), timeout);
    }

    async waitTreeItemExpanded(itemTitle: string, timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug(`GithubPullRequestPlugin.waitTreeItemExpanded`);

        await this.driverHelper.waitVisibility(this.getExpandedTreeItemIconLocator(itemTitle), timeout);
    }

    async waitTreeItemCollapsed(itemTitle: string, timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug(`GithubPullRequestPlugin.waitTreeItemCollapsed`);

        await this.driverHelper.waitVisibility(this.getCollapsedTreeItemIconLocator(itemTitle), timeout);
    }

    async expandTreeItem(itemTitle: string, timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug(`GithubPullRequestPlugin.expandTreeItem`);

        await this.driverHelper.getDriver().wait(async () => {
            if (await this.isTreeItemCollapsed(itemTitle)) {
                await this.clickTreeItem(itemTitle, timeout);
            }

            try {
                await this.waitTreeItemExpanded(itemTitle, timeout / 4);
                return true;
            } catch (err) {
                if (!(err instanceof error.TimeoutError)) {
                    throw err;
                }

                Logger.info(`The '${itemTitle}' item has not been expanded, try again`);
                await this.driverHelper.wait(TestConstants.TS_SELENIUM_DEFAULT_POLLING);
            }
        }, timeout);
    }

    async clickSignInButton(timeout: number = TimeoutConstants.TS_COMMON_PLUGIN_TEST_TIMEOUT) {
        Logger.debug(`GithubPullRequestPlugin.clickSignInButton`);

        const signInButtonLocator: By = By.xpath(`//div[@id='pr:github']//button[text()='Sign in']`);

        await this.driverHelper.waitAndClick(signInButtonLocator, timeout);
    }

    async createPrFromCommandMenu() {
        Logger.debug(`GithubPullRequestPlugin.createPrFromCommandMenu`);

        await this.topMenu.selectOption('View', 'Find Command...');
        await this.quickOpenContainer.typeAndSelectSuggestion('pull', 'GitHub Pull Requests: Create Pull Request');
    }

    private async isTreeItemCollapsed(itemTitle: string): Promise<boolean> {
        return await this.driverHelper.isVisible(this.getCollapsedTreeItemIconLocator(itemTitle));
    }

    private getTreeItemXpath(itemTitle: string): string {
        return `//div[@id='plugin-view-container:github-pull-requests']//div[@title='${itemTitle}']`;
    }

    private getTreeItemLocator(itemTitle: string): By {
        return By.xpath(this.getTreeItemXpath(itemTitle));
    }

    private getCollapsedTreeItemIconLocator(itemTitle: string): By {
        const collapsedTreeItemIconXpath: string = `${this.getTreeItemXpath(itemTitle)}` +
            `/parent::div//div[@data-node-id and contains(@class, 'theia-mod-collapsed')]`;

        return By.xpath(collapsedTreeItemIconXpath);
    }

    private getExpandedTreeItemIconLocator(itemTitle: string): By {
        const expandedTreeItemIconXpath: string = `${this.getTreeItemXpath(itemTitle)}` +
            `/parent::div//div[@data-node-id and not(contains(@class, 'theia-mod-collapsed'))]`;

        return By.xpath(expandedTreeItemIconXpath);
    }

}
