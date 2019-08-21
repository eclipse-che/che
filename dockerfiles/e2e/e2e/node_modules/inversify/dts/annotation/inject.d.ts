import { interfaces } from "../interfaces/interfaces";
export declare type ServiceIdentifierOrFunc = interfaces.ServiceIdentifier<any> | LazyServiceIdentifer;
export declare class LazyServiceIdentifer<T = any> {
    private _cb;
    constructor(cb: () => interfaces.ServiceIdentifier<T>);
    unwrap(): string | symbol | interfaces.Newable<T> | interfaces.Abstract<T>;
}
declare function inject(serviceIdentifier: ServiceIdentifierOrFunc): (target: any, targetKey: string, index?: number | undefined) => void;
export { inject };
