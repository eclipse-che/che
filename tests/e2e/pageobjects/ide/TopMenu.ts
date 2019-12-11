import { injectable, inject } from 'inversify';
import { CLASSES } from '../../inversify.types';
import { DriverHelper } from '../../utils/DriverHelper';
import { TestConstants } from '../../TestConstants';
import { By, error } from 'selenium-webdriver';
import { Ide } from './Ide';
import { Logger } from '../../utils/Logger';
import { QuickOpenContainer } from './QuickOpenContainer';

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
export class TopMenu {
    private static readonly TOP_MENU_BUTTONS: string[] = ['File', 'Edit', 'Selection', 'View', 'Go', 'Debug', 'Terminal', 'Help'];

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.Ide) private readonly ide: Ide,
        @inject(CLASSES.QuickOpenContainer) private readonly quickOpenContainer: QuickOpenContainer) { }

    public async waitTopMenu(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug('TopMenu.waitTopMenu');

        for (const buttonText of TopMenu.TOP_MENU_BUTTONS) {
            const buttonLocator: By = this.getTopMenuButtonLocator(buttonText);
            await this.driverHelper.waitVisibility(buttonLocator, timeout);
        }
    }

    public async selectOption(topMenuButtonText: string, submenuItemtext: string) {
        Logger.debug(`TopMenu.selectOption "${topMenuButtonText}"`);

        await this.clickOnTopMenuButton(topMenuButtonText);
        await this.clickOnSubmenuItem(submenuItemtext);
    }

    public async clickOnTopMenuButton(buttonText: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`TopMenu.clickOnTopMenuButton "${buttonText}"`);

        const buttonLocator: By = this.getTopMenuButtonLocator(buttonText);

        await this.ide.closeAllNotifications();
        await this.driverHelper.waitAndClick(buttonLocator, timeout);
    }

    public async clickOnSubmenuItem(itemText: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`TopMenu.clickOnSubmenuItem "${itemText}"`);

        const submenuItemLocator: By = this.getSubmenuItemLocator(itemText);
        await this.driverHelper.waitAndClick(submenuItemLocator, timeout);
    }

    public async runTask(task: string) {
        await this.selectOption('Terminal', 'Run Task...');
        try {
            await this.quickOpenContainer.waitContainer();
        } catch (err) {
            if (err instanceof error.TimeoutError) {
                console.log(`After clicking to the "Terminal" -> "Run Task ..." the "Quick Open Container" has not been displayed, one more try`);

                await this.selectOption('Terminal', 'Run Task...');
                await this.quickOpenContainer.waitContainer();
            }
        }

        await this.quickOpenContainer.clickOnContainerItem(task);
        await this.quickOpenContainer.clickOnContainerItem('Continue without scanning the task output');
    }

    private getTopMenuButtonLocator(buttonText: string): By {
        return By.xpath(`//div[@id='theia:menubar']//div[@class='p-MenuBar-itemLabel' and text()='${buttonText}']`);
    }

    private getSubmenuItemLocator(submenuItemtext: string): By {
        return By.xpath(`//ul[@class='p-Menu-content']//li[@data-type='command']//div[text()='${submenuItemtext}']`);
    }

}
