define(["require", "exports", "../constants/error_msgs", "../constants/metadata_keys", "../planning/metadata", "./decorator_utils"], function (require, exports, error_msgs_1, METADATA_KEY, metadata_1, decorator_utils_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var LazyServiceIdentifer = (function () {
        function LazyServiceIdentifer(cb) {
            this._cb = cb;
        }
        LazyServiceIdentifer.prototype.unwrap = function () {
            return this._cb();
        };
        return LazyServiceIdentifer;
    }());
    exports.LazyServiceIdentifer = LazyServiceIdentifer;
    function inject(serviceIdentifier) {
        return function (target, targetKey, index) {
            if (serviceIdentifier === undefined) {
                throw new Error(error_msgs_1.UNDEFINED_INJECT_ANNOTATION(target.name));
            }
            var metadata = new metadata_1.Metadata(METADATA_KEY.INJECT_TAG, serviceIdentifier);
            if (typeof index === "number") {
                decorator_utils_1.tagParameter(target, targetKey, index, metadata);
            }
            else {
                decorator_utils_1.tagProperty(target, targetKey, metadata);
            }
        };
    }
    exports.inject = inject;
});
