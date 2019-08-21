define(["require", "exports", "../utils/id"], function (require, exports, id_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var ContainerModule = (function () {
        function ContainerModule(registry) {
            this.id = id_1.id();
            this.registry = registry;
        }
        return ContainerModule;
    }());
    exports.ContainerModule = ContainerModule;
    var AsyncContainerModule = (function () {
        function AsyncContainerModule(registry) {
            this.id = id_1.id();
            this.registry = registry;
        }
        return AsyncContainerModule;
    }());
    exports.AsyncContainerModule = AsyncContainerModule;
});
