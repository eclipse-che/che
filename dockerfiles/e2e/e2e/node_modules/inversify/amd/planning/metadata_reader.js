define(["require", "exports", "../constants/metadata_keys"], function (require, exports, METADATA_KEY) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var MetadataReader = (function () {
        function MetadataReader() {
        }
        MetadataReader.prototype.getConstructorMetadata = function (constructorFunc) {
            var compilerGeneratedMetadata = Reflect.getMetadata(METADATA_KEY.PARAM_TYPES, constructorFunc);
            var userGeneratedMetadata = Reflect.getMetadata(METADATA_KEY.TAGGED, constructorFunc);
            return {
                compilerGeneratedMetadata: compilerGeneratedMetadata,
                userGeneratedMetadata: userGeneratedMetadata || {}
            };
        };
        MetadataReader.prototype.getPropertiesMetadata = function (constructorFunc) {
            var userGeneratedMetadata = Reflect.getMetadata(METADATA_KEY.TAGGED_PROP, constructorFunc) || [];
            return userGeneratedMetadata;
        };
        return MetadataReader;
    }());
    exports.MetadataReader = MetadataReader;
});
