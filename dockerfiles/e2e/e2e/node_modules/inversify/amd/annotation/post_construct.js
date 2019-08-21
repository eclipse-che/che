define(["require", "exports", "../constants/error_msgs", "../constants/metadata_keys", "../planning/metadata"], function (require, exports, ERRORS_MSGS, METADATA_KEY, metadata_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
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
});
