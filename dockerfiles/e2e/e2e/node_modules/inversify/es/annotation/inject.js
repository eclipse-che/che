import { UNDEFINED_INJECT_ANNOTATION } from "../constants/error_msgs";
import * as METADATA_KEY from "../constants/metadata_keys";
import { Metadata } from "../planning/metadata";
import { tagParameter, tagProperty } from "./decorator_utils";
var LazyServiceIdentifer = (function () {
    function LazyServiceIdentifer(cb) {
        this._cb = cb;
    }
    LazyServiceIdentifer.prototype.unwrap = function () {
        return this._cb();
    };
    return LazyServiceIdentifer;
}());
export { LazyServiceIdentifer };
function inject(serviceIdentifier) {
    return function (target, targetKey, index) {
        if (serviceIdentifier === undefined) {
            throw new Error(UNDEFINED_INJECT_ANNOTATION(target.name));
        }
        var metadata = new Metadata(METADATA_KEY.INJECT_TAG, serviceIdentifier);
        if (typeof index === "number") {
            tagParameter(target, targetKey, index, metadata);
        }
        else {
            tagProperty(target, targetKey, metadata);
        }
    };
}
export { inject };
