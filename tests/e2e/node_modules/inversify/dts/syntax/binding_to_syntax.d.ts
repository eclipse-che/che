import { interfaces } from "../interfaces/interfaces";
declare class BindingToSyntax<T> implements interfaces.BindingToSyntax<T> {
    private _binding;
    constructor(binding: interfaces.Binding<T>);
    to(constructor: {
        new (...args: any[]): T;
    }): interfaces.BindingInWhenOnSyntax<T>;
    toSelf(): interfaces.BindingInWhenOnSyntax<T>;
    toConstantValue(value: T): interfaces.BindingWhenOnSyntax<T>;
    toDynamicValue(func: (context: interfaces.Context) => T): interfaces.BindingInWhenOnSyntax<T>;
    toConstructor<T2>(constructor: interfaces.Newable<T2>): interfaces.BindingWhenOnSyntax<T>;
    toFactory<T2>(factory: interfaces.FactoryCreator<T2>): interfaces.BindingWhenOnSyntax<T>;
    toFunction(func: T): interfaces.BindingWhenOnSyntax<T>;
    toAutoFactory<T2>(serviceIdentifier: interfaces.ServiceIdentifier<T2>): interfaces.BindingWhenOnSyntax<T>;
    toProvider<T2>(provider: interfaces.ProviderCreator<T2>): interfaces.BindingWhenOnSyntax<T>;
    toService(service: string | symbol | interfaces.Newable<T> | interfaces.Abstract<T>): void;
}
export { BindingToSyntax };
