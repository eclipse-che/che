import { interfaces } from "../interfaces/interfaces";
declare class Context implements interfaces.Context {
    id: number;
    container: interfaces.Container;
    plan: interfaces.Plan;
    currentRequest: interfaces.Request;
    constructor(container: interfaces.Container);
    addPlan(plan: interfaces.Plan): void;
    setCurrentRequest(currentRequest: interfaces.Request): void;
}
export { Context };
