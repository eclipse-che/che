define(["require", "exports", "../constants/error_msgs", "../constants/metadata_keys"], function (require, exports, ERRORS_MSGS, METADATA_KEY) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    function injectable() {
        return function (target) {
            if (Reflect.hasOwnMetadata(METADATA_KEY.PARAM_TYPES, target)) {
                throw new Error(ERRORS_MSGS.DUPLICATED_INJECTABLE_DECORATOR);
            }
            var types = Reflect.getMetadata(METADATA_KEY.DESIGN_PARAM_TYPES, target) || [];
            Reflect.defineMetadata(METADATA_KEY.PARAM_TYPES, types, target);
            return target;
        };
    }
    exports.injectable = injectable;
});
