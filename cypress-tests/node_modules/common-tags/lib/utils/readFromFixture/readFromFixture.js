'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = readFromFixture;

var _fs = require('fs');

var _fs2 = _interopRequireDefault(_fs);

var _node = require('when/node');

var _node2 = _interopRequireDefault(_node);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

/**
 * reads the text contents of <name>.txt in the fixtures folder
 * relative to the caller module's test file
 * @param  {String} name - the name of the fixture you want to read
 * @return {Promise<String>} - the retrieved fixture's file contents
 */
function readFromFixture(name) {
  return _node2.default.call(_fs2.default.readFile, './fixtures/' + name + '.txt', 'utf8').then(function (contents) {
    return contents.replace(/\r\n/g, '\n').trim();
  });
}
module.exports = exports['default'];
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy91dGlscy9yZWFkRnJvbUZpeHR1cmUvcmVhZEZyb21GaXh0dXJlLmpzIl0sIm5hbWVzIjpbInJlYWRGcm9tRml4dHVyZSIsIm5hbWUiLCJjYWxsIiwicmVhZEZpbGUiLCJ0aGVuIiwiY29udGVudHMiLCJyZXBsYWNlIiwidHJpbSJdLCJtYXBwaW5ncyI6IkFBQUE7Ozs7O2tCQVd3QkEsZTs7QUFUeEI7Ozs7QUFDQTs7Ozs7O0FBRUE7Ozs7OztBQU1lLFNBQVNBLGVBQVQsQ0FBMEJDLElBQTFCLEVBQWdDO0FBQzdDLFNBQU8sZUFBS0MsSUFBTCxDQUFVLGFBQUdDLFFBQWIsa0JBQXFDRixJQUFyQyxXQUFpRCxNQUFqRCxFQUNKRyxJQURJLENBQ0MsVUFBQ0MsUUFBRDtBQUFBLFdBQWNBLFNBQVNDLE9BQVQsQ0FBaUIsT0FBakIsRUFBMEIsSUFBMUIsRUFBZ0NDLElBQWhDLEVBQWQ7QUFBQSxHQURELENBQVA7QUFFRCIsImZpbGUiOiJyZWFkRnJvbUZpeHR1cmUuanMiLCJzb3VyY2VzQ29udGVudCI6WyIndXNlIHN0cmljdCdcblxuaW1wb3J0IGZzIGZyb20gJ2ZzJ1xuaW1wb3J0IG5vZGUgZnJvbSAnd2hlbi9ub2RlJ1xuXG4vKipcbiAqIHJlYWRzIHRoZSB0ZXh0IGNvbnRlbnRzIG9mIDxuYW1lPi50eHQgaW4gdGhlIGZpeHR1cmVzIGZvbGRlclxuICogcmVsYXRpdmUgdG8gdGhlIGNhbGxlciBtb2R1bGUncyB0ZXN0IGZpbGVcbiAqIEBwYXJhbSAge1N0cmluZ30gbmFtZSAtIHRoZSBuYW1lIG9mIHRoZSBmaXh0dXJlIHlvdSB3YW50IHRvIHJlYWRcbiAqIEByZXR1cm4ge1Byb21pc2U8U3RyaW5nPn0gLSB0aGUgcmV0cmlldmVkIGZpeHR1cmUncyBmaWxlIGNvbnRlbnRzXG4gKi9cbmV4cG9ydCBkZWZhdWx0IGZ1bmN0aW9uIHJlYWRGcm9tRml4dHVyZSAobmFtZSkge1xuICByZXR1cm4gbm9kZS5jYWxsKGZzLnJlYWRGaWxlLCBgLi9maXh0dXJlcy8ke25hbWV9LnR4dGAsICd1dGY4JylcbiAgICAudGhlbigoY29udGVudHMpID0+IGNvbnRlbnRzLnJlcGxhY2UoL1xcclxcbi9nLCAnXFxuJykudHJpbSgpKVxufVxuIl19