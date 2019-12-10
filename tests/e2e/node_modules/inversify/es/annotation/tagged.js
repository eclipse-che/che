import { Metadata } from "../planning/metadata";
import { tagParameter, tagProperty } from "./decorator_utils";
function tagged(metadataKey, metadataValue) {
    return function (target, targetKey, index) {
        var metadata = new Metadata(metadataKey, metadataValue);
        if (typeof index === "number") {
            tagParameter(target, targetKey, index, metadata);
        }
        else {
            tagProperty(target, targetKey, metadata);
        }
    };
}
export { tagged };
