define(["require", "exports"], function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var BindingScopeEnum = {
        Request: "Request",
        Singleton: "Singleton",
        Transient: "Transient"
    };
    exports.BindingScopeEnum = BindingScopeEnum;
    var BindingTypeEnum = {
        ConstantValue: "ConstantValue",
        Constructor: "Constructor",
        DynamicValue: "DynamicValue",
        Factory: "Factory",
        Function: "Function",
        Instance: "Instance",
        Invalid: "Invalid",
        Provider: "Provider"
    };
    exports.BindingTypeEnum = BindingTypeEnum;
    var TargetTypeEnum = {
        ClassProperty: "ClassProperty",
        ConstructorArgument: "ConstructorArgument",
        Variable: "Variable"
    };
    exports.TargetTypeEnum = TargetTypeEnum;
});
