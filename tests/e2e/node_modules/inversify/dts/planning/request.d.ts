import { interfaces } from "../interfaces/interfaces";
declare class Request implements interfaces.Request {
    id: number;
    serviceIdentifier: interfaces.ServiceIdentifier<any>;
    parentContext: interfaces.Context;
    parentRequest: interfaces.Request | null;
    bindings: interfaces.Binding<any>[];
    childRequests: interfaces.Request[];
    target: interfaces.Target;
    requestScope: interfaces.RequestScope;
    constructor(serviceIdentifier: interfaces.ServiceIdentifier<any>, parentContext: interfaces.Context, parentRequest: interfaces.Request | null, bindings: (interfaces.Binding<any> | interfaces.Binding<any>[]), target: interfaces.Target);
    addChildRequest(serviceIdentifier: interfaces.ServiceIdentifier<any>, bindings: (interfaces.Binding<any> | interfaces.Binding<any>[]), target: interfaces.Target): interfaces.Request;
}
export { Request };
