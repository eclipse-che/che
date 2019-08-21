import { interfaces } from "../interfaces/interfaces";
declare function tagParameter(annotationTarget: any, propertyName: string, parameterIndex: number, metadata: interfaces.Metadata): void;
declare function tagProperty(annotationTarget: any, propertyName: string, metadata: interfaces.Metadata): void;
declare function decorate(decorator: (ClassDecorator | ParameterDecorator | MethodDecorator), target: any, parameterIndex?: number | string): void;
export { decorate, tagParameter, tagProperty };
