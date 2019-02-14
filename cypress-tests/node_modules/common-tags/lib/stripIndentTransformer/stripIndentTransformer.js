'use strict';

/**
 * strips indentation from a template literal
 * @param  {String} type = 'initial' - whether to remove all indentation or just leading indentation. can be 'all' or 'initial'
 * @return {Object}                  - a TemplateTag transformer
 */

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _toConsumableArray2 = require('babel-runtime/helpers/toConsumableArray');

var _toConsumableArray3 = _interopRequireDefault(_toConsumableArray2);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var stripIndentTransformer = function stripIndentTransformer() {
  var type = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 'initial';
  return {
    onEndResult: function onEndResult(endResult) {
      if (type === 'initial') {
        // remove the shortest leading indentation from each line
        var match = endResult.match(/^[ \t]*(?=\S)/gm);
        // return early if there's nothing to strip
        if (match === null) {
          return endResult;
        }
        var indent = Math.min.apply(Math, (0, _toConsumableArray3.default)(match.map(function (el) {
          return el.length;
        })));
        var regexp = new RegExp('^[ \\t]{' + indent + '}', 'gm');
        endResult = indent > 0 ? endResult.replace(regexp, '') : endResult;
      } else if (type === 'all') {
        // remove all indentation from each line
        endResult = endResult.split('\n').map(function (line) {
          return line.trimLeft();
        }).join('\n');
      } else {
        throw new Error('Unknown type: ' + type);
      }
      return endResult;
    }
  };
};

