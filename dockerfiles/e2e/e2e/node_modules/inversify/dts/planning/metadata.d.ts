import { interfaces } from "../interfaces/interfaces";
declare class Metadata implements interfaces.Metadata {
    key: string | number | symbol;
    value: any;
    constructor(key: string | number | symbol, value: any);
    toString(): string;
}
export { Metadata };
