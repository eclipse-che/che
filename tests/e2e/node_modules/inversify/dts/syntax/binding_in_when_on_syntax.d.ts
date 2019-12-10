import { interfaces } from "../interfaces/interfaces";
declare class BindingInWhenOnSyntax<T> implements interfaces.BindingInSyntax<T>, interfaces.BindingWhenSyntax<T>, interfaces.BindingOnSyntax<T> {
    private _bindingInSyntax;
    private _bindingWhenSyntax;
    private _bindingOnSyntax;
    private _binding;
    constructor(binding: interfaces.Binding<T>);
    inRequestScope(): interfaces.BindingWhenOnSyntax<T>;
    inSingletonScope(): interfaces.BindingWhenOnSyntax<T>;
    inTransientScope(): interfaces.BindingWhenOnSyntax<T>;
    when(constraint: (request: interfaces.Request) => boolean): interfaces.BindingOnSyntax<T>;
    whenTargetNamed(name: string): interfaces.BindingOnSyntax<T>;
    whenTargetIsDefault(): interfaces.BindingOnSyntax<T>;
    whenTargetTagged(tag: string, value: any): interfaces.BindingOnSyntax<T>;
    whenInjectedInto(parent: (Function | string)): interfaces.BindingOnSyntax<T>;
    whenParentNamed(name: string): interfaces.BindingOnSyntax<T>;
    whenParentTagged(tag: string, value: any): interfaces.BindingOnSyntax<T>;
    whenAnyAncestorIs(ancestor: (Function | string)): interfaces.BindingOnSyntax<T>;
    whenNoAncestorIs(ancestor: (Function | string)): interfaces.BindingOnSyntax<T>;
    whenAnyAncestorNamed(name: string): interfaces.BindingOnSyntax<T>;
    whenAnyAncestorTagged(tag: string, value: any): interfaces.BindingOnSyntax<T>;
    whenNoAncestorNamed(name: string): interfaces.BindingOnSyntax<T>;
    whenNoAncestorTagged(tag: string, value: any): interfaces.BindingOnSyntax<T>;
    whenAnyAncestorMatches(constraint: (request: interfaces.Request) => boolean): interfaces.BindingOnSyntax<T>;
    whenNoAncestorMatches(constraint: (request: interfaces.Request) => boolean): interfaces.BindingOnSyntax<T>;
    onActivation(handler: (context: interfaces.Context, injectable: T) => T): interfaces.BindingWhenSyntax<T>;
}
export { BindingInWhenOnSyntax };
