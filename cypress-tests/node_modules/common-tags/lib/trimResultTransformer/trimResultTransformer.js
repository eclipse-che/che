'use strict';

/**
 * TemplateTag transformer that trims whitespace on the end result of a tagged template
 * @param  {String} side = '' - The side of the string to trim. Can be 'left' or 'right'
 * @return {Object}           - a TemplateTag transformer
 */

Object.defineProperty(exports, "__esModule", {
  value: true
});
var trimResultTransformer = function trimResultTransformer() {
  var side = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : '';
  return {
    onEndResult: function onEndResult(endResult) {
      side = side.toLowerCase();
      // uppercase the first letter of side value
      if (side === 'left' || side === 'right') {
        side = side.charAt(0).toUpperCase() + side.slice(1);
      } else if (side !== '') {
        throw new Error('Side not supported: ' + side);
      }
      return endResult['trim' + side]();
    }
  };
};

exports.default = trimResultTransformer;
module.exports = exports['default'];
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uL3NyYy90cmltUmVzdWx0VHJhbnNmb3JtZXIvdHJpbVJlc3VsdFRyYW5zZm9ybWVyLmpzIl0sIm5hbWVzIjpbInRyaW1SZXN1bHRUcmFuc2Zvcm1lciIsInNpZGUiLCJvbkVuZFJlc3VsdCIsImVuZFJlc3VsdCIsInRvTG93ZXJDYXNlIiwiY2hhckF0IiwidG9VcHBlckNhc2UiLCJzbGljZSIsIkVycm9yIl0sIm1hcHBpbmdzIjoiQUFBQTs7QUFFQTs7Ozs7Ozs7O0FBS0EsSUFBTUEsd0JBQXdCLFNBQXhCQSxxQkFBd0I7QUFBQSxNQUFDQyxJQUFELHVFQUFRLEVBQVI7QUFBQSxTQUFnQjtBQUM1Q0MsZUFENEMsdUJBQy9CQyxTQUQrQixFQUNwQjtBQUN0QkYsYUFBT0EsS0FBS0csV0FBTCxFQUFQO0FBQ0E7QUFDQSxVQUFJSCxTQUFTLE1BQVQsSUFBbUJBLFNBQVMsT0FBaEMsRUFBeUM7QUFDdkNBLGVBQU9BLEtBQUtJLE1BQUwsQ0FBWSxDQUFaLEVBQWVDLFdBQWYsS0FBK0JMLEtBQUtNLEtBQUwsQ0FBVyxDQUFYLENBQXRDO0FBQ0QsT0FGRCxNQUVPLElBQUlOLFNBQVMsRUFBYixFQUFpQjtBQUN0QixjQUFNLElBQUlPLEtBQUosMEJBQWlDUCxJQUFqQyxDQUFOO0FBQ0Q7QUFDRCxhQUFPRSxtQkFBaUJGLElBQWpCLEdBQVA7QUFDRDtBQVYyQyxHQUFoQjtBQUFBLENBQTlCOztrQkFhZUQscUIiLCJmaWxlIjoidHJpbVJlc3VsdFRyYW5zZm9ybWVyLmpzIiwic291cmNlc0NvbnRlbnQiOlsiJ3VzZSBzdHJpY3QnXG5cbi8qKlxuICogVGVtcGxhdGVUYWcgdHJhbnNmb3JtZXIgdGhhdCB0cmltcyB3aGl0ZXNwYWNlIG9uIHRoZSBlbmQgcmVzdWx0IG9mIGEgdGFnZ2VkIHRlbXBsYXRlXG4gKiBAcGFyYW0gIHtTdHJpbmd9IHNpZGUgPSAnJyAtIFRoZSBzaWRlIG9mIHRoZSBzdHJpbmcgdG8gdHJpbS4gQ2FuIGJlICdsZWZ0JyBvciAncmlnaHQnXG4gKiBAcmV0dXJuIHtPYmplY3R9ICAgICAgICAgICAtIGEgVGVtcGxhdGVUYWcgdHJhbnNmb3JtZXJcbiAqL1xuY29uc3QgdHJpbVJlc3VsdFRyYW5zZm9ybWVyID0gKHNpZGUgPSAnJykgPT4gKHtcbiAgb25FbmRSZXN1bHQgKGVuZFJlc3VsdCkge1xuICAgIHNpZGUgPSBzaWRlLnRvTG93ZXJDYXNlKClcbiAgICAvLyB1cHBlcmNhc2UgdGhlIGZpcnN0IGxldHRlciBvZiBzaWRlIHZhbHVlXG4gICAgaWYgKHNpZGUgPT09ICdsZWZ0JyB8fCBzaWRlID09PSAncmlnaHQnKSB7XG4gICAgICBzaWRlID0gc2lkZS5jaGFyQXQoMCkudG9VcHBlckNhc2UoKSArIHNpZGUuc2xpY2UoMSlcbiAgICB9IGVsc2UgaWYgKHNpZGUgIT09ICcnKSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoYFNpZGUgbm90IHN1cHBvcnRlZDogJHtzaWRlfWApXG4gICAgfVxuICAgIHJldHVybiBlbmRSZXN1bHRbYHRyaW0ke3NpZGV9YF0oKVxuICB9XG59KVxuXG5leHBvcnQgZGVmYXVsdCB0cmltUmVzdWx0VHJhbnNmb3JtZXJcbiJdfQ==