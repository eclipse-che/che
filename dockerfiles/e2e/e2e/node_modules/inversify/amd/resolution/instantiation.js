define(["require", "exports", "../constants/error_msgs", "../constants/literal_types", "../constants/metadata_keys"], function (require, exports, error_msgs_1, literal_types_1, METADATA_KEY) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    function _injectProperties(instance, childRequests, resolveRequest) {
        var propertyInjectionsRequests = childRequests.filter(function (childRequest) {
            return (childRequest.target !== null &&
                childRequest.target.type === literal_types_1.TargetTypeEnum.ClassProperty);
        });
        var propertyInjections = propertyInjectionsRequests.map(resolveRequest);
        propertyInjectionsRequests.forEach(function (r, index) {
            var propertyName = "";
            propertyName = r.target.name.value();
            var injection = propertyInjections[index];
            instance[propertyName] = injection;
        });
        return instance;
    }
    function _createInstance(Func, injections) {
        return new (Func.bind.apply(Func, [void 0].concat(injections)))();
    }
    function _postConstruct(constr, result) {
        if (Reflect.hasMetadata(METADATA_KEY.POST_CONSTRUCT, constr)) {
            var data = Reflect.getMetadata(METADATA_KEY.POST_CONSTRUCT, constr);
            try {
                result[data.value]();
            }
            catch (e) {
                throw new Error(error_msgs_1.POST_CONSTRUCT_ERROR(constr.name, e.message));
            }
        }
    }
    function resolveInstance(constr, childRequests, resolveRequest) {
        var result = null;
        if (childRequests.length > 0) {
            var constructorInjectionsRequests = childRequests.filter(function (childRequest) {
                return (childRequest.target !== null && childRequest.target.type === literal_types_1.TargetTypeEnum.ConstructorArgument);
            });
            var constructorInjections = constructorInjectionsRequests.map(resolveRequest);
            result = _createInstance(constr, constructorInjections);
            result = _injectProperties(result, childRequests, resolveRequest);
        }
        else {
            result = new constr();
        }
        _postConstruct(constr, result);
        return result;
    }
    exports.resolveInstance = resolveInstance;
});
