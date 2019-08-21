"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var METADATA_KEY = require("../constants/metadata_keys");
var metadata_1 = require("../planning/metadata");
var decorator_utils_1 = require("./decorator_utils");
function optional() {
    return function (target, targetKey, index) {
        var metadata = new metadata_1.Metadata(METADATA_KEY.OPTIONAL_TAG, true);
        if (typeof index === "number") {
            decorator_utils_1.tagParameter(target, targetKey, index, metadata);
        }
        else {
            decorator_utils_1.tagProperty(target, targetKey, metadata);
        }
    };
}
exports.optional = optional;
