import { interfaces } from "../interfaces/interfaces";
declare class ContainerSnapshot implements interfaces.ContainerSnapshot {
    bindings: interfaces.Lookup<interfaces.Binding<any>>;
    middleware: interfaces.Next | null;
    static of(bindings: interfaces.Lookup<interfaces.Binding<any>>, middleware: interfaces.Next | null): ContainerSnapshot;
}
export { ContainerSnapshot };
