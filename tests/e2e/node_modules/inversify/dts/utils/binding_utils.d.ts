import { interfaces } from "../inversify";
export declare const multiBindToService: (container: interfaces.Container) => (service: string | symbol | interfaces.Newable<any> | interfaces.Abstract<any>) => (...types: (string | symbol | interfaces.Newable<any> | interfaces.Abstract<any>)[]) => void;
