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
import { By, until } from 'selenium-webdriver';
import { DriverHelper } from '../utils/DriverHelper';
import { CLASSES } from '../configs/inversify.types';
import { Logger } from '../utils/Logger';
import { TimeoutConstants } from '../constants/TimeoutConstants';

@injectable()
export class ProjectAndFileTests {

    constructor(
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) {}

    public async waitWorkspaceReadinessForCheCodeEditor(): Promise<void> {
        Logger.debug(`${this.constructor.name}.${this.waitWorkspaceReadinessForCheCodeEditor.name} - Waiting for editor.`);
        try {
            const start: number = new Date().getTime();
            await this.driverHelper.getDriver().wait(until.elementLocated(By.className('monaco-workbench')), TimeoutConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
            const end: number = new Date().getTime();
            Logger.debug(`${this.constructor.name}.${this.waitWorkspaceReadinessForCheCodeEditor.name} - editor was opened in ${end - start} seconds.`);
        } catch (err) {
            Logger.error(`ProjectAndFileTestsCheCode.waitWorkspaceReadinessForCheCodeEditor - waiting for workspace readiness failed: ${err}`);
            throw err;
        }
    }
}
