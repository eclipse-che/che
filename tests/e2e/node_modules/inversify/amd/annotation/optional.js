define(["require", "exports", "../constants/metadata_keys", "../planning/metadata", "./decorator_utils"], function (require, exports, METADATA_KEY, metadata_1, decorator_utils_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
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
});
