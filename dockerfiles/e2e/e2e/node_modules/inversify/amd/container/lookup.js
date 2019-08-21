define(["require", "exports", "../constants/error_msgs"], function (require, exports, ERROR_MSGS) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var Lookup = (function () {
        function Lookup() {
            this._map = new Map();
        }
        Lookup.prototype.getMap = function () {
            return this._map;
        };
        Lookup.prototype.add = function (serviceIdentifier, value) {
            if (serviceIdentifier === null || serviceIdentifier === undefined) {
                throw new Error(ERROR_MSGS.NULL_ARGUMENT);
            }
            if (value === null || value === undefined) {
                throw new Error(ERROR_MSGS.NULL_ARGUMENT);
            }
            var entry = this._map.get(serviceIdentifier);
            if (entry !== undefined) {
                entry.push(value);
                this._map.set(serviceIdentifier, entry);
            }
            else {
                this._map.set(serviceIdentifier, [value]);
            }
        };
        Lookup.prototype.get = function (serviceIdentifier) {
            if (serviceIdentifier === null || serviceIdentifier === undefined) {
                throw new Error(ERROR_MSGS.NULL_ARGUMENT);
            }
            var entry = this._map.get(serviceIdentifier);
            if (entry !== undefined) {
                return entry;
            }
            else {
                throw new Error(ERROR_MSGS.KEY_NOT_FOUND);
            }
        };
        Lookup.prototype.remove = function (serviceIdentifier) {
            if (serviceIdentifier === null || serviceIdentifier === undefined) {
                throw new Error(ERROR_MSGS.NULL_ARGUMENT);
            }
            if (!this._map.delete(serviceIdentifier)) {
                throw new Error(ERROR_MSGS.KEY_NOT_FOUND);
            }
        };
        Lookup.prototype.removeByCondition = function (condition) {
            var _this = this;
            this._map.forEach(function (entries, key) {
                var updatedEntries = entries.filter(function (entry) { return !condition(entry); });
                if (updatedEntries.length > 0) {
                    _this._map.set(key, updatedEntries);
                }
                else {
                    _this._map.delete(key);
                }
            });
        };
        Lookup.prototype.hasKey = function (serviceIdentifier) {
            if (serviceIdentifier === null || serviceIdentifier === undefined) {
                throw new Error(ERROR_MSGS.NULL_ARGUMENT);
            }
            return this._map.has(serviceIdentifier);
        };
        Lookup.prototype.clone = function () {
            var copy = new Lookup();
            this._map.forEach(function (value, key) {
                value.forEach(function (b) { return copy.add(key, b.clone()); });
            });
            return copy;
        };
        Lookup.prototype.traverse = function (func) {
            this._map.forEach(function (value, key) {
                func(key, value);
            });
        };
        return Lookup;
    }());
    exports.Lookup = Lookup;
});
