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
import { TimeoutConstants } from '../../../TimeoutConstants';
import { DialogWindow } from '../DialogWindow';
import { TopMenu } from '../TopMenu';
import { QuickOpenContainer } from '../QuickOpenContainer';

@injectable()
export class GitPlugin {
    private static readonly COMMIT_MESSAGE_TEXTAREA_CSS: string = 'textarea#theia-scm-input-message';

    constructor(
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.DialogWindow) private readonly dialogWindow: DialogWindow,
        @inject(CLASSES.TopMenu) private readonly topMenu: TopMenu,
        @inject(CLASSES.QuickOpenContainer) private readonly quickOpenContainer: QuickOpenContainer
    ) { }

    async openGitPluginContainer(timeout: number = TimeoutConstants.TS_GIT_CONAINER_INTERACTION_TIMEOUT) {
        Logger.debug('GitPlugin.openGitPluginContainer');

        const sourceControlGitBtnXpathLocator: string = '//li[@id=\'shell-tab-scm-view-container\' and contains(@style, \'height\')]';
        await this.driverHelper.waitAndClick(By.xpath(sourceControlGitBtnXpathLocator), timeout);
        await this.waitViewOfContainer(timeout);
    }

    async waitViewOfContainer(timeout: number = TimeoutConstants.TS_GIT_CONAINER_INTERACTION_TIMEOUT) {
        Logger.debug('GitPlugin.waitViewOfContainer');

        const gitHubContainerIdLocator: By = By.id('scm-view-container--scm-view');
        await this.driverHelper.waitVisibility(gitHubContainerIdLocator, timeout);
    }

    async waitCommitMessageTextArea(timeout: number = TimeoutConstants.TS_GIT_CONAINER_INTERACTION_TIMEOUT) {
        Logger.debug('GitPlugin.waitCommitMessageTextArea');

        const textAreaCssLocator: By = By.css(GitPlugin.COMMIT_MESSAGE_TEXTAREA_CSS);
        await this.driverHelper.waitVisibility(textAreaCssLocator, timeout);
    }

    async typeCommitMessage(commitMessage: string, timeout: number = TimeoutConstants.TS_GIT_CONAINER_INTERACTION_TIMEOUT) {
        Logger.debug('GitPlugin.typeCommitMessage');

        await this.waitCommitMessageTextArea(timeout);
        await this.driverHelper.type(By.css(GitPlugin.COMMIT_MESSAGE_TEXTAREA_CSS), commitMessage, timeout);
    }

    async selectCommandInMoreActionsMenu(commandName: string, timeout: number = TimeoutConstants.TS_GIT_CONAINER_INTERACTION_TIMEOUT) {
        Logger.debug('GitPlugin.selectCommandInMoreActionsMenu');

        await this.clickOnMoreActions(timeout);
        await this.driverHelper.waitAndClick(By.xpath(`//li[@data-command]/div[text()=\'${commandName}\']`), timeout);
    }

    async clickOnMoreActions(timeout: number = TimeoutConstants.TS_GIT_CONAINER_INTERACTION_TIMEOUT) {
        Logger.debug('GitPlugin.clickOnMoreActions');

        await this.driverHelper.waitAndClick(By.id('__more__'), timeout);
    }

    async waitChangedFileInChagesList(expectedItem: string, timeout: number = TimeoutConstants.TS_GIT_CONAINER_INTERACTION_TIMEOUT) {
        Logger.debug('GitPlugin.waitChangedFileInChagesList');

        await this.driverHelper.waitPresence(By.xpath(`//span[@class='name' and text()=\'${expectedItem}\']`), timeout);
    }

    async waitStagedFileInStagedChanges(expectedStagedItem: string, timeout: number = TimeoutConstants.TS_GIT_CONAINER_INTERACTION_TIMEOUT) {
        Logger.debug('GitPlugin.waitStagedFileInStagedChanges');

        await this.driverHelper.waitPresence(By.xpath(`//div[text()='Staged Changes']/parent::div/parent::div/parent::div/following-sibling::div//span[text()=\'${expectedStagedItem}\']`), timeout);
    }

    async commitFromCommandMenu() {
        Logger.debug('GitPlugin.commitFromScmView');
        await this.topMenu.selectOption('View', 'Find Command...');
        await this.quickOpenContainer.typeAndSelectSuggestion('Commit', 'Git: Commit');
    }

    async pushChangesFromCommandMenu() {
        Logger.debug('GitPlugin.commitFromScmView');
        await this.topMenu.selectOption('View', 'Find Command...');
        await this.quickOpenContainer.typeAndSelectSuggestion('Push', 'Git: Push');
    }

    async stageAllChanges(expectedStagedItem: string, timeout: number = TimeoutConstants.TS_GIT_CONAINER_INTERACTION_TIMEOUT) {
        Logger.debug('GitPlugin.stageAllChanges');

        await this.driverHelper.waitVisibility(By.xpath('//div[@class=\'noWrapInfo theia-TreeNodeSegmentGrow\' and text()=\'Changes\']'), timeout);
        await this.driverHelper.scrollTo(By.xpath('//div[@class=\'noWrapInfo theia-TreeNodeSegmentGrow\' and text()=\'Changes\']'));
        await this.driverHelper.waitAndClick(By.xpath('//a[@title=\'Stage All Changes\']'), timeout);
        await this.waitStagedFileInStagedChanges(expectedStagedItem);
    }

    async waitDataIsSynchronized(timeout: number = TimeoutConstants.TS_GIT_CONAINER_INTERACTION_TIMEOUT) {
        Logger.debug('GitPlugin.waitDataIsSynchronized');
        await this.driverHelper.waitDisappearance(By.xpath(`//div[contains(@title,'Synchronize Changes')]//span[contains(.,' 0↓')]`), timeout);
    }

    async clickOnSelectRepositoryButton(timeout: number = TimeoutConstants.TS_GIT_CONAINER_INTERACTION_TIMEOUT) {
        Logger.debug('GitPlugin.clickOnSelectRepositoryButton');
        await this.driverHelper.waitAndClick(By.xpath(`//button[@class='theia-button main' and text()='Select Repository Location']`), timeout);
        await this.dialogWindow.waitDialogDissappearance();
    }

}
