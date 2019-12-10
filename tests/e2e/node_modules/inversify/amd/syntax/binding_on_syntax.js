define(["require", "exports", "./binding_when_syntax"], function (require, exports, binding_when_syntax_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var BindingOnSyntax = (function () {
        function BindingOnSyntax(binding) {
            this._binding = binding;
        }
        BindingOnSyntax.prototype.onActivation = function (handler) {
            this._binding.onActivation = handler;
            return new binding_when_syntax_1.BindingWhenSyntax(this._binding);
        };
        return BindingOnSyntax;
    }());
    exports.BindingOnSyntax = BindingOnSyntax;
});
