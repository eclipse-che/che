import { interfaces } from "../interfaces/interfaces";
export declare class ContainerModule implements interfaces.ContainerModule {
    id: number;
    registry: interfaces.ContainerModuleCallBack;
    constructor(registry: interfaces.ContainerModuleCallBack);
}
export declare class AsyncContainerModule implements interfaces.AsyncContainerModule {
    id: number;
    registry: interfaces.AsyncContainerModuleCallBack;
    constructor(registry: interfaces.AsyncContainerModuleCallBack);
}
