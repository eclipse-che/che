import { interfaces } from "../interfaces/interfaces";
declare function getBindingDictionary(cntnr: any): interfaces.Lookup<interfaces.Binding<any>>;
declare function plan(metadataReader: interfaces.MetadataReader, container: interfaces.Container, isMultiInject: boolean, targetType: interfaces.TargetType, serviceIdentifier: interfaces.ServiceIdentifier<any>, key?: string | number | symbol, value?: any, avoidConstraints?: boolean): interfaces.Context;
declare function createMockRequest(container: interfaces.Container, serviceIdentifier: interfaces.ServiceIdentifier<any>, key: string | number | symbol, value: any): interfaces.Request;
export { plan, createMockRequest, getBindingDictionary };
