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
Object.defineProperty(exports, "__esModule", { value: true });
require("chromedriver");
require("reflect-metadata");
var inversify_1 = require("inversify");
var selenium_webdriver_1 = require("selenium-webdriver");
var ChromeDriver = /** @class */ (function () {
    function ChromeDriver() {
        this.driver = new selenium_webdriver_1.Builder()
            .forBrowser('chrome')
            .build();
        this.driver
            .manage()
            .window()
            .setSize(1920, 1080);
    }
    ChromeDriver.prototype.get = function () {
        return this.driver;
    };
    ChromeDriver = __decorate([
        inversify_1.injectable(),
        __metadata("design:paramtypes", [])
    ], ChromeDriver);
    return ChromeDriver;
}());
exports.ChromeDriver = ChromeDriver;
