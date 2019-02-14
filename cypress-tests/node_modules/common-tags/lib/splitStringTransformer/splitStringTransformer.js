'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
var splitStringTransformer = function splitStringTransformer(splitBy) {
  return {
    onSubstitution: function onSubstitution(substitution, resultSoFar) {
      if (splitBy != null && typeof splitBy === 'string') {
        if (typeof substitution === 'string' && substitution.includes(splitBy)) {
          substitution = substitution.split(splitBy);
        }
      } else {
        throw new Error('You need to specify a string character to split by.');
      }
      return substitution;
    }
  };
};

exports.default = splitStringTransformer;
module.exports = exports['default'];
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uL3NyYy9zcGxpdFN0cmluZ1RyYW5zZm9ybWVyL3NwbGl0U3RyaW5nVHJhbnNmb3JtZXIuanMiXSwibmFtZXMiOlsic3BsaXRTdHJpbmdUcmFuc2Zvcm1lciIsInNwbGl0QnkiLCJvblN1YnN0aXR1dGlvbiIsInN1YnN0aXR1dGlvbiIsInJlc3VsdFNvRmFyIiwiaW5jbHVkZXMiLCJzcGxpdCIsIkVycm9yIl0sIm1hcHBpbmdzIjoiQUFBQTs7Ozs7QUFFQSxJQUFNQSx5QkFBeUIsU0FBekJBLHNCQUF5QixDQUFDQyxPQUFEO0FBQUEsU0FBYztBQUMzQ0Msa0JBRDJDLDBCQUMzQkMsWUFEMkIsRUFDYkMsV0FEYSxFQUNBO0FBQ3pDLFVBQUlILFdBQVcsSUFBWCxJQUFtQixPQUFPQSxPQUFQLEtBQW1CLFFBQTFDLEVBQW9EO0FBQ2xELFlBQUksT0FBT0UsWUFBUCxLQUF3QixRQUF4QixJQUFvQ0EsYUFBYUUsUUFBYixDQUFzQkosT0FBdEIsQ0FBeEMsRUFBd0U7QUFDdEVFLHlCQUFlQSxhQUFhRyxLQUFiLENBQW1CTCxPQUFuQixDQUFmO0FBQ0Q7QUFDRixPQUpELE1BSU87QUFDTCxjQUFNLElBQUlNLEtBQUosQ0FBVSxxREFBVixDQUFOO0FBQ0Q7QUFDRCxhQUFPSixZQUFQO0FBQ0Q7QUFWMEMsR0FBZDtBQUFBLENBQS9COztrQkFhZUgsc0IiLCJmaWxlIjoic3BsaXRTdHJpbmdUcmFuc2Zvcm1lci5qcyIsInNvdXJjZXNDb250ZW50IjpbIid1c2Ugc3RyaWN0J1xuXG5jb25zdCBzcGxpdFN0cmluZ1RyYW5zZm9ybWVyID0gKHNwbGl0QnkpID0+ICh7XG4gIG9uU3Vic3RpdHV0aW9uIChzdWJzdGl0dXRpb24sIHJlc3VsdFNvRmFyKSB7XG4gICAgaWYgKHNwbGl0QnkgIT0gbnVsbCAmJiB0eXBlb2Ygc3BsaXRCeSA9PT0gJ3N0cmluZycpIHtcbiAgICAgIGlmICh0eXBlb2Ygc3Vic3RpdHV0aW9uID09PSAnc3RyaW5nJyAmJiBzdWJzdGl0dXRpb24uaW5jbHVkZXMoc3BsaXRCeSkpIHtcbiAgICAgICAgc3Vic3RpdHV0aW9uID0gc3Vic3RpdHV0aW9uLnNwbGl0KHNwbGl0QnkpXG4gICAgICB9XG4gICAgfSBlbHNlIHtcbiAgICAgIHRocm93IG5ldyBFcnJvcignWW91IG5lZWQgdG8gc3BlY2lmeSBhIHN0cmluZyBjaGFyYWN0ZXIgdG8gc3BsaXQgYnkuJylcbiAgICB9XG4gICAgcmV0dXJuIHN1YnN0aXR1dGlvblxuICB9XG59KVxuXG5leHBvcnQgZGVmYXVsdCBzcGxpdFN0cmluZ1RyYW5zZm9ybWVyXG4iXX0=