exports.default = stripIndentTransformer;
module.exports = exports['default'];
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uL3NyYy9zdHJpcEluZGVudFRyYW5zZm9ybWVyL3N0cmlwSW5kZW50VHJhbnNmb3JtZXIuanMiXSwibmFtZXMiOlsic3RyaXBJbmRlbnRUcmFuc2Zvcm1lciIsInR5cGUiLCJvbkVuZFJlc3VsdCIsImVuZFJlc3VsdCIsIm1hdGNoIiwiaW5kZW50IiwiTWF0aCIsIm1pbiIsIm1hcCIsImVsIiwibGVuZ3RoIiwicmVnZXhwIiwiUmVnRXhwIiwicmVwbGFjZSIsInNwbGl0IiwibGluZSIsInRyaW1MZWZ0Iiwiam9pbiIsIkVycm9yIl0sIm1hcHBpbmdzIjoiQUFBQTs7QUFFQTs7Ozs7Ozs7Ozs7Ozs7OztBQUtBLElBQU1BLHlCQUF5QixTQUF6QkEsc0JBQXlCO0FBQUEsTUFBQ0MsSUFBRCx1RUFBUSxTQUFSO0FBQUEsU0FBdUI7QUFDcERDLGVBRG9ELHVCQUN2Q0MsU0FEdUMsRUFDNUI7QUFDdEIsVUFBSUYsU0FBUyxTQUFiLEVBQXdCO0FBQ3RCO0FBQ0EsWUFBTUcsUUFBUUQsVUFBVUMsS0FBVixDQUFnQixpQkFBaEIsQ0FBZDtBQUNBO0FBQ0EsWUFBSUEsVUFBVSxJQUFkLEVBQW9CO0FBQ2xCLGlCQUFPRCxTQUFQO0FBQ0Q7QUFDRCxZQUFNRSxTQUFTQyxLQUFLQyxHQUFMLDhDQUFZSCxNQUFNSSxHQUFOLENBQVU7QUFBQSxpQkFBTUMsR0FBR0MsTUFBVDtBQUFBLFNBQVYsQ0FBWixFQUFmO0FBQ0EsWUFBTUMsU0FBUyxJQUFJQyxNQUFKLENBQVcsYUFBYVAsTUFBYixHQUFzQixHQUFqQyxFQUFzQyxJQUF0QyxDQUFmO0FBQ0FGLG9CQUFZRSxTQUFTLENBQVQsR0FBYUYsVUFBVVUsT0FBVixDQUFrQkYsTUFBbEIsRUFBMEIsRUFBMUIsQ0FBYixHQUE2Q1IsU0FBekQ7QUFDRCxPQVZELE1BVU8sSUFBSUYsU0FBUyxLQUFiLEVBQW9CO0FBQ3pCO0FBQ0FFLG9CQUFZQSxVQUFVVyxLQUFWLENBQWdCLElBQWhCLEVBQXNCTixHQUF0QixDQUEwQjtBQUFBLGlCQUFRTyxLQUFLQyxRQUFMLEVBQVI7QUFBQSxTQUExQixFQUFtREMsSUFBbkQsQ0FBd0QsSUFBeEQsQ0FBWjtBQUNELE9BSE0sTUFHQTtBQUNMLGNBQU0sSUFBSUMsS0FBSixvQkFBMkJqQixJQUEzQixDQUFOO0FBQ0Q7QUFDRCxhQUFPRSxTQUFQO0FBQ0Q7QUFuQm1ELEdBQXZCO0FBQUEsQ0FBL0I7O2tCQXNCZUgsc0IiLCJmaWxlIjoic3RyaXBJbmRlbnRUcmFuc2Zvcm1lci5qcyIsInNvdXJjZXNDb250ZW50IjpbIid1c2Ugc3RyaWN0J1xuXG4vKipcbiAqIHN0cmlwcyBpbmRlbnRhdGlvbiBmcm9tIGEgdGVtcGxhdGUgbGl0ZXJhbFxuICogQHBhcmFtICB7U3RyaW5nfSB0eXBlID0gJ2luaXRpYWwnIC0gd2hldGhlciB0byByZW1vdmUgYWxsIGluZGVudGF0aW9uIG9yIGp1c3QgbGVhZGluZyBpbmRlbnRhdGlvbi4gY2FuIGJlICdhbGwnIG9yICdpbml0aWFsJ1xuICogQHJldHVybiB7T2JqZWN0fSAgICAgICAgICAgICAgICAgIC0gYSBUZW1wbGF0ZVRhZyB0cmFuc2Zvcm1lclxuICovXG5jb25zdCBzdHJpcEluZGVudFRyYW5zZm9ybWVyID0gKHR5cGUgPSAnaW5pdGlhbCcpID0+ICh7XG4gIG9uRW5kUmVzdWx0IChlbmRSZXN1bHQpIHtcbiAgICBpZiAodHlwZSA9PT0gJ2luaXRpYWwnKSB7XG4gICAgICAvLyByZW1vdmUgdGhlIHNob3J0ZXN0IGxlYWRpbmcgaW5kZW50YXRpb24gZnJvbSBlYWNoIGxpbmVcbiAgICAgIGNvbnN0IG1hdGNoID0gZW5kUmVzdWx0Lm1hdGNoKC9eWyBcXHRdKig/PVxcUykvZ20pXG4gICAgICAvLyByZXR1cm4gZWFybHkgaWYgdGhlcmUncyBub3RoaW5nIHRvIHN0cmlwXG4gICAgICBpZiAobWF0Y2ggPT09IG51bGwpIHtcbiAgICAgICAgcmV0dXJuIGVuZFJlc3VsdFxuICAgICAgfVxuICAgICAgY29uc3QgaW5kZW50ID0gTWF0aC5taW4oLi4ubWF0Y2gubWFwKGVsID0+IGVsLmxlbmd0aCkpXG4gICAgICBjb25zdCByZWdleHAgPSBuZXcgUmVnRXhwKCdeWyBcXFxcdF17JyArIGluZGVudCArICd9JywgJ2dtJylcbiAgICAgIGVuZFJlc3VsdCA9IGluZGVudCA+IDAgPyBlbmRSZXN1bHQucmVwbGFjZShyZWdleHAsICcnKSA6IGVuZFJlc3VsdFxuICAgIH0gZWxzZSBpZiAodHlwZSA9PT0gJ2FsbCcpIHtcbiAgICAgIC8vIHJlbW92ZSBhbGwgaW5kZW50YXRpb24gZnJvbSBlYWNoIGxpbmVcbiAgICAgIGVuZFJlc3VsdCA9IGVuZFJlc3VsdC5zcGxpdCgnXFxuJykubWFwKGxpbmUgPT4gbGluZS50cmltTGVmdCgpKS5qb2luKCdcXG4nKVxuICAgIH0gZWxzZSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoYFVua25vd24gdHlwZTogJHt0eXBlfWApXG4gICAgfVxuICAgIHJldHVybiBlbmRSZXN1bHRcbiAgfVxufSlcblxuZXhwb3J0IGRlZmF1bHQgc3RyaXBJbmRlbnRUcmFuc2Zvcm1lclxuIl19