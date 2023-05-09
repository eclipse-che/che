/*********************************************************************
 * Copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import 'reflect-metadata';
import { inject, injectable } from 'inversify';
import { DriverHelper } from '../../utils/DriverHelper';
import { CLASSES } from '../../configs/inversify.types';
import { By } from 'selenium-webdriver';
import { Logger } from '../../utils/Logger';
import { TimeoutConstants } from '../../constants/TimeoutConstants';
import { OcpImportFromGitPage } from './OcpImportFromGitPage';
import { e2eContainer } from '../../configs/inversify.config';

@injectable()
export class OcpMainPage {

    private static readonly MAIN_PAGE_HEADER_LOCATOR: By = By.id('page-main-header');
    private static readonly SELECT_ROLE_BUTTON_LOCATOR: By = By.xpath('//*[@data-test-id="perspective-switcher-toggle"]');
    private static readonly ADD_BUTTON_LOCATOR: By = By.xpath('//*[@data-test-id="+Add-header"]');
    private static readonly IMPORT_FROM_GIT_ITEM_LOCATOR: By = By.xpath('//*[@data-test="item import-from-git"]');
    private static readonly SELECT_PROJECT_DROPDOWN_LOCATOR: By = By.xpath('//div[@class="co-namespace-dropdown"]//button');
    private static readonly PROJECT_FILTER_INPUT_LOCATOR: By = By.xpath('//*[@data-test="dropdown-text-filter"]');
    private static readonly SKIP_TOUR_BUTTON_LOCATOR: By = By.xpath('//*[text()="Skip tour"]');

    private static getRoleLocator(role: string): By {
        return By.xpath(`//a//*[text()="${role}"]`);
    }

    private static getProjectDropdownItemLocator(projectName: string): By {
        return By.xpath(`//button//*[text()="${projectName}"]`);
    }

    constructor(
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async waitOpenMainPage(): Promise<void> {
        Logger.debug(`${this.constructor.name}.${this.waitOpenMainPage.name}`);

        await this.driverHelper.waitVisibility(OcpMainPage.MAIN_PAGE_HEADER_LOCATOR, TimeoutConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT);
    }

    async clickOnSelectRoleButton(): Promise<void> {
        Logger.debug(`${this.constructor.name}.${this.clickOnSelectRoleButton.name}`);

        await this.driverHelper.waitAndClick(OcpMainPage.SELECT_ROLE_BUTTON_LOCATOR);
    }

    async clickAddToProjectButton(): Promise<void> {
        Logger.debug(`${this.constructor.name}.${this.clickAddToProjectButton.name}`);

        await this.driverHelper.waitAndClick(OcpMainPage.ADD_BUTTON_LOCATOR);
    }

    async selectDeveloperRole(): Promise<void> {
        Logger.debug(`${this.constructor.name}.${this.selectDeveloperRole.name}`);

        await this.waitOpenMainPage();
        await this.tryToSkipWebTour();
        await this.clickOnSelectRoleButton();
        await this.selectRole('Developer');
        await this.tryToSkipWebTour();
    }

    async selectImportFromGitMethod(): Promise<OcpImportFromGitPage> {
        Logger.debug(`${this.constructor.name}.${this.selectImportFromGitMethod.name}`);

        await this.driverHelper.waitAndClick(OcpMainPage.IMPORT_FROM_GIT_ITEM_LOCATOR);
        return e2eContainer.get(CLASSES.OcpImportFromGitPage);
    }

    async openImportFromGitPage(): Promise<OcpImportFromGitPage> {
        Logger.debug(`${this.constructor.name}.${this.openImportFromGitPage.name}`);

        await this.clickAddToProjectButton();
        return await this.selectImportFromGitMethod();
    }

    async selectProject(projectName: string): Promise<void> {
        Logger.debug(`${this.constructor.name}.${this.selectProject.name}`);

        await this.driverHelper.waitAndClick(OcpMainPage.SELECT_PROJECT_DROPDOWN_LOCATOR);
        await this.driverHelper.enterValue(OcpMainPage.PROJECT_FILTER_INPUT_LOCATOR, projectName);
        await this.driverHelper.waitAndClick(OcpMainPage.getProjectDropdownItemLocator(projectName));
    }

    private async selectRole(role: string): Promise<void> {
        Logger.debug(`${this.constructor.name}.${this.selectRole.name} - selecting role ${role}`);

        await this.driverHelper.waitAndClick(OcpMainPage.getRoleLocator(role));
    }

    private async tryToSkipWebTour(): Promise<void> {
        Logger.debug(`${this.constructor.name}.${this.tryToSkipWebTour.name}`);

        if (await this.driverHelper.isVisible(OcpMainPage.SKIP_TOUR_BUTTON_LOCATOR)) {
            await this.driverHelper.waitAndClick(OcpMainPage.SKIP_TOUR_BUTTON_LOCATOR);

            Logger.debug(`${this.constructor.name}.${this.tryToSkipWebTour.name} - Welcome tour modal dialog was located and skipped`);
        } else {
            Logger.debug(`${this.constructor.name}.${this.tryToSkipWebTour.name} - Welcome tour modal dialog was not located`);
        }
    }
}
