define(["require", "exports", "../constants/metadata_keys", "../utils/id", "./metadata", "./queryable_string"], function (require, exports, METADATA_KEY, id_1, metadata_1, queryable_string_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var Target = (function () {
        function Target(type, name, serviceIdentifier, namedOrTagged) {
            this.id = id_1.id();
            this.type = type;
            this.serviceIdentifier = serviceIdentifier;
            this.name = new queryable_string_1.QueryableString(name || "");
            this.metadata = new Array();
            var metadataItem = null;
            if (typeof namedOrTagged === "string") {
                metadataItem = new metadata_1.Metadata(METADATA_KEY.NAMED_TAG, namedOrTagged);
            }
            else if (namedOrTagged instanceof metadata_1.Metadata) {
                metadataItem = namedOrTagged;
            }
            if (metadataItem !== null) {
                this.metadata.push(metadataItem);
            }
        }
        Target.prototype.hasTag = function (key) {
            for (var _i = 0, _a = this.metadata; _i < _a.length; _i++) {
                var m = _a[_i];
                if (m.key === key) {
                    return true;
                }
            }
            return false;
        };
        Target.prototype.isArray = function () {
            return this.hasTag(METADATA_KEY.MULTI_INJECT_TAG);
        };
        Target.prototype.matchesArray = function (name) {
            return this.matchesTag(METADATA_KEY.MULTI_INJECT_TAG)(name);
        };
        Target.prototype.isNamed = function () {
            return this.hasTag(METADATA_KEY.NAMED_TAG);
        };
        Target.prototype.isTagged = function () {
            return this.metadata.some(function (m) {
                return (m.key !== METADATA_KEY.INJECT_TAG) &&
                    (m.key !== METADATA_KEY.MULTI_INJECT_TAG) &&
                    (m.key !== METADATA_KEY.NAME_TAG) &&
                    (m.key !== METADATA_KEY.UNMANAGED_TAG) &&
                    (m.key !== METADATA_KEY.NAMED_TAG);
            });
        };
        Target.prototype.isOptional = function () {
            return this.matchesTag(METADATA_KEY.OPTIONAL_TAG)(true);
        };
        Target.prototype.getNamedTag = function () {
            if (this.isNamed()) {
                return this.metadata.filter(function (m) { return m.key === METADATA_KEY.NAMED_TAG; })[0];
            }
            return null;
        };
        Target.prototype.getCustomTags = function () {
            if (this.isTagged()) {
                return this.metadata.filter(function (m) {
                    return (m.key !== METADATA_KEY.INJECT_TAG) &&
                        (m.key !== METADATA_KEY.MULTI_INJECT_TAG) &&
                        (m.key !== METADATA_KEY.NAME_TAG) &&
                        (m.key !== METADATA_KEY.UNMANAGED_TAG) &&
                        (m.key !== METADATA_KEY.NAMED_TAG);
                });
            }
            return null;
        };
        Target.prototype.matchesNamedTag = function (name) {
            return this.matchesTag(METADATA_KEY.NAMED_TAG)(name);
        };
        Target.prototype.matchesTag = function (key) {
            var _this = this;
            return function (value) {
                for (var _i = 0, _a = _this.metadata; _i < _a.length; _i++) {
                    var m = _a[_i];
                    if (m.key === key && m.value === value) {
                        return true;
                    }
                }
                return false;
            };
        };
        return Target;
    }());
    exports.Target = Target;
});
