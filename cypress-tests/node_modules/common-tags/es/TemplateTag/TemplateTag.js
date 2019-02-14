'use strict';

/**
 * @class TemplateTag
 * @classdesc Consumes a pipeline of composeable transformer plugins and produces a template tag.
 */

import _taggedTemplateLiteral from 'babel-runtime/helpers/taggedTemplateLiteral';
import _classCallCheck from 'babel-runtime/helpers/classCallCheck';
import _createClass from 'babel-runtime/helpers/createClass';

var _templateObject = _taggedTemplateLiteral(['', ''], ['', '']);

var TemplateTag = function () {
  /**
   * constructs a template tag
   * @constructs TemplateTag
   * @param  {...Object} [...transformers] - an array or arguments list of transformers
   * @return {Function}                    - a template tag
   */
  function TemplateTag() {
    for (var _len = arguments.length, transformers = Array(_len), _key = 0; _key < _len; _key++) {
      transformers[_key] = arguments[_key];
    }

    _classCallCheck(this, TemplateTag);

    // if first argument is an array, extrude it as a list of transformers
    if (transformers.length && Array.isArray(transformers[0])) {
      transformers = transformers[0];
    }

    // if any transformers are functions, this means they are not initiated - automatically initiate them
    this.transformers = transformers.map(function (transformer) {
      return typeof transformer === 'function' ? transformer() : transformer;
    });

    // return an ES2015 template tag
    return this.tag.bind(this);
  }

  /**
   * Applies all transformers to a template literal tagged with this method.
   * If a function is passed as the first argument, assumes the function is a template tag
   * and applies it to the template, returning a template tag.
   * @param  {(Function|Array<String>)} args[0] - Either a template tag or an array containing template strings separated by identifier
   * @param  {...*} [args[1]]                   - Optional list of substitution values.
   * @return {(String|Function)}                - Either an intermediary tag function or the results of processing the template.
   */


  _createClass(TemplateTag, [{
    key: 'tag',
    value: function tag() {
      for (var _len2 = arguments.length, args = Array(_len2), _key2 = 0; _key2 < _len2; _key2++) {
        args[_key2] = arguments[_key2];
      }

      // if the first argument passed is a function, assume it is a template tag and return
      // an intermediary tag that processes the template using the aforementioned tag, passing the
      // result to our tag
      if (typeof args[0] === 'function') {
        return this.interimTag.bind(this, args.shift());
      }

      // else, return a transformed end result of processing the template with our tag
      return this.transformEndResult(args.shift().reduce(this.processSubstitutions.bind(this, args)));
    }

    /**
     * An intermediary template tag that receives a template tag and passes the result of calling the template with the received
     * template tag to our own template tag.
     * @param  {Function}        nextTag          - the received template tag
     * @param  {Array<String>}   template         - the template to process
     * @param  {...*}            ...substitutions - `substitutions` is an array of all substitutions in the template
     * @return {*}                                - the final processed value
     */

  }, {
    key: 'interimTag',
    value: function interimTag(previousTag, template) {
      for (var _len3 = arguments.length, substitutions = Array(_len3 > 2 ? _len3 - 2 : 0), _key3 = 2; _key3 < _len3; _key3++) {
        substitutions[_key3 - 2] = arguments[_key3];
      }

      return this.tag(_templateObject, previousTag.apply(undefined, [template].concat(substitutions)));
    }

    /**
     * Performs bulk processing on the tagged template, transforming each substitution and then
     * concatenating the resulting values into a string.
     * @param  {Array<*>} substitutions - an array of all remaining substitutions present in this template
     * @param  {String}   resultSoFar   - this iteration's result string so far
     * @param  {String}   remainingPart - the template chunk after the current substitution
     * @return {String}                 - the result of joining this iteration's processed substitution with the result
     */

  }, {
    key: 'processSubstitutions',
    value: function processSubstitutions(substitutions, resultSoFar, remainingPart) {
      var substitution = this.transformSubstitution(substitutions.shift(), resultSoFar);
      return resultSoFar + substitution + remainingPart;
    }

    /**
     * When a substitution is encountered, iterates through each transformer and applies the transformer's
     * `onSubstitution` method to the substitution.
     * @param  {*}      substitution - The current substitution
     * @param  {String} resultSoFar  - The result up to and excluding this substitution.
     * @return {*}                   - The final result of applying all substitution transformations.
     */

  }, {
    key: 'transformSubstitution',
    value: function transformSubstitution(substitution, resultSoFar) {
      var cb = function cb(res, transform) {
        return transform.onSubstitution ? transform.onSubstitution(res, resultSoFar) : res;
      };
      return this.transformers.reduce(cb, substitution);
    }

    /**
     * Iterates through each transformer, applying the transformer's `onEndResult` method to the
     * template literal after all substitutions have finished processing.
     * @param  {String} endResult - The processed template, just before it is returned from the tag
     * @return {String}           - The final results of processing each transformer
     */

  }, {
    key: 'transformEndResult',
    value: function transformEndResult(endResult) {
      var cb = function cb(res, transform) {
        return transform.onEndResult ? transform.onEndResult(res) : res;
      };
      return this.transformers.reduce(cb, endResult);
    }
  }]);

  return TemplateTag;
}();

