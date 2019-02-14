'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
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

exports.default = inlineArrayTransformer;
module.exports = exports['default'];
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uL3NyYy9pbmxpbmVBcnJheVRyYW5zZm9ybWVyL2lubGluZUFycmF5VHJhbnNmb3JtZXIuanMiXSwibmFtZXMiOlsiZGVmYXVsdHMiLCJzZXBhcmF0b3IiLCJjb25qdW5jdGlvbiIsInNlcmlhbCIsImlubGluZUFycmF5VHJhbnNmb3JtZXIiLCJvcHRzIiwib25TdWJzdGl0dXRpb24iLCJzdWJzdGl0dXRpb24iLCJyZXN1bHRTb0ZhciIsIkFycmF5IiwiaXNBcnJheSIsImluZGVudCIsIm1hdGNoIiwiam9pbiIsInNlcGFyYXRvckluZGV4IiwibGFzdEluZGV4T2YiLCJzdWJzdHIiXSwibWFwcGluZ3MiOiJBQUFBOzs7OztBQUVBLElBQU1BLFdBQVc7QUFDZkMsYUFBVyxFQURJO0FBRWZDLGVBQWEsRUFGRTtBQUdmQyxVQUFRO0FBSE8sQ0FBakI7O0FBTUE7Ozs7Ozs7O0FBUUEsSUFBTUMseUJBQXlCLFNBQXpCQSxzQkFBeUI7QUFBQSxNQUFDQyxJQUFELHVFQUFRTCxRQUFSO0FBQUEsU0FBc0I7QUFDbkRNLGtCQURtRCwwQkFDbkNDLFlBRG1DLEVBQ3JCQyxXQURxQixFQUNSO0FBQ3pDO0FBQ0EsVUFBSUMsTUFBTUMsT0FBTixDQUFjSCxZQUFkLENBQUosRUFBaUM7QUFDL0IsWUFBTU4sWUFBWUksS0FBS0osU0FBdkI7QUFDQSxZQUFNQyxjQUFjRyxLQUFLSCxXQUF6QjtBQUNBLFlBQU1DLFNBQVNFLEtBQUtGLE1BQXBCO0FBQ0E7QUFDQTtBQUNBLFlBQU1RLFNBQVNILFlBQVlJLEtBQVosQ0FBa0IsUUFBbEIsQ0FBZjtBQUNBLFlBQUlELE1BQUosRUFBWTtBQUNWSix5QkFBZUEsYUFBYU0sSUFBYixDQUFrQlosWUFBWVUsT0FBTyxDQUFQLENBQTlCLENBQWY7QUFDRCxTQUZELE1BRU87QUFDTEoseUJBQWVBLGFBQWFNLElBQWIsQ0FBa0JaLFlBQVksR0FBOUIsQ0FBZjtBQUNEO0FBQ0Q7QUFDQSxZQUFJQyxXQUFKLEVBQWlCO0FBQ2YsY0FBTVksaUJBQWlCUCxhQUFhUSxXQUFiLENBQXlCZCxTQUF6QixDQUF2QjtBQUNBTSx5QkFBZUEsYUFDWlMsTUFEWSxDQUNMLENBREssRUFDRkYsY0FERSxLQUNpQlgsU0FBU0YsU0FBVCxHQUFxQixFQUR0QyxJQUM0QyxHQUQ1QyxHQUVYQyxXQUZXLEdBRUdLLGFBQWFTLE1BQWIsQ0FBb0JGLGlCQUFpQixDQUFyQyxDQUZsQjtBQUdEO0FBQ0Y7QUFDRCxhQUFPUCxZQUFQO0FBQ0Q7QUF4QmtELEdBQXRCO0FBQUEsQ0FBL0I7O2tCQTJCZUgsc0IiLCJmaWxlIjoiaW5saW5lQXJyYXlUcmFuc2Zvcm1lci5qcyIsInNvdXJjZXNDb250ZW50IjpbIid1c2Ugc3RyaWN0J1xuXG5jb25zdCBkZWZhdWx0cyA9IHtcbiAgc2VwYXJhdG9yOiAnJyxcbiAgY29uanVuY3Rpb246ICcnLFxuICBzZXJpYWw6IGZhbHNlXG59XG5cbi8qKlxuICogQ29udmVydHMgYW4gYXJyYXkgc3Vic3RpdHV0aW9uIHRvIGEgc3RyaW5nIGNvbnRhaW5pbmcgYSBsaXN0XG4gKiBAcGFyYW0gIHtTdHJpbmd9IFtvcHRzLnNlcGFyYXRvciA9ICcnXSAtIHRoZSBjaGFyYWN0ZXIgdGhhdCBzZXBhcmF0ZXMgZWFjaCBpdGVtXG4gKiBAcGFyYW0gIHtTdHJpbmd9IFtvcHRzLmNvbmp1bmN0aW9uID0gJyddICAtIHJlcGxhY2UgdGhlIGxhc3Qgc2VwYXJhdG9yIHdpdGggdGhpc1xuICogQHBhcmFtICB7Qm9vbGVhbn0gW29wdHMuc2VyaWFsID0gZmFsc2VdIC0gaW5jbHVkZSB0aGUgc2VwYXJhdG9yIGJlZm9yZSB0aGUgY29uanVuY3Rpb24/IChPeGZvcmQgY29tbWEgdXNlLWNhc2UpXG4gKlxuICogQHJldHVybiB7T2JqZWN0fSAgICAgICAgICAgICAgICAgICAgIC0gYSBUZW1wbGF0ZVRhZyB0cmFuc2Zvcm1lclxuICovXG5jb25zdCBpbmxpbmVBcnJheVRyYW5zZm9ybWVyID0gKG9wdHMgPSBkZWZhdWx0cykgPT4gKHtcbiAgb25TdWJzdGl0dXRpb24gKHN1YnN0aXR1dGlvbiwgcmVzdWx0U29GYXIpIHtcbiAgICAvLyBvbmx5IG9wZXJhdGUgb24gYXJyYXlzXG4gICAgaWYgKEFycmF5LmlzQXJyYXkoc3Vic3RpdHV0aW9uKSkge1xuICAgICAgY29uc3Qgc2VwYXJhdG9yID0gb3B0cy5zZXBhcmF0b3JcbiAgICAgIGNvbnN0IGNvbmp1bmN0aW9uID0gb3B0cy5jb25qdW5jdGlvblxuICAgICAgY29uc3Qgc2VyaWFsID0gb3B0cy5zZXJpYWxcbiAgICAgIC8vIGpvaW4gZWFjaCBpdGVtIGluIHRoZSBhcnJheSBpbnRvIGEgc3RyaW5nIHdoZXJlIGVhY2ggaXRlbSBpcyBzZXBhcmF0ZWQgYnkgc2VwYXJhdG9yXG4gICAgICAvLyBiZSBzdXJlIHRvIG1haW50YWluIGluZGVudGF0aW9uXG4gICAgICBjb25zdCBpbmRlbnQgPSByZXN1bHRTb0Zhci5tYXRjaCgvKFxccyspJC8pXG4gICAgICBpZiAoaW5kZW50KSB7XG4gICAgICAgIHN1YnN0aXR1dGlvbiA9IHN1YnN0aXR1dGlvbi5qb2luKHNlcGFyYXRvciArIGluZGVudFsxXSlcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIHN1YnN0aXR1dGlvbiA9IHN1YnN0aXR1dGlvbi5qb2luKHNlcGFyYXRvciArICcgJylcbiAgICAgIH1cbiAgICAgIC8vIGlmIGNvbmp1bmN0aW9uIGlzIHNldCwgcmVwbGFjZSB0aGUgbGFzdCBzZXBhcmF0b3Igd2l0aCBjb25qdW5jdGlvblxuICAgICAgaWYgKGNvbmp1bmN0aW9uKSB7XG4gICAgICAgIGNvbnN0IHNlcGFyYXRvckluZGV4ID0gc3Vic3RpdHV0aW9uLmxhc3RJbmRleE9mKHNlcGFyYXRvcilcbiAgICAgICAgc3Vic3RpdHV0aW9uID0gc3Vic3RpdHV0aW9uXG4gICAgICAgICAgLnN1YnN0cigwLCBzZXBhcmF0b3JJbmRleCkgKyAoc2VyaWFsID8gc2VwYXJhdG9yIDogJycpICsgJyAnICtcbiAgICAgICAgICAgIGNvbmp1bmN0aW9uICsgc3Vic3RpdHV0aW9uLnN1YnN0cihzZXBhcmF0b3JJbmRleCArIDEpXG4gICAgICB9XG4gICAgfVxuICAgIHJldHVybiBzdWJzdGl0dXRpb25cbiAgfVxufSlcblxuZXhwb3J0IGRlZmF1bHQgaW5saW5lQXJyYXlUcmFuc2Zvcm1lclxuIl19