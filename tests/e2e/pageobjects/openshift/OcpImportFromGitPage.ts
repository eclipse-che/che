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
import { OcpApplicationPage } from './OcpApplicationPage';
import { e2eContainer } from '../../configs/inversify.config';

@injectable()
export class OcpImportFromGitPage {

    constructor(
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) {
    }

    async enterGitRepoUrl(gitRepoUrl: string): Promise<void> {
        Logger.debug(`${this.constructor.name}.${this.enterGitRepoUrl.name} "${gitRepoUrl}"`);

        await this.driverHelper.enterValue(By.id('form-input-git-url-field'), gitRepoUrl);
    }

    async clickOnAdvancedOptionsButton(): Promise<void> {
        Logger.debug(`${this.constructor.name}.${this.clickOnAdvancedOptionsButton.name}`);

        if (!(await this.driverHelper.isVisible(By.xpath('//*[text()="Hide advanced Git options"]')))) {
            await this.driverHelper.waitAndClick(By.xpath('//*[text()="Show advanced Git options"]//ancestor::button'));
        }
    }

    async enterGitReference(gitReference: string): Promise<void> {
        Logger.debug(`${this.constructor.name}.${this.enterGitReference.name} "${gitReference}"`);

        await this.driverHelper.enterValue(By.id('form-input-git-ref-field'), gitReference);
    }

    async selectBuilderImageImportStrategy(): Promise<void> {
        Logger.debug(`${this.constructor.name}.${this.selectBuilderImageImportStrategy.name}`);

        await this.driverHelper.scrollToAndClick(By.xpath('//*[text()="Edit Import Strategy"]//ancestor::button'));
        await this.driverHelper.scrollToAndClick(By.xpath('//*[text()="Builder Image"]//parent::div//parent::div'));
    }

    async addLabel(label: string): Promise<void> {
        Logger.debug(`${this.constructor.name}.${this.addLabel.name} "${label}"`);

        await this.driverHelper.scrollToAndClick(By.xpath('//button[text()="Labels"]'));
        await this.driverHelper.scrollToAndEnterValue(By.id('form-selector-labels-field'), label);
    }

    async submitConfiguration(): Promise<OcpApplicationPage> {
        Logger.debug(`${this.constructor.name}.${this.submitConfiguration.name}`);

        await this.driverHelper.waitAndClick(By.xpath('//*[@data-test-id="submit-button"]'));
        return e2eContainer.get(CLASSES.OcpApplicationPage);
    }

    async fitAndSubmitConfiguration(gitRepoUrl: string, gitReference: string, label: string): Promise<OcpApplicationPage> {
        Logger.debug(`${this.constructor.name}.${this.fitAndSubmitConfiguration.name}`);

        await this.enterGitRepoUrl(gitRepoUrl);
        await this.clickOnAdvancedOptionsButton();
        await this.enterGitReference(gitReference);
        await this.selectBuilderImageImportStrategy();
        await this.addLabel(label);
        return await this.submitConfiguration();
    }
}
