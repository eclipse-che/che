import { interfaces } from "../interfaces/interfaces";
declare class BindingOnSyntax<T> implements interfaces.BindingOnSyntax<T> {
    private _binding;
    constructor(binding: interfaces.Binding<T>);
    onActivation(handler: (context: interfaces.Context, injectable: T) => T): interfaces.BindingWhenSyntax<T>;
}
export { BindingOnSyntax };
