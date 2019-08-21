import { injectable, inject } from 'inversify';
import { CLASSES } from '../../inversify.types';
import { DriverHelper } from '../../utils/DriverHelper';
import { TestConstants } from '../../TestConstants';
import { By, WebElement } from 'selenium-webdriver';
import { Ide, RightToolbarButton } from './Ide';

/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

@injectable()
export class GitHubPlugin {
    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.Ide) private readonly ide: Ide) { }

    async openGitHubPluginContainer(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const selectedGitButtonLocator: By = By.xpath(Ide.SELECTED_GIT_BUTTON_XPATH);

        await this.ide.waitRightToolbarButton(RightToolbarButton.Git, timeout);
        const isButtonEnabled: boolean = await this.driverHelper.waitVisibilityBoolean(selectedGitButtonLocator);

        if (!isButtonEnabled) {
            await this.ide.waitAndClickRightToolbarButton(RightToolbarButton.Git);
        }

        await this.waitGitHubContainer(timeout);
    }

    async waitGitHubContainer(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const githubContainerLocator: By = By.css('#theia-gitContainer .theia-git-main-container');

        await this.driverHelper.waitVisibility(githubContainerLocator, timeout);
    }

    async getChangesList(): Promise<string[]> {
        const gitHubChangesLocator: By = By.xpath('//div[@id=\'theia-gitContainer\']//div[@id=\'unstagedChanges\']//div[contains(@class, \'gitItem\')]');
        const changesElements: WebElement[] = await this.driverHelper.waitAllPresence(gitHubChangesLocator);
        const changesCount: number = changesElements.length;
        let gitHubChanges: string[] = [];

        for (let i = 0; i < changesCount; i++) {
            const gitHubChangesItemLocator: By = By.xpath(this.getGitHubChangesItemXpathLocator(i));
            const changesText: string = await this.driverHelper.waitAndGetText(gitHubChangesItemLocator);

            gitHubChanges.push(changesText);
        }

        return gitHubChanges;
    }

    async waitChangesPresence(changesText: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper
            .getDriver()
            .wait(async () => {
                const changes: string[] = await this.getChangesList();
                const isChangesPresent: boolean = changes.indexOf(changesText) !== -1;

                if (isChangesPresent) {
                    return true;
                }

            }, timeout);
    }

    getGitHubChangesItemXpathLocator(index: number): string {
        return `(//div[@id='theia-gitContainer']//div[@id='unstagedChanges']//div[contains(@class, 'gitItem')])[${index + 1}]`;
    }

}
