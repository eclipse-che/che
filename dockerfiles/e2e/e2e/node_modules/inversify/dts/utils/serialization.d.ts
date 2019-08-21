import { interfaces } from "../interfaces/interfaces";
declare function getServiceIdentifierAsString(serviceIdentifier: interfaces.ServiceIdentifier<any>): string;
declare function listRegisteredBindingsForServiceIdentifier(container: interfaces.Container, serviceIdentifier: string, getBindings: <T>(container: interfaces.Container, serviceIdentifier: interfaces.ServiceIdentifier<T>) => interfaces.Binding<T>[]): string;
declare function circularDependencyToException(request: interfaces.Request): void;
declare function listMetadataForTarget(serviceIdentifierString: string, target: interfaces.Target): string;
declare function getFunctionName(v: any): string;
export { getFunctionName, getServiceIdentifierAsString, listRegisteredBindingsForServiceIdentifier, listMetadataForTarget, circularDependencyToException };
