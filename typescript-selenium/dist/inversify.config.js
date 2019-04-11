"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var inversify_1 = require("inversify");
var types_1 = require("./types");
var ChromeDriver_1 = require("./driver/ChromeDriver");
var e2eContainer = new inversify_1.Container();
exports.e2eContainer = e2eContainer;
e2eContainer.bind(types_1.TYPES.Driver).to(ChromeDriver_1.ChromeDriver);
