import { BindingOnSyntax } from "./binding_on_syntax";
import { namedConstraint, taggedConstraint, traverseAncerstors, typeConstraint } from "./constraint_helpers";
var BindingWhenSyntax = (function () {
    function BindingWhenSyntax(binding) {
        this._binding = binding;
    }
    BindingWhenSyntax.prototype.when = function (constraint) {
        this._binding.constraint = constraint;
        return new BindingOnSyntax(this._binding);
    };
    BindingWhenSyntax.prototype.whenTargetNamed = function (name) {
        this._binding.constraint = namedConstraint(name);
        return new BindingOnSyntax(this._binding);
    };
    BindingWhenSyntax.prototype.whenTargetIsDefault = function () {
        this._binding.constraint = function (request) {
            var targetIsDefault = (request.target !== null) &&
                (!request.target.isNamed()) &&
                (!request.target.isTagged());
            return targetIsDefault;
        };
        return new BindingOnSyntax(this._binding);
    };
    BindingWhenSyntax.prototype.whenTargetTagged = function (tag, value) {
        this._binding.constraint = taggedConstraint(tag)(value);
        return new BindingOnSyntax(this._binding);
    };
    BindingWhenSyntax.prototype.whenInjectedInto = function (parent) {
        this._binding.constraint = function (request) {
            return typeConstraint(parent)(request.parentRequest);
        };
        return new BindingOnSyntax(this._binding);
    };
    BindingWhenSyntax.prototype.whenParentNamed = function (name) {
        this._binding.constraint = function (request) {
            return namedConstraint(name)(request.parentRequest);
        };
        return new BindingOnSyntax(this._binding);
    };
    BindingWhenSyntax.prototype.whenParentTagged = function (tag, value) {
        this._binding.constraint = function (request) {
            return taggedConstraint(tag)(value)(request.parentRequest);
        };
        return new BindingOnSyntax(this._binding);
    };
    BindingWhenSyntax.prototype.whenAnyAncestorIs = function (ancestor) {
        this._binding.constraint = function (request) {
            return traverseAncerstors(request, typeConstraint(ancestor));
        };
        return new BindingOnSyntax(this._binding);
    };
    BindingWhenSyntax.prototype.whenNoAncestorIs = function (ancestor) {
        this._binding.constraint = function (request) {
            return !traverseAncerstors(request, typeConstraint(ancestor));
        };
        return new BindingOnSyntax(this._binding);
    };
    BindingWhenSyntax.prototype.whenAnyAncestorNamed = function (name) {
        this._binding.constraint = function (request) {
            return traverseAncerstors(request, namedConstraint(name));
        };
        return new BindingOnSyntax(this._binding);
    };
    BindingWhenSyntax.prototype.whenNoAncestorNamed = function (name) {
        this._binding.constraint = function (request) {
            return !traverseAncerstors(request, namedConstraint(name));
        };
        return new BindingOnSyntax(this._binding);
    };
    BindingWhenSyntax.prototype.whenAnyAncestorTagged = function (tag, value) {
        this._binding.constraint = function (request) {
            return traverseAncerstors(request, taggedConstraint(tag)(value));
        };
        return new BindingOnSyntax(this._binding);
    };
    BindingWhenSyntax.prototype.whenNoAncestorTagged = function (tag, value) {
        this._binding.constraint = function (request) {
            return !traverseAncerstors(request, taggedConstraint(tag)(value));
        };
        return new BindingOnSyntax(this._binding);
    };
    BindingWhenSyntax.prototype.whenAnyAncestorMatches = function (constraint) {
        this._binding.constraint = function (request) {
            return traverseAncerstors(request, constraint);
        };
        return new BindingOnSyntax(this._binding);
    };
    BindingWhenSyntax.prototype.whenNoAncestorMatches = function (constraint) {
        this._binding.constraint = function (request) {
            return !traverseAncerstors(request, constraint);
        };
        return new BindingOnSyntax(this._binding);
    };
    return BindingWhenSyntax;
}());
export { BindingWhenSyntax };
