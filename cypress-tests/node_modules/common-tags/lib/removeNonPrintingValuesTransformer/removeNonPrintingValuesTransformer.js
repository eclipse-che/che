'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _isNan = require('babel-runtime/core-js/number/is-nan');

var _isNan2 = _interopRequireDefault(_isNan);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var isValidValue = function isValidValue(x) {
  return x != null && !(0, _isNan2.default)(x) && typeof x !== 'boolean';
};

var removeNonPrintingValuesTransformer = function removeNonPrintingValuesTransformer() {
  return {
    onSubstitution: function onSubstitution(substitution) {
      if (Array.isArray(substitution)) {
        return substitution.filter(isValidValue);
      }
      if (isValidValue(substitution)) {
        return substitution;
      }
      return '';
    }
  };
};

exports.default = removeNonPrintingValuesTransformer;
module.exports = exports['default'];
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uL3NyYy9yZW1vdmVOb25QcmludGluZ1ZhbHVlc1RyYW5zZm9ybWVyL3JlbW92ZU5vblByaW50aW5nVmFsdWVzVHJhbnNmb3JtZXIuanMiXSwibmFtZXMiOlsiaXNWYWxpZFZhbHVlIiwieCIsInJlbW92ZU5vblByaW50aW5nVmFsdWVzVHJhbnNmb3JtZXIiLCJvblN1YnN0aXR1dGlvbiIsInN1YnN0aXR1dGlvbiIsIkFycmF5IiwiaXNBcnJheSIsImZpbHRlciJdLCJtYXBwaW5ncyI6IkFBQUE7Ozs7Ozs7Ozs7OztBQUVBLElBQU1BLGVBQWUsU0FBZkEsWUFBZSxDQUFDQyxDQUFEO0FBQUEsU0FDbkJBLEtBQUssSUFBTCxJQUNBLENBQUMscUJBQWFBLENBQWIsQ0FERCxJQUVBLE9BQU9BLENBQVAsS0FBYSxTQUhNO0FBQUEsQ0FBckI7O0FBS0EsSUFBTUMscUNBQXFDLFNBQXJDQSxrQ0FBcUM7QUFBQSxTQUFPO0FBQ2hEQyxrQkFEZ0QsMEJBQ2hDQyxZQURnQyxFQUNsQjtBQUM1QixVQUFJQyxNQUFNQyxPQUFOLENBQWNGLFlBQWQsQ0FBSixFQUFpQztBQUMvQixlQUFPQSxhQUFhRyxNQUFiLENBQW9CUCxZQUFwQixDQUFQO0FBQ0Q7QUFDRCxVQUFJQSxhQUFhSSxZQUFiLENBQUosRUFBZ0M7QUFDOUIsZUFBT0EsWUFBUDtBQUNEO0FBQ0QsYUFBTyxFQUFQO0FBQ0Q7QUFUK0MsR0FBUDtBQUFBLENBQTNDOztrQkFZZUYsa0MiLCJmaWxlIjoicmVtb3ZlTm9uUHJpbnRpbmdWYWx1ZXNUcmFuc2Zvcm1lci5qcyIsInNvdXJjZXNDb250ZW50IjpbIid1c2Ugc3RyaWN0J1xuXG5jb25zdCBpc1ZhbGlkVmFsdWUgPSAoeCkgPT5cbiAgeCAhPSBudWxsICYmXG4gICFOdW1iZXIuaXNOYU4oeCkgJiZcbiAgdHlwZW9mIHggIT09ICdib29sZWFuJ1xuXG5jb25zdCByZW1vdmVOb25QcmludGluZ1ZhbHVlc1RyYW5zZm9ybWVyID0gKCkgPT4gKHtcbiAgb25TdWJzdGl0dXRpb24gKHN1YnN0aXR1dGlvbikge1xuICAgIGlmIChBcnJheS5pc0FycmF5KHN1YnN0aXR1dGlvbikpIHtcbiAgICAgIHJldHVybiBzdWJzdGl0dXRpb24uZmlsdGVyKGlzVmFsaWRWYWx1ZSlcbiAgICB9XG4gICAgaWYgKGlzVmFsaWRWYWx1ZShzdWJzdGl0dXRpb24pKSB7XG4gICAgICByZXR1cm4gc3Vic3RpdHV0aW9uXG4gICAgfVxuICAgIHJldHVybiAnJ1xuICB9XG59KVxuXG5leHBvcnQgZGVmYXVsdCByZW1vdmVOb25QcmludGluZ1ZhbHVlc1RyYW5zZm9ybWVyXG4iXX0=