/*********************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import 'reflect-metadata';
import { injectable, inject } from 'inversify';
import { DriverHelper } from '../../utils/DriverHelper';
import { CLASSES } from '../../inversify.types';
import { By } from 'selenium-webdriver';
import { Logger } from '../../utils/Logger';
import { TimeoutConstants } from '../../TimeoutConstants';

@injectable()
export class OpenEditors {
    private static readonly OPEN_EDITORS_WIDGET_LOCATOR: By = By.id('explorer-view-container--theia-open-editors-widget');
    private static readonly OPEN_EDITORS_CONTAINER_LOCATOR: By = By.css('#theia-left-side-panel #explorer-view-container--theia-open-editors-widget .theia-TreeContainer');
    private static readonly OPEN_EDITORS_EXPANSION_TOGGLE_LOCATOR: By = By.css('#theia-left-side-panel #explorer-view-container--theia-open-editors-widget .theia-ExpansionToggle.codicon.codicon-chevron-down');
    private static readonly OPEN_EDITORS_EXPANSION_TOGGLE_COLLAPSED_LOCATOR: By = By.css('#theia-left-side-panel #explorer-view-container--theia-open-editors-widget .theia-ExpansionToggle.codicon.codicon-chevron-down.theia-mod-collapsed');
    private static readonly OPEN_EDITORS_CLOSE_ALL_BUTTON_LOCATOR: By = By.id('navigator.close.all.editors.toolbar');

    constructor(
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper
    ) { }

    async waitOpenEditors(timeout: number = TimeoutConstants.TS_OPEN_EDITORS_TIMEOUT) {
        Logger.debug('OpenEditors.waitOpenEditors');

        await this.driverHelper.waitPresence(OpenEditors.OPEN_EDITORS_WIDGET_LOCATOR, timeout);
    }

    async waitOpenEditorsContainer(timeout: number = TimeoutConstants.TS_OPEN_EDITORS_TIMEOUT) {
        Logger.debug('OpenEditors.waitOpenEditorsContainer');

        await this.driverHelper.waitPresence(OpenEditors.OPEN_EDITORS_CONTAINER_LOCATOR, timeout);
    }

    async clickCloseAllEditors(timeout: number = TimeoutConstants.TS_OPEN_EDITORS_CLICK_ON_ITEM_TIMEOUT) {
        Logger.debug('OpenEditors.clickCloseAllEditors');

        await this.driverHelper.waitAndClick(OpenEditors.OPEN_EDITORS_CLOSE_ALL_BUTTON_LOCATOR, timeout);
    }

    async waitAndClickExpansionToggle(timeout: number = TimeoutConstants.TS_OPEN_EDITORS_CLICK_ON_ITEM_TIMEOUT) {
        Logger.debug('OpenEditors.waitAndClickExpansionToggle');

        Logger.trace('OpenEditors.waitAndClickExpansionToggle waiting for presence of expansion toggle');
        await this.driverHelper.waitPresence(OpenEditors.OPEN_EDITORS_EXPANSION_TOGGLE_LOCATOR);
        Logger.trace('OpenEditors.waitAndClickExpansionToggle expansion toggle located, clicking.');
        await this.driverHelper.waitAndClick(OpenEditors.OPEN_EDITORS_EXPANSION_TOGGLE_LOCATOR, timeout);
        Logger.trace('OpenEditors.waitAndClickExpansionToggle expansion toggle clicked.');
    }

    async isExpansionToggleCollapsed(timeout: number = TimeoutConstants.TS_OPEN_EDITORS_CLICK_ON_ITEM_TIMEOUT) : Promise<boolean> {
        Logger.debug('OpenEditors.isExpansionToggleCollapsed');
        return await this.driverHelper.waitVisibilityBoolean(OpenEditors.OPEN_EDITORS_EXPANSION_TOGGLE_COLLAPSED_LOCATOR, 1, timeout);
    }
}
