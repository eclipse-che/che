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
import { CLASSES } from '../../inversify.types';
import { DriverHelper } from '../../utils/DriverHelper';
import { By } from 'selenium-webdriver';
import { Logger } from '../../utils/Logger';
import { TimeoutConstants } from '../../TimeoutConstants';

@injectable()
export class OpenWorkspaceWidget {

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) {
    }

    async selectItemInTree(pathToItem: string, timeout: number = TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM) {
        Logger.debug(`OpenWorkspaceWidget.selectItemInTree "${pathToItem}"`);

        await this.driverHelper.waitAndClick(By.id(pathToItem), timeout);
    }

    async expandTreeToPath(path: string, timeout: number = TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM): Promise<any> {
        Logger.debug(`OpenWorkspaceWidget.expandTreeToPath "${path}"`);
        const pathNodes: string[] = path.split('/');
        let buildPath: string = '';

        const promises = pathNodes.map(async currentPath => {
            buildPath += `/${currentPath}`;
            await  this.driverHelper.waitAndClick(By.id(buildPath), timeout);
        });

        return Promise.all(promises);
    }
}
