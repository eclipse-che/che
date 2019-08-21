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
            const screenshotDir = `${executionScreenCastDir}/${methodIndex}-${methodName}`;
            if (!fs.existsSync(__1.TestConstants.TS_SELENIUM_REPORT_FOLDER)) {
                fs.mkdirSync(__1.TestConstants.TS_SELENIUM_REPORT_FOLDER);
            }
            if (!fs.existsSync(executionScreenCastDir)) {
                fs.mkdirSync(executionScreenCastDir);
            }
            if (!fs.existsSync(screenshotDir)) {
                fs.mkdirSync(screenshotDir);
            }
            const date = new Date();
            const timeStamp = `(${date.getHours()}:${date.getMinutes()}:${date.getSeconds()}:${date.getMilliseconds()})`;
            const screenshotPath = `${screenshotDir}/${screenshotIndex}-${methodName}-${timeStamp}.png`;
            yield this.catcheScreen(screenshotPath);
        });
    }
    catcheScreen(screenshotPath) {
        return __awaiter(this, void 0, void 0, function* () {
            const screenshot = yield this.driverHelper.getDriver().takeScreenshot();
            const screenshotStream = fs.createWriteStream(screenshotPath);
            screenshotStream.write(new Buffer(screenshot, 'base64'));
            screenshotStream.end();
        });
    }
};
ScreenCatcher = __decorate([
    inversify_1.injectable(),
    __param(0, inversify_1.inject(inversify_types_1.CLASSES.DriverHelper)),
    __metadata("design:paramtypes", [DriverHelper_1.DriverHelper])
], ScreenCatcher);
exports.ScreenCatcher = ScreenCatcher;
