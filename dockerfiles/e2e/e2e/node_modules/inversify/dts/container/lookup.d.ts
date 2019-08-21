import { interfaces } from "../interfaces/interfaces";
declare class Lookup<T extends interfaces.Clonable<T>> implements interfaces.Lookup<T> {
    private _map;
    constructor();
    getMap(): Map<string | symbol | interfaces.Newable<any> | interfaces.Abstract<any>, T[]>;
    add(serviceIdentifier: interfaces.ServiceIdentifier<any>, value: T): void;
    get(serviceIdentifier: interfaces.ServiceIdentifier<any>): T[];
    remove(serviceIdentifier: interfaces.ServiceIdentifier<any>): void;
    removeByCondition(condition: (item: T) => boolean): void;
    hasKey(serviceIdentifier: interfaces.ServiceIdentifier<any>): boolean;
    clone(): interfaces.Lookup<T>;
    traverse(func: (key: interfaces.ServiceIdentifier<any>, value: T[]) => void): void;
}
export { Lookup };