export default TemplateTag;
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uL3NyYy9UZW1wbGF0ZVRhZy9UZW1wbGF0ZVRhZy5qcyJdLCJuYW1lcyI6WyJUZW1wbGF0ZVRhZyIsInRyYW5zZm9ybWVycyIsImxlbmd0aCIsIkFycmF5IiwiaXNBcnJheSIsIm1hcCIsInRyYW5zZm9ybWVyIiwidGFnIiwiYXJncyIsImludGVyaW1UYWciLCJiaW5kIiwic2hpZnQiLCJ0cmFuc2Zvcm1FbmRSZXN1bHQiLCJyZWR1Y2UiLCJwcm9jZXNzU3Vic3RpdHV0aW9ucyIsInByZXZpb3VzVGFnIiwidGVtcGxhdGUiLCJzdWJzdGl0dXRpb25zIiwicmVzdWx0U29GYXIiLCJyZW1haW5pbmdQYXJ0Iiwic3Vic3RpdHV0aW9uIiwidHJhbnNmb3JtU3Vic3RpdHV0aW9uIiwiY2IiLCJyZXMiLCJ0cmFuc2Zvcm0iLCJvblN1YnN0aXR1dGlvbiIsImVuZFJlc3VsdCIsIm9uRW5kUmVzdWx0Il0sIm1hcHBpbmdzIjoiQUFBQTs7QUFFQTs7Ozs7Ozs7Ozs7SUFJcUJBLFc7QUFDbkI7Ozs7OztBQU1BLHlCQUE4QjtBQUFBLHNDQUFkQyxZQUFjO0FBQWRBLGtCQUFjO0FBQUE7O0FBQUE7O0FBQzVCO0FBQ0EsUUFBSUEsYUFBYUMsTUFBYixJQUF1QkMsTUFBTUMsT0FBTixDQUFjSCxhQUFhLENBQWIsQ0FBZCxDQUEzQixFQUEyRDtBQUN6REEscUJBQWVBLGFBQWEsQ0FBYixDQUFmO0FBQ0Q7O0FBRUQ7QUFDQSxTQUFLQSxZQUFMLEdBQW9CQSxhQUFhSSxHQUFiLENBQWlCLFVBQUNDLFdBQUQsRUFBaUI7QUFDcEQsYUFBTyxPQUFPQSxXQUFQLEtBQXVCLFVBQXZCLEdBQ0hBLGFBREcsR0FFSEEsV0FGSjtBQUdELEtBSm1CLENBQXBCOztBQU1BO0FBQ0EsV0FBUyxLQUFLQyxHQUFkLE1BQVMsSUFBVDtBQUNEOztBQUVEOzs7Ozs7Ozs7Ozs7MEJBUWM7QUFBQSx5Q0FBTkMsSUFBTTtBQUFOQSxZQUFNO0FBQUE7O0FBQ1o7QUFDQTtBQUNBO0FBQ0EsVUFBSSxPQUFPQSxLQUFLLENBQUwsQ0FBUCxLQUFtQixVQUF2QixFQUFtQztBQUNqQyxlQUFPLEtBQUtDLFVBQUwsQ0FBZ0JDLElBQWhCLENBQXFCLElBQXJCLEVBQTJCRixLQUFLRyxLQUFMLEVBQTNCLENBQVA7QUFDRDs7QUFFRDtBQUNBLGFBQU8sS0FBS0Msa0JBQUwsQ0FDTEosS0FBS0csS0FBTCxHQUFhRSxNQUFiLENBQW9CLEtBQUtDLG9CQUFMLENBQTBCSixJQUExQixDQUErQixJQUEvQixFQUFxQ0YsSUFBckMsQ0FBcEIsQ0FESyxDQUFQO0FBR0Q7O0FBRUQ7Ozs7Ozs7Ozs7OytCQVFZTyxXLEVBQWFDLFEsRUFBNEI7QUFBQSx5Q0FBZkMsYUFBZTtBQUFmQSxxQkFBZTtBQUFBOztBQUNuRCxhQUFPLEtBQUtWLEdBQVosa0JBQWtCUSw4QkFBWUMsUUFBWixTQUF5QkMsYUFBekIsRUFBbEI7QUFDRDs7QUFFRDs7Ozs7Ozs7Ozs7eUNBUXNCQSxhLEVBQWVDLFcsRUFBYUMsYSxFQUFlO0FBQy9ELFVBQU1DLGVBQWUsS0FBS0MscUJBQUwsQ0FDbkJKLGNBQWNOLEtBQWQsRUFEbUIsRUFFbkJPLFdBRm1CLENBQXJCO0FBSUEsYUFBT0EsY0FBY0UsWUFBZCxHQUE2QkQsYUFBcEM7QUFDRDs7QUFFRDs7Ozs7Ozs7OzswQ0FPdUJDLFksRUFBY0YsVyxFQUFhO0FBQ2hELFVBQU1JLEtBQUssU0FBTEEsRUFBSyxDQUFDQyxHQUFELEVBQU1DLFNBQU47QUFBQSxlQUFvQkEsVUFBVUMsY0FBVixHQUMzQkQsVUFBVUMsY0FBVixDQUF5QkYsR0FBekIsRUFBOEJMLFdBQTlCLENBRDJCLEdBRTNCSyxHQUZPO0FBQUEsT0FBWDtBQUdBLGFBQU8sS0FBS3RCLFlBQUwsQ0FBa0JZLE1BQWxCLENBQXlCUyxFQUF6QixFQUE2QkYsWUFBN0IsQ0FBUDtBQUNEOztBQUVEOzs7Ozs7Ozs7dUNBTW9CTSxTLEVBQVc7QUFDN0IsVUFBTUosS0FBSyxTQUFMQSxFQUFLLENBQUNDLEdBQUQsRUFBTUMsU0FBTjtBQUFBLGVBQW9CQSxVQUFVRyxXQUFWLEdBQzNCSCxVQUFVRyxXQUFWLENBQXNCSixHQUF0QixDQUQyQixHQUUzQkEsR0FGTztBQUFBLE9BQVg7QUFHQSxhQUFPLEtBQUt0QixZQUFMLENBQWtCWSxNQUFsQixDQUF5QlMsRUFBekIsRUFBNkJJLFNBQTdCLENBQVA7QUFDRDs7Ozs7O2VBbkdrQjFCLFciLCJmaWxlIjoiVGVtcGxhdGVUYWcuanMiLCJzb3VyY2VzQ29udGVudCI6WyIndXNlIHN0cmljdCdcblxuLyoqXG4gKiBAY2xhc3MgVGVtcGxhdGVUYWdcbiAqIEBjbGFzc2Rlc2MgQ29uc3VtZXMgYSBwaXBlbGluZSBvZiBjb21wb3NlYWJsZSB0cmFuc2Zvcm1lciBwbHVnaW5zIGFuZCBwcm9kdWNlcyBhIHRlbXBsYXRlIHRhZy5cbiAqL1xuZXhwb3J0IGRlZmF1bHQgY2xhc3MgVGVtcGxhdGVUYWcge1xuICAvKipcbiAgICogY29uc3RydWN0cyBhIHRlbXBsYXRlIHRhZ1xuICAgKiBAY29uc3RydWN0cyBUZW1wbGF0ZVRhZ1xuICAgKiBAcGFyYW0gIHsuLi5PYmplY3R9IFsuLi50cmFuc2Zvcm1lcnNdIC0gYW4gYXJyYXkgb3IgYXJndW1lbnRzIGxpc3Qgb2YgdHJhbnNmb3JtZXJzXG4gICAqIEByZXR1cm4ge0Z1bmN0aW9ufSAgICAgICAgICAgICAgICAgICAgLSBhIHRlbXBsYXRlIHRhZ1xuICAgKi9cbiAgY29uc3RydWN0b3IgKC4uLnRyYW5zZm9ybWVycykge1xuICAgIC8vIGlmIGZpcnN0IGFyZ3VtZW50IGlzIGFuIGFycmF5LCBleHRydWRlIGl0IGFzIGEgbGlzdCBvZiB0cmFuc2Zvcm1lcnNcbiAgICBpZiAodHJhbnNmb3JtZXJzLmxlbmd0aCAmJiBBcnJheS5pc0FycmF5KHRyYW5zZm9ybWVyc1swXSkpIHtcbiAgICAgIHRyYW5zZm9ybWVycyA9IHRyYW5zZm9ybWVyc1swXVxuICAgIH1cblxuICAgIC8vIGlmIGFueSB0cmFuc2Zvcm1lcnMgYXJlIGZ1bmN0aW9ucywgdGhpcyBtZWFucyB0aGV5IGFyZSBub3QgaW5pdGlhdGVkIC0gYXV0b21hdGljYWxseSBpbml0aWF0ZSB0aGVtXG4gICAgdGhpcy50cmFuc2Zvcm1lcnMgPSB0cmFuc2Zvcm1lcnMubWFwKCh0cmFuc2Zvcm1lcikgPT4ge1xuICAgICAgcmV0dXJuIHR5cGVvZiB0cmFuc2Zvcm1lciA9PT0gJ2Z1bmN0aW9uJ1xuICAgICAgICA/IHRyYW5zZm9ybWVyKClcbiAgICAgICAgOiB0cmFuc2Zvcm1lclxuICAgIH0pXG5cbiAgICAvLyByZXR1cm4gYW4gRVMyMDE1IHRlbXBsYXRlIHRhZ1xuICAgIHJldHVybiA6OnRoaXMudGFnXG4gIH1cblxuICAvKipcbiAgICogQXBwbGllcyBhbGwgdHJhbnNmb3JtZXJzIHRvIGEgdGVtcGxhdGUgbGl0ZXJhbCB0YWdnZWQgd2l0aCB0aGlzIG1ldGhvZC5cbiAgICogSWYgYSBmdW5jdGlvbiBpcyBwYXNzZWQgYXMgdGhlIGZpcnN0IGFyZ3VtZW50LCBhc3N1bWVzIHRoZSBmdW5jdGlvbiBpcyBhIHRlbXBsYXRlIHRhZ1xuICAgKiBhbmQgYXBwbGllcyBpdCB0byB0aGUgdGVtcGxhdGUsIHJldHVybmluZyBhIHRlbXBsYXRlIHRhZy5cbiAgICogQHBhcmFtICB7KEZ1bmN0aW9ufEFycmF5PFN0cmluZz4pfSBhcmdzWzBdIC0gRWl0aGVyIGEgdGVtcGxhdGUgdGFnIG9yIGFuIGFycmF5IGNvbnRhaW5pbmcgdGVtcGxhdGUgc3RyaW5ncyBzZXBhcmF0ZWQgYnkgaWRlbnRpZmllclxuICAgKiBAcGFyYW0gIHsuLi4qfSBbYXJnc1sxXV0gICAgICAgICAgICAgICAgICAgLSBPcHRpb25hbCBsaXN0IG9mIHN1YnN0aXR1dGlvbiB2YWx1ZXMuXG4gICAqIEByZXR1cm4geyhTdHJpbmd8RnVuY3Rpb24pfSAgICAgICAgICAgICAgICAtIEVpdGhlciBhbiBpbnRlcm1lZGlhcnkgdGFnIGZ1bmN0aW9uIG9yIHRoZSByZXN1bHRzIG9mIHByb2Nlc3NpbmcgdGhlIHRlbXBsYXRlLlxuICAgKi9cbiAgdGFnICguLi5hcmdzKSB7XG4gICAgLy8gaWYgdGhlIGZpcnN0IGFyZ3VtZW50IHBhc3NlZCBpcyBhIGZ1bmN0aW9uLCBhc3N1bWUgaXQgaXMgYSB0ZW1wbGF0ZSB0YWcgYW5kIHJldHVyblxuICAgIC8vIGFuIGludGVybWVkaWFyeSB0YWcgdGhhdCBwcm9jZXNzZXMgdGhlIHRlbXBsYXRlIHVzaW5nIHRoZSBhZm9yZW1lbnRpb25lZCB0YWcsIHBhc3NpbmcgdGhlXG4gICAgLy8gcmVzdWx0IHRvIG91ciB0YWdcbiAgICBpZiAodHlwZW9mIGFyZ3NbMF0gPT09ICdmdW5jdGlvbicpIHtcbiAgICAgIHJldHVybiB0aGlzLmludGVyaW1UYWcuYmluZCh0aGlzLCBhcmdzLnNoaWZ0KCkpXG4gICAgfVxuXG4gICAgLy8gZWxzZSwgcmV0dXJuIGEgdHJhbnNmb3JtZWQgZW5kIHJlc3VsdCBvZiBwcm9jZXNzaW5nIHRoZSB0ZW1wbGF0ZSB3aXRoIG91ciB0YWdcbiAgICByZXR1cm4gdGhpcy50cmFuc2Zvcm1FbmRSZXN1bHQoXG4gICAgICBhcmdzLnNoaWZ0KCkucmVkdWNlKHRoaXMucHJvY2Vzc1N1YnN0aXR1dGlvbnMuYmluZCh0aGlzLCBhcmdzKSlcbiAgICApXG4gIH1cblxuICAvKipcbiAgICogQW4gaW50ZXJtZWRpYXJ5IHRlbXBsYXRlIHRhZyB0aGF0IHJlY2VpdmVzIGEgdGVtcGxhdGUgdGFnIGFuZCBwYXNzZXMgdGhlIHJlc3VsdCBvZiBjYWxsaW5nIHRoZSB0ZW1wbGF0ZSB3aXRoIHRoZSByZWNlaXZlZFxuICAgKiB0ZW1wbGF0ZSB0YWcgdG8gb3VyIG93biB0ZW1wbGF0ZSB0YWcuXG4gICAqIEBwYXJhbSAge0Z1bmN0aW9ufSAgICAgICAgbmV4dFRhZyAgICAgICAgICAtIHRoZSByZWNlaXZlZCB0ZW1wbGF0ZSB0YWdcbiAgICogQHBhcmFtICB7QXJyYXk8U3RyaW5nPn0gICB0ZW1wbGF0ZSAgICAgICAgIC0gdGhlIHRlbXBsYXRlIHRvIHByb2Nlc3NcbiAgICogQHBhcmFtICB7Li4uKn0gICAgICAgICAgICAuLi5zdWJzdGl0dXRpb25zIC0gYHN1YnN0aXR1dGlvbnNgIGlzIGFuIGFycmF5IG9mIGFsbCBzdWJzdGl0dXRpb25zIGluIHRoZSB0ZW1wbGF0ZVxuICAgKiBAcmV0dXJuIHsqfSAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgLSB0aGUgZmluYWwgcHJvY2Vzc2VkIHZhbHVlXG4gICAqL1xuICBpbnRlcmltVGFnIChwcmV2aW91c1RhZywgdGVtcGxhdGUsIC4uLnN1YnN0aXR1dGlvbnMpIHtcbiAgICByZXR1cm4gdGhpcy50YWdgJHtwcmV2aW91c1RhZyh0ZW1wbGF0ZSwgLi4uc3Vic3RpdHV0aW9ucyl9YFxuICB9XG5cbiAgLyoqXG4gICAqIFBlcmZvcm1zIGJ1bGsgcHJvY2Vzc2luZyBvbiB0aGUgdGFnZ2VkIHRlbXBsYXRlLCB0cmFuc2Zvcm1pbmcgZWFjaCBzdWJzdGl0dXRpb24gYW5kIHRoZW5cbiAgICogY29uY2F0ZW5hdGluZyB0aGUgcmVzdWx0aW5nIHZhbHVlcyBpbnRvIGEgc3RyaW5nLlxuICAgKiBAcGFyYW0gIHtBcnJheTwqPn0gc3Vic3RpdHV0aW9ucyAtIGFuIGFycmF5IG9mIGFsbCByZW1haW5pbmcgc3Vic3RpdHV0aW9ucyBwcmVzZW50IGluIHRoaXMgdGVtcGxhdGVcbiAgICogQHBhcmFtICB7U3RyaW5nfSAgIHJlc3VsdFNvRmFyICAgLSB0aGlzIGl0ZXJhdGlvbidzIHJlc3VsdCBzdHJpbmcgc28gZmFyXG4gICAqIEBwYXJhbSAge1N0cmluZ30gICByZW1haW5pbmdQYXJ0IC0gdGhlIHRlbXBsYXRlIGNodW5rIGFmdGVyIHRoZSBjdXJyZW50IHN1YnN0aXR1dGlvblxuICAgKiBAcmV0dXJuIHtTdHJpbmd9ICAgICAgICAgICAgICAgICAtIHRoZSByZXN1bHQgb2Ygam9pbmluZyB0aGlzIGl0ZXJhdGlvbidzIHByb2Nlc3NlZCBzdWJzdGl0dXRpb24gd2l0aCB0aGUgcmVzdWx0XG4gICAqL1xuICBwcm9jZXNzU3Vic3RpdHV0aW9ucyAoc3Vic3RpdHV0aW9ucywgcmVzdWx0U29GYXIsIHJlbWFpbmluZ1BhcnQpIHtcbiAgICBjb25zdCBzdWJzdGl0dXRpb24gPSB0aGlzLnRyYW5zZm9ybVN1YnN0aXR1dGlvbihcbiAgICAgIHN1YnN0aXR1dGlvbnMuc2hpZnQoKSxcbiAgICAgIHJlc3VsdFNvRmFyXG4gICAgKVxuICAgIHJldHVybiByZXN1bHRTb0ZhciArIHN1YnN0aXR1dGlvbiArIHJlbWFpbmluZ1BhcnRcbiAgfVxuXG4gIC8qKlxuICAgKiBXaGVuIGEgc3Vic3RpdHV0aW9uIGlzIGVuY291bnRlcmVkLCBpdGVyYXRlcyB0aHJvdWdoIGVhY2ggdHJhbnNmb3JtZXIgYW5kIGFwcGxpZXMgdGhlIHRyYW5zZm9ybWVyJ3NcbiAgICogYG9uU3Vic3RpdHV0aW9uYCBtZXRob2QgdG8gdGhlIHN1YnN0aXR1dGlvbi5cbiAgICogQHBhcmFtICB7Kn0gICAgICBzdWJzdGl0dXRpb24gLSBUaGUgY3VycmVudCBzdWJzdGl0dXRpb25cbiAgICogQHBhcmFtICB7U3RyaW5nfSByZXN1bHRTb0ZhciAgLSBUaGUgcmVzdWx0IHVwIHRvIGFuZCBleGNsdWRpbmcgdGhpcyBzdWJzdGl0dXRpb24uXG4gICAqIEByZXR1cm4geyp9ICAgICAgICAgICAgICAgICAgIC0gVGhlIGZpbmFsIHJlc3VsdCBvZiBhcHBseWluZyBhbGwgc3Vic3RpdHV0aW9uIHRyYW5zZm9ybWF0aW9ucy5cbiAgICovXG4gIHRyYW5zZm9ybVN1YnN0aXR1dGlvbiAoc3Vic3RpdHV0aW9uLCByZXN1bHRTb0Zhcikge1xuICAgIGNvbnN0IGNiID0gKHJlcywgdHJhbnNmb3JtKSA9PiB0cmFuc2Zvcm0ub25TdWJzdGl0dXRpb25cbiAgICAgID8gdHJhbnNmb3JtLm9uU3Vic3RpdHV0aW9uKHJlcywgcmVzdWx0U29GYXIpXG4gICAgICA6IHJlc1xuICAgIHJldHVybiB0aGlzLnRyYW5zZm9ybWVycy5yZWR1Y2UoY2IsIHN1YnN0aXR1dGlvbilcbiAgfVxuXG4gIC8qKlxuICAgKiBJdGVyYXRlcyB0aHJvdWdoIGVhY2ggdHJhbnNmb3JtZXIsIGFwcGx5aW5nIHRoZSB0cmFuc2Zvcm1lcidzIGBvbkVuZFJlc3VsdGAgbWV0aG9kIHRvIHRoZVxuICAgKiB0ZW1wbGF0ZSBsaXRlcmFsIGFmdGVyIGFsbCBzdWJzdGl0dXRpb25zIGhhdmUgZmluaXNoZWQgcHJvY2Vzc2luZy5cbiAgICogQHBhcmFtICB7U3RyaW5nfSBlbmRSZXN1bHQgLSBUaGUgcHJvY2Vzc2VkIHRlbXBsYXRlLCBqdXN0IGJlZm9yZSBpdCBpcyByZXR1cm5lZCBmcm9tIHRoZSB0YWdcbiAgICogQHJldHVybiB7U3RyaW5nfSAgICAgICAgICAgLSBUaGUgZmluYWwgcmVzdWx0cyBvZiBwcm9jZXNzaW5nIGVhY2ggdHJhbnNmb3JtZXJcbiAgICovXG4gIHRyYW5zZm9ybUVuZFJlc3VsdCAoZW5kUmVzdWx0KSB7XG4gICAgY29uc3QgY2IgPSAocmVzLCB0cmFuc2Zvcm0pID0+IHRyYW5zZm9ybS5vbkVuZFJlc3VsdFxuICAgICAgPyB0cmFuc2Zvcm0ub25FbmRSZXN1bHQocmVzKVxuICAgICAgOiByZXNcbiAgICByZXR1cm4gdGhpcy50cmFuc2Zvcm1lcnMucmVkdWNlKGNiLCBlbmRSZXN1bHQpXG4gIH1cbn1cbiJdfQ==