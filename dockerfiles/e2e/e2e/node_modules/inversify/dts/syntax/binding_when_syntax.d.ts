import { interfaces } from "../interfaces/interfaces";
declare class BindingWhenSyntax<T> implements interfaces.BindingWhenSyntax<T> {
    private _binding;
    constructor(binding: interfaces.Binding<T>);
    when(constraint: (request: interfaces.Request) => boolean): interfaces.BindingOnSyntax<T>;
    whenTargetNamed(name: string | number | symbol): interfaces.BindingOnSyntax<T>;
    whenTargetIsDefault(): interfaces.BindingOnSyntax<T>;
    whenTargetTagged(tag: string | number | symbol, value: any): interfaces.BindingOnSyntax<T>;
    whenInjectedInto(parent: (Function | string)): interfaces.BindingOnSyntax<T>;
    whenParentNamed(name: string | number | symbol): interfaces.BindingOnSyntax<T>;
    whenParentTagged(tag: string | number | symbol, value: any): interfaces.BindingOnSyntax<T>;
    whenAnyAncestorIs(ancestor: (Function | string)): interfaces.BindingOnSyntax<T>;
    whenNoAncestorIs(ancestor: (Function | string)): interfaces.BindingOnSyntax<T>;
    whenAnyAncestorNamed(name: string | number | symbol): interfaces.BindingOnSyntax<T>;
    whenNoAncestorNamed(name: string | number | symbol): interfaces.BindingOnSyntax<T>;
    whenAnyAncestorTagged(tag: string | number | symbol, value: any): interfaces.BindingOnSyntax<T>;
    whenNoAncestorTagged(tag: string | number | symbol, value: any): interfaces.BindingOnSyntax<T>;
    whenAnyAncestorMatches(constraint: (request: interfaces.Request) => boolean): interfaces.BindingOnSyntax<T>;
    whenNoAncestorMatches(constraint: (request: interfaces.Request) => boolean): interfaces.BindingOnSyntax<T>;
}
export { BindingWhenSyntax };
