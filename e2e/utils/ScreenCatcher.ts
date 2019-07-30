/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import * as fs from 'fs';
import { injectable, inject } from 'inversify';
import { CLASSES } from '../inversify.types';
import { DriverHelper } from './DriverHelper';

@injectable()
export class ScreenCatcher {
    private screenshotIndex: number;
    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) {
        this.screenshotIndex = 1;
    }

    async catchMethodScreen(methodName: string, startMethodScreenshot: boolean = true) {
        const screenshotIndex: number = this.screenshotIndex;
        const reportDir: string = `./report`;
        const executionScreenCastDir = `${reportDir}/executionScreencast`;
        const screenshotDir: string = `${executionScreenCastDir}/${screenshotIndex}-${methodName}`;

        let screenshotPath: string = `${screenshotDir}/${methodName}-begin.png`;

        if (!startMethodScreenshot) {
            this.iterateScreenshotIndex();
            screenshotPath = `${screenshotDir}/${methodName}-finish.png`;
        }

        if (!fs.existsSync(reportDir)) {
            fs.mkdirSync(reportDir);
        }

        if (!fs.existsSync(executionScreenCastDir)) {
            fs.mkdirSync(executionScreenCastDir);
        }

        if (!fs.existsSync(screenshotDir)) {
            fs.mkdirSync(screenshotDir);
        }

        this.catcheScreen(screenshotPath);
    }

    async catcheScreen(screenshotPath: string) {
        const screenshot: string = await this.driverHelper.getDriver().takeScreenshot();
        const screenshotStream = fs.createWriteStream(screenshotPath);
        screenshotStream.write(new Buffer(screenshot, 'base64'));
        screenshotStream.end();
    }

    private iterateScreenshotIndex() {
        this.screenshotIndex = this.screenshotIndex + 1;
    }

}
