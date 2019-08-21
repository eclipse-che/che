define(["require", "exports", "../constants/metadata_keys", "../planning/metadata", "./decorator_utils"], function (require, exports, METADATA_KEY, metadata_1, decorator_utils_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    function unmanaged() {
        return function (target, targetKey, index) {
            var metadata = new metadata_1.Metadata(METADATA_KEY.UNMANAGED_TAG, true);
            decorator_utils_1.tagParameter(target, targetKey, index, metadata);
        };
    }
    exports.unmanaged = unmanaged;
});
