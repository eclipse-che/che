"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var inversify_config_1 = require("./inversify.config");
var types_1 = require("./types");
var driver = inversify_config_1.e2eContainer.get(types_1.TYPES.Driver);
function doNavigation() {
    driver.get()
        .navigate()
        .to("https://google.com");
}
doNavigation();
