'use strict';

/**
 * TemplateTag transformer that trims whitespace on the end result of a tagged template
 * @param  {String} side = '' - The side of the string to trim. Can be 'left' or 'right'
 * @return {Object}           - a TemplateTag transformer
 */

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

export default trimResultTransformer;
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uL3NyYy90cmltUmVzdWx0VHJhbnNmb3JtZXIvdHJpbVJlc3VsdFRyYW5zZm9ybWVyLmpzIl0sIm5hbWVzIjpbInRyaW1SZXN1bHRUcmFuc2Zvcm1lciIsInNpZGUiLCJvbkVuZFJlc3VsdCIsImVuZFJlc3VsdCIsInRvTG93ZXJDYXNlIiwiY2hhckF0IiwidG9VcHBlckNhc2UiLCJzbGljZSIsIkVycm9yIl0sIm1hcHBpbmdzIjoiQUFBQTs7QUFFQTs7Ozs7O0FBS0EsSUFBTUEsd0JBQXdCLFNBQXhCQSxxQkFBd0I7QUFBQSxNQUFDQyxJQUFELHVFQUFRLEVBQVI7QUFBQSxTQUFnQjtBQUM1Q0MsZUFENEMsdUJBQy9CQyxTQUQrQixFQUNwQjtBQUN0QkYsYUFBT0EsS0FBS0csV0FBTCxFQUFQO0FBQ0E7QUFDQSxVQUFJSCxTQUFTLE1BQVQsSUFBbUJBLFNBQVMsT0FBaEMsRUFBeUM7QUFDdkNBLGVBQU9BLEtBQUtJLE1BQUwsQ0FBWSxDQUFaLEVBQWVDLFdBQWYsS0FBK0JMLEtBQUtNLEtBQUwsQ0FBVyxDQUFYLENBQXRDO0FBQ0QsT0FGRCxNQUVPLElBQUlOLFNBQVMsRUFBYixFQUFpQjtBQUN0QixjQUFNLElBQUlPLEtBQUosMEJBQWlDUCxJQUFqQyxDQUFOO0FBQ0Q7QUFDRCxhQUFPRSxtQkFBaUJGLElBQWpCLEdBQVA7QUFDRDtBQVYyQyxHQUFoQjtBQUFBLENBQTlCOztBQWFBLGVBQWVELHFCQUFmIiwiZmlsZSI6InRyaW1SZXN1bHRUcmFuc2Zvcm1lci5qcyIsInNvdXJjZXNDb250ZW50IjpbIid1c2Ugc3RyaWN0J1xuXG4vKipcbiAqIFRlbXBsYXRlVGFnIHRyYW5zZm9ybWVyIHRoYXQgdHJpbXMgd2hpdGVzcGFjZSBvbiB0aGUgZW5kIHJlc3VsdCBvZiBhIHRhZ2dlZCB0ZW1wbGF0ZVxuICogQHBhcmFtICB7U3RyaW5nfSBzaWRlID0gJycgLSBUaGUgc2lkZSBvZiB0aGUgc3RyaW5nIHRvIHRyaW0uIENhbiBiZSAnbGVmdCcgb3IgJ3JpZ2h0J1xuICogQHJldHVybiB7T2JqZWN0fSAgICAgICAgICAgLSBhIFRlbXBsYXRlVGFnIHRyYW5zZm9ybWVyXG4gKi9cbmNvbnN0IHRyaW1SZXN1bHRUcmFuc2Zvcm1lciA9IChzaWRlID0gJycpID0+ICh7XG4gIG9uRW5kUmVzdWx0IChlbmRSZXN1bHQpIHtcbiAgICBzaWRlID0gc2lkZS50b0xvd2VyQ2FzZSgpXG4gICAgLy8gdXBwZXJjYXNlIHRoZSBmaXJzdCBsZXR0ZXIgb2Ygc2lkZSB2YWx1ZVxuICAgIGlmIChzaWRlID09PSAnbGVmdCcgfHwgc2lkZSA9PT0gJ3JpZ2h0Jykge1xuICAgICAgc2lkZSA9IHNpZGUuY2hhckF0KDApLnRvVXBwZXJDYXNlKCkgKyBzaWRlLnNsaWNlKDEpXG4gICAgfSBlbHNlIGlmIChzaWRlICE9PSAnJykge1xuICAgICAgdGhyb3cgbmV3IEVycm9yKGBTaWRlIG5vdCBzdXBwb3J0ZWQ6ICR7c2lkZX1gKVxuICAgIH1cbiAgICByZXR1cm4gZW5kUmVzdWx0W2B0cmltJHtzaWRlfWBdKClcbiAgfVxufSlcblxuZXhwb3J0IGRlZmF1bHQgdHJpbVJlc3VsdFRyYW5zZm9ybWVyXG4iXX0=