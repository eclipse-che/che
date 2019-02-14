'use strict';

var defaults = {
  separator: '',
  conjunction: '',
  serial: false
};

/**
 * Converts an array substitution to a string containing a list
 * @param  {String} [opts.separator = ''] - the character that separates each item
 * @param  {String} [opts.conjunction = '']  - replace the last separator with this
 * @param  {Boolean} [opts.serial = false] - include the separator before the conjunction? (Oxford comma use-case)
 *
 * @return {Object}                     - a TemplateTag transformer
 */
var inlineArrayTransformer = function inlineArrayTransformer() {
  var opts = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : defaults;
  return {
    onSubstitution: function onSubstitution(substitution, resultSoFar) {
      // only operate on arrays
      if (Array.isArray(substitution)) {
        var separator = opts.separator;
        var conjunction = opts.conjunction;
        var serial = opts.serial;
        // join each item in the array into a string where each item is separated by separator
        // be sure to maintain indentation
        var indent = resultSoFar.match(/(\s+)$/);
        if (indent) {
          substitution = substitution.join(separator + indent[1]);
        } else {
          substitution = substitution.join(separator + ' ');
        }
        // if conjunction is set, replace the last separator with conjunction
        if (conjunction) {
          var separatorIndex = substitution.lastIndexOf(separator);
          substitution = substitution.substr(0, separatorIndex) + (serial ? separator : '') + ' ' + conjunction + substitution.substr(separatorIndex + 1);
        }
      }
      return substitution;
    }
  };
};

