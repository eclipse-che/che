import { interfaces } from "../interfaces/interfaces";
declare class Binding<T> implements interfaces.Binding<T> {
    id: number;
    moduleId: string;
    activated: boolean;
    serviceIdentifier: interfaces.ServiceIdentifier<T>;
    implementationType: interfaces.Newable<T> | null;
    cache: T | null;
    dynamicValue: ((context: interfaces.Context) => T) | null;
    scope: interfaces.BindingScope;
    type: interfaces.BindingType;
    factory: interfaces.FactoryCreator<T> | null;
    provider: interfaces.ProviderCreator<T> | null;
    constraint: (request: interfaces.Request) => boolean;
    onActivation: ((context: interfaces.Context, injectable: T) => T) | null;
    constructor(serviceIdentifier: interfaces.ServiceIdentifier<T>, scope: interfaces.BindingScope);
    clone(): interfaces.Binding<T>;
}
export { Binding };
