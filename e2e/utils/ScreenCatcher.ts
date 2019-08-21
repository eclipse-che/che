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
import { TestConstants } from '..';

@injectable()
export class ScreenCatcher {
    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async catchMethodScreen(methodName: string, methodIndex: number, screenshotIndex: number) {
        const executionScreenCastDir = `${TestConstants.TS_SELENIUM_REPORT_FOLDER}/executionScreencast`;
        const formattedMethodIndex: string = new Intl.NumberFormat('en-us', {minimumIntegerDigits: 2}).format(methodIndex);
        const screenshotDir: string = `${executionScreenCastDir}/${formattedMethodIndex}-${methodName}`;

        if (!fs.existsSync(TestConstants.TS_SELENIUM_REPORT_FOLDER)) {
            fs.mkdirSync(TestConstants.TS_SELENIUM_REPORT_FOLDER);
        }

        if (!fs.existsSync(executionScreenCastDir)) {
            fs.mkdirSync(executionScreenCastDir);
        }

        if (!fs.existsSync(screenshotDir)) {
            fs.mkdirSync(screenshotDir);
        }

        const date: Date = new Date();
        const timeStr: string = date.toLocaleTimeString('en-us', {hour12: false}) + '.' + new Intl.NumberFormat('en-us', {minimumIntegerDigits: 3}).format(date.getMilliseconds());

        const screenshotPath: string = `${screenshotDir}/${timeStr}_${formattedMethodIndex}-${methodName}.png`;

        await this.catchScreen(screenshotPath);
    }

    async catchScreen(screenshotPath: string) {
        const screenshot: string = await this.driverHelper.getDriver().takeScreenshot();
        const screenshotStream = fs.createWriteStream(screenshotPath);
        screenshotStream.write(new Buffer(screenshot, 'base64'));
        screenshotStream.end();
    }

}
