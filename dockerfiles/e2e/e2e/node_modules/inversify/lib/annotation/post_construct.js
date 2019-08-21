"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var ERRORS_MSGS = require("../constants/error_msgs");
var METADATA_KEY = require("../constants/metadata_keys");
var metadata_1 = require("../planning/metadata");
function postConstruct() {
    return function (target, propertyKey, descriptor) {
        var metadata = new metadata_1.Metadata(METADATA_KEY.POST_CONSTRUCT, propertyKey);
        if (Reflect.hasOwnMetadata(METADATA_KEY.POST_CONSTRUCT, target.constructor)) {
            throw new Error(ERRORS_MSGS.MULTIPLE_POST_CONSTRUCT_METHODS);
        }
        Reflect.defineMetadata(METADATA_KEY.POST_CONSTRUCT, metadata, target.constructor);
    };
}
exports.postConstruct = postConstruct;
