import * as METADATA_KEY from "../constants/metadata_keys";
import { Metadata } from "../planning/metadata";
import { tagParameter, tagProperty } from "./decorator_utils";
function named(name) {
    return function (target, targetKey, index) {
        var metadata = new Metadata(METADATA_KEY.NAMED_TAG, name);
        if (typeof index === "number") {
            tagParameter(target, targetKey, index, metadata);
        }
        else {
            tagProperty(target, targetKey, metadata);
        }
    };
}
export { named };
