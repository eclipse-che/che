define(["require", "exports", "./binding_on_syntax", "./constraint_helpers"], function (require, exports, binding_on_syntax_1, constraint_helpers_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var BindingWhenSyntax = (function () {
        function BindingWhenSyntax(binding) {
            this._binding = binding;
        }
        BindingWhenSyntax.prototype.when = function (constraint) {
            this._binding.constraint = constraint;
            return new binding_on_syntax_1.BindingOnSyntax(this._binding);
        };
        BindingWhenSyntax.prototype.whenTargetNamed = function (name) {
            this._binding.constraint = constraint_helpers_1.namedConstraint(name);
            return new binding_on_syntax_1.BindingOnSyntax(this._binding);
        };
        BindingWhenSyntax.prototype.whenTargetIsDefault = function () {
            this._binding.constraint = function (request) {
                var targetIsDefault = (request.target !== null) &&
                    (!request.target.isNamed()) &&
                    (!request.target.isTagged());
                return targetIsDefault;
            };
            return new binding_on_syntax_1.BindingOnSyntax(this._binding);
        };
        BindingWhenSyntax.prototype.whenTargetTagged = function (tag, value) {
            this._binding.constraint = constraint_helpers_1.taggedConstraint(tag)(value);
            return new binding_on_syntax_1.BindingOnSyntax(this._binding);
        };
        BindingWhenSyntax.prototype.whenInjectedInto = function (parent) {
            this._binding.constraint = function (request) {
                return constraint_helpers_1.typeConstraint(parent)(request.parentRequest);
            };
            return new binding_on_syntax_1.BindingOnSyntax(this._binding);
        };
        BindingWhenSyntax.prototype.whenParentNamed = function (name) {
            this._binding.constraint = function (request) {
                return constraint_helpers_1.namedConstraint(name)(request.parentRequest);
            };
            return new binding_on_syntax_1.BindingOnSyntax(this._binding);
        };
        BindingWhenSyntax.prototype.whenParentTagged = function (tag, value) {
            this._binding.constraint = function (request) {
                return constraint_helpers_1.taggedConstraint(tag)(value)(request.parentRequest);
            };
            return new binding_on_syntax_1.BindingOnSyntax(this._binding);
        };
        BindingWhenSyntax.prototype.whenAnyAncestorIs = function (ancestor) {
            this._binding.constraint = function (request) {
                return constraint_helpers_1.traverseAncerstors(request, constraint_helpers_1.typeConstraint(ancestor));
            };
            return new binding_on_syntax_1.BindingOnSyntax(this._binding);
        };
        BindingWhenSyntax.prototype.whenNoAncestorIs = function (ancestor) {
            this._binding.constraint = function (request) {
                return !constraint_helpers_1.traverseAncerstors(request, constraint_helpers_1.typeConstraint(ancestor));
            };
            return new binding_on_syntax_1.BindingOnSyntax(this._binding);
        };
        BindingWhenSyntax.prototype.whenAnyAncestorNamed = function (name) {
            this._binding.constraint = function (request) {
                return constraint_helpers_1.traverseAncerstors(request, constraint_helpers_1.namedConstraint(name));
            };
            return new binding_on_syntax_1.BindingOnSyntax(this._binding);
        };
        BindingWhenSyntax.prototype.whenNoAncestorNamed = function (name) {
            this._binding.constraint = function (request) {
                return !constraint_helpers_1.traverseAncerstors(request, constraint_helpers_1.namedConstraint(name));
            };
            return new binding_on_syntax_1.BindingOnSyntax(this._binding);
        };
        BindingWhenSyntax.prototype.whenAnyAncestorTagged = function (tag, value) {
            this._binding.constraint = function (request) {
                return constraint_helpers_1.traverseAncerstors(request, constraint_helpers_1.taggedConstraint(tag)(value));
            };
            return new binding_on_syntax_1.BindingOnSyntax(this._binding);
        };
        BindingWhenSyntax.prototype.whenNoAncestorTagged = function (tag, value) {
            this._binding.constraint = function (request) {
                return !constraint_helpers_1.traverseAncerstors(request, constraint_helpers_1.taggedConstraint(tag)(value));
            };
            return new binding_on_syntax_1.BindingOnSyntax(this._binding);
        };
        BindingWhenSyntax.prototype.whenAnyAncestorMatches = function (constraint) {
            this._binding.constraint = function (request) {
                return constraint_helpers_1.traverseAncerstors(request, constraint);
            };
            return new binding_on_syntax_1.BindingOnSyntax(this._binding);
        };
        BindingWhenSyntax.prototype.whenNoAncestorMatches = function (constraint) {
            this._binding.constraint = function (request) {
                return !constraint_helpers_1.traverseAncerstors(request, constraint);
            };
            return new binding_on_syntax_1.BindingOnSyntax(this._binding);
        };
        return BindingWhenSyntax;
    }());
    exports.BindingWhenSyntax = BindingWhenSyntax;
});
