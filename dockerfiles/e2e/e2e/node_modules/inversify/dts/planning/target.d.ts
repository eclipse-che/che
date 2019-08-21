import { interfaces } from "../interfaces/interfaces";
import { Metadata } from "./metadata";
declare class Target implements interfaces.Target {
    id: number;
    type: interfaces.TargetType;
    serviceIdentifier: interfaces.ServiceIdentifier<any>;
    name: interfaces.QueryableString;
    metadata: Metadata[];
    constructor(type: interfaces.TargetType, name: string, serviceIdentifier: interfaces.ServiceIdentifier<any>, namedOrTagged?: (string | Metadata));
    hasTag(key: string): boolean;
    isArray(): boolean;
    matchesArray(name: interfaces.ServiceIdentifier<any>): boolean;
    isNamed(): boolean;
    isTagged(): boolean;
    isOptional(): boolean;
    getNamedTag(): interfaces.Metadata | null;
    getCustomTags(): interfaces.Metadata[] | null;
    matchesNamedTag(name: string): boolean;
    matchesTag(key: string): (value: any) => boolean;
}
export { Target };
