import { BindingWhenSyntax } from "./binding_when_syntax";
var BindingOnSyntax = (function () {
    function BindingOnSyntax(binding) {
        this._binding = binding;
    }
    BindingOnSyntax.prototype.onActivation = function (handler) {
        this._binding.onActivation = handler;
        return new BindingWhenSyntax(this._binding);
    };
    return BindingOnSyntax;
}());
export { BindingOnSyntax };
