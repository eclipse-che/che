import { id } from "../utils/id";
var ContainerModule = (function () {
    function ContainerModule(registry) {
        this.id = id();
        this.registry = registry;
    }
    return ContainerModule;
}());
export { ContainerModule };
var AsyncContainerModule = (function () {
    function AsyncContainerModule(registry) {
        this.id = id();
        this.registry = registry;
    }
    return AsyncContainerModule;
}());
export { AsyncContainerModule };
