import { interfaces } from "../interfaces/interfaces";
declare class Plan implements interfaces.Plan {
    parentContext: interfaces.Context;
    rootRequest: interfaces.Request;
    constructor(parentContext: interfaces.Context, rootRequest: interfaces.Request);
}
export { Plan };
