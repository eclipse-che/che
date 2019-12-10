define(["require", "exports", "../planning/metadata", "./decorator_utils"], function (require, exports, metadata_1, decorator_utils_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    function tagged(metadataKey, metadataValue) {
        return function (target, targetKey, index) {
            var metadata = new metadata_1.Metadata(metadataKey, metadataValue);
            if (typeof index === "number") {
                decorator_utils_1.tagParameter(target, targetKey, index, metadata);
            }
            else {
                decorator_utils_1.tagProperty(target, targetKey, metadata);
            }
        };
    }
    exports.tagged = tagged;
});
