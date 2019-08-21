import { BindingScopeEnum } from "../constants/literal_types";
import { BindingWhenOnSyntax } from "./binding_when_on_syntax";
var BindingInSyntax = (function () {
    function BindingInSyntax(binding) {
        this._binding = binding;
    }
    BindingInSyntax.prototype.inRequestScope = function () {
        this._binding.scope = BindingScopeEnum.Request;
        return new BindingWhenOnSyntax(this._binding);
    };
    BindingInSyntax.prototype.inSingletonScope = function () {
        this._binding.scope = BindingScopeEnum.Singleton;
        return new BindingWhenOnSyntax(this._binding);
    };
    BindingInSyntax.prototype.inTransientScope = function () {
        this._binding.scope = BindingScopeEnum.Transient;
        return new BindingWhenOnSyntax(this._binding);
    };
    return BindingInSyntax;
}());
export { BindingInSyntax };
