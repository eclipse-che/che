"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var __param = (this && this.__param) || function (paramIndex, decorator) {
    return function (target, key) { decorator(target, key, paramIndex); }
};
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : new P(function (resolve) { resolve(result.value); }).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __importStar = (this && this.__importStar) || function (mod) {
    if (mod && mod.__esModule) return mod;
    var result = {};
    if (mod != null) for (var k in mod) if (Object.hasOwnProperty.call(mod, k)) result[k] = mod[k];
    result["default"] = mod;
    return result;
};
Object.defineProperty(exports, "__esModule", { value: true });
/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
const fs = __importStar(require("fs"));
const inversify_1 = require("inversify");
const inversify_types_1 = require("../inversify.types");
const DriverHelper_1 = require("./DriverHelper");
const __1 = require("..");
let ScreenCatcher = class ScreenCatcher {
    constructor(driverHelper) {
        this.driverHelper = driverHelper;
    }
    catchMethodScreen(methodName, methodIndex, screenshotIndex) {
        return __awaiter(this, void 0, void 0, function* () {
            const executionScreenCastDir = `${__1.TestConstants.TS_SELENIUM_REPORT_FOLDER}/executionScreencast`;
            const executionScreenCastErrorsDir = `${__1.TestConstants.TS_SELENIUM_REPORT_FOLDER}/executionScreencastErrors`;
            const formattedMethodIndex = new Intl.NumberFormat('en-us', { minimumIntegerDigits: 3 }).format(methodIndex);
            const formattedScreenshotIndex = new Intl.NumberFormat('en-us', { minimumIntegerDigits: 5 }).format(screenshotIndex).replace(/,/g, '');
            if (!fs.existsSync(__1.TestConstants.TS_SELENIUM_REPORT_FOLDER)) {
                fs.mkdirSync(__1.TestConstants.TS_SELENIUM_REPORT_FOLDER);
            }
            if (!fs.existsSync(executionScreenCastDir)) {
                fs.mkdirSync(executionScreenCastDir);
            }
            const date = new Date();
            const timeStr = date.toLocaleTimeString('en-us', { hour12: false }) + '.' + new Intl.NumberFormat('en-us', { minimumIntegerDigits: 3 }).format(date.getMilliseconds());
            const screenshotPath = `${executionScreenCastDir}/${formattedMethodIndex}${formattedScreenshotIndex}--(${timeStr}): ${methodName}.png`;
            try {
                yield this.catchScreen(screenshotPath);
            }
            catch (err) {
                if (!fs.existsSync(executionScreenCastErrorsDir)) {
                    fs.mkdirSync(executionScreenCastErrorsDir);
                }
                let errorLogFilePath = screenshotPath.replace('.png', '.txt');
                errorLogFilePath = errorLogFilePath.replace(executionScreenCastDir, executionScreenCastErrorsDir);
                yield this.writeErrorLog(errorLogFilePath, err);
            }
        });
    }
    catchScreen(screenshotPath) {
        return __awaiter(this, void 0, void 0, function* () {
            const screenshot = yield this.driverHelper.getDriver().takeScreenshot();
            const screenshotStream = fs.createWriteStream(screenshotPath);
            screenshotStream.write(new Buffer(screenshot, 'base64'));
            screenshotStream.end();
        });
    }
    writeErrorLog(errorLogPath, err) {
        return __awaiter(this, void 0, void 0, function* () {
            console.log(`Failed to save screenshot, additional information in the ${errorLogPath}`);
            if (err.stack) {
                const screenshotStream = fs.createWriteStream(errorLogPath);
                screenshotStream.write(new Buffer(err.stack, 'utf8'));
                screenshotStream.end();
            }
        });
    }
};
ScreenCatcher = __decorate([
    inversify_1.injectable(),
    __param(0, inversify_1.inject(inversify_types_1.CLASSES.DriverHelper)),
    __metadata("design:paramtypes", [DriverHelper_1.DriverHelper])
], ScreenCatcher);
exports.ScreenCatcher = ScreenCatcher;
//# sourceMappingURL=ScreenCatcher.js.map