export default inlineArrayTransformer;
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uL3NyYy9pbmxpbmVBcnJheVRyYW5zZm9ybWVyL2lubGluZUFycmF5VHJhbnNmb3JtZXIuanMiXSwibmFtZXMiOlsiZGVmYXVsdHMiLCJzZXBhcmF0b3IiLCJjb25qdW5jdGlvbiIsInNlcmlhbCIsImlubGluZUFycmF5VHJhbnNmb3JtZXIiLCJvcHRzIiwib25TdWJzdGl0dXRpb24iLCJzdWJzdGl0dXRpb24iLCJyZXN1bHRTb0ZhciIsIkFycmF5IiwiaXNBcnJheSIsImluZGVudCIsIm1hdGNoIiwiam9pbiIsInNlcGFyYXRvckluZGV4IiwibGFzdEluZGV4T2YiLCJzdWJzdHIiXSwibWFwcGluZ3MiOiJBQUFBOztBQUVBLElBQU1BLFdBQVc7QUFDZkMsYUFBVyxFQURJO0FBRWZDLGVBQWEsRUFGRTtBQUdmQyxVQUFRO0FBSE8sQ0FBakI7O0FBTUE7Ozs7Ozs7O0FBUUEsSUFBTUMseUJBQXlCLFNBQXpCQSxzQkFBeUI7QUFBQSxNQUFDQyxJQUFELHVFQUFRTCxRQUFSO0FBQUEsU0FBc0I7QUFDbkRNLGtCQURtRCwwQkFDbkNDLFlBRG1DLEVBQ3JCQyxXQURxQixFQUNSO0FBQ3pDO0FBQ0EsVUFBSUMsTUFBTUMsT0FBTixDQUFjSCxZQUFkLENBQUosRUFBaUM7QUFDL0IsWUFBTU4sWUFBWUksS0FBS0osU0FBdkI7QUFDQSxZQUFNQyxjQUFjRyxLQUFLSCxXQUF6QjtBQUNBLFlBQU1DLFNBQVNFLEtBQUtGLE1BQXBCO0FBQ0E7QUFDQTtBQUNBLFlBQU1RLFNBQVNILFlBQVlJLEtBQVosQ0FBa0IsUUFBbEIsQ0FBZjtBQUNBLFlBQUlELE1BQUosRUFBWTtBQUNWSix5QkFBZUEsYUFBYU0sSUFBYixDQUFrQlosWUFBWVUsT0FBTyxDQUFQLENBQTlCLENBQWY7QUFDRCxTQUZELE1BRU87QUFDTEoseUJBQWVBLGFBQWFNLElBQWIsQ0FBa0JaLFlBQVksR0FBOUIsQ0FBZjtBQUNEO0FBQ0Q7QUFDQSxZQUFJQyxXQUFKLEVBQWlCO0FBQ2YsY0FBTVksaUJBQWlCUCxhQUFhUSxXQUFiLENBQXlCZCxTQUF6QixDQUF2QjtBQUNBTSx5QkFBZUEsYUFDWlMsTUFEWSxDQUNMLENBREssRUFDRkYsY0FERSxLQUNpQlgsU0FBU0YsU0FBVCxHQUFxQixFQUR0QyxJQUM0QyxHQUQ1QyxHQUVYQyxXQUZXLEdBRUdLLGFBQWFTLE1BQWIsQ0FBb0JGLGlCQUFpQixDQUFyQyxDQUZsQjtBQUdEO0FBQ0Y7QUFDRCxhQUFPUCxZQUFQO0FBQ0Q7QUF4QmtELEdBQXRCO0FBQUEsQ0FBL0I7O0FBMkJBLGVBQWVILHNCQUFmIiwiZmlsZSI6ImlubGluZUFycmF5VHJhbnNmb3JtZXIuanMiLCJzb3VyY2VzQ29udGVudCI6WyIndXNlIHN0cmljdCdcblxuY29uc3QgZGVmYXVsdHMgPSB7XG4gIHNlcGFyYXRvcjogJycsXG4gIGNvbmp1bmN0aW9uOiAnJyxcbiAgc2VyaWFsOiBmYWxzZVxufVxuXG4vKipcbiAqIENvbnZlcnRzIGFuIGFycmF5IHN1YnN0aXR1dGlvbiB0byBhIHN0cmluZyBjb250YWluaW5nIGEgbGlzdFxuICogQHBhcmFtICB7U3RyaW5nfSBbb3B0cy5zZXBhcmF0b3IgPSAnJ10gLSB0aGUgY2hhcmFjdGVyIHRoYXQgc2VwYXJhdGVzIGVhY2ggaXRlbVxuICogQHBhcmFtICB7U3RyaW5nfSBbb3B0cy5jb25qdW5jdGlvbiA9ICcnXSAgLSByZXBsYWNlIHRoZSBsYXN0IHNlcGFyYXRvciB3aXRoIHRoaXNcbiAqIEBwYXJhbSAge0Jvb2xlYW59IFtvcHRzLnNlcmlhbCA9IGZhbHNlXSAtIGluY2x1ZGUgdGhlIHNlcGFyYXRvciBiZWZvcmUgdGhlIGNvbmp1bmN0aW9uPyAoT3hmb3JkIGNvbW1hIHVzZS1jYXNlKVxuICpcbiAqIEByZXR1cm4ge09iamVjdH0gICAgICAgICAgICAgICAgICAgICAtIGEgVGVtcGxhdGVUYWcgdHJhbnNmb3JtZXJcbiAqL1xuY29uc3QgaW5saW5lQXJyYXlUcmFuc2Zvcm1lciA9IChvcHRzID0gZGVmYXVsdHMpID0+ICh7XG4gIG9uU3Vic3RpdHV0aW9uIChzdWJzdGl0dXRpb24sIHJlc3VsdFNvRmFyKSB7XG4gICAgLy8gb25seSBvcGVyYXRlIG9uIGFycmF5c1xuICAgIGlmIChBcnJheS5pc0FycmF5KHN1YnN0aXR1dGlvbikpIHtcbiAgICAgIGNvbnN0IHNlcGFyYXRvciA9IG9wdHMuc2VwYXJhdG9yXG4gICAgICBjb25zdCBjb25qdW5jdGlvbiA9IG9wdHMuY29uanVuY3Rpb25cbiAgICAgIGNvbnN0IHNlcmlhbCA9IG9wdHMuc2VyaWFsXG4gICAgICAvLyBqb2luIGVhY2ggaXRlbSBpbiB0aGUgYXJyYXkgaW50byBhIHN0cmluZyB3aGVyZSBlYWNoIGl0ZW0gaXMgc2VwYXJhdGVkIGJ5IHNlcGFyYXRvclxuICAgICAgLy8gYmUgc3VyZSB0byBtYWludGFpbiBpbmRlbnRhdGlvblxuICAgICAgY29uc3QgaW5kZW50ID0gcmVzdWx0U29GYXIubWF0Y2goLyhcXHMrKSQvKVxuICAgICAgaWYgKGluZGVudCkge1xuICAgICAgICBzdWJzdGl0dXRpb24gPSBzdWJzdGl0dXRpb24uam9pbihzZXBhcmF0b3IgKyBpbmRlbnRbMV0pXG4gICAgICB9IGVsc2Uge1xuICAgICAgICBzdWJzdGl0dXRpb24gPSBzdWJzdGl0dXRpb24uam9pbihzZXBhcmF0b3IgKyAnICcpXG4gICAgICB9XG4gICAgICAvLyBpZiBjb25qdW5jdGlvbiBpcyBzZXQsIHJlcGxhY2UgdGhlIGxhc3Qgc2VwYXJhdG9yIHdpdGggY29uanVuY3Rpb25cbiAgICAgIGlmIChjb25qdW5jdGlvbikge1xuICAgICAgICBjb25zdCBzZXBhcmF0b3JJbmRleCA9IHN1YnN0aXR1dGlvbi5sYXN0SW5kZXhPZihzZXBhcmF0b3IpXG4gICAgICAgIHN1YnN0aXR1dGlvbiA9IHN1YnN0aXR1dGlvblxuICAgICAgICAgIC5zdWJzdHIoMCwgc2VwYXJhdG9ySW5kZXgpICsgKHNlcmlhbCA/IHNlcGFyYXRvciA6ICcnKSArICcgJyArXG4gICAgICAgICAgICBjb25qdW5jdGlvbiArIHN1YnN0aXR1dGlvbi5zdWJzdHIoc2VwYXJhdG9ySW5kZXggKyAxKVxuICAgICAgfVxuICAgIH1cbiAgICByZXR1cm4gc3Vic3RpdHV0aW9uXG4gIH1cbn0pXG5cbmV4cG9ydCBkZWZhdWx0IGlubGluZUFycmF5VHJhbnNmb3JtZXJcbiJdfQ==