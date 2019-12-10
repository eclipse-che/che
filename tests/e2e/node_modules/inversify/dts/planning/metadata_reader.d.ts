import { interfaces } from "../interfaces/interfaces";
declare class MetadataReader implements interfaces.MetadataReader {
    getConstructorMetadata(constructorFunc: Function): interfaces.ConstructorMetadata;
    getPropertiesMetadata(constructorFunc: Function): interfaces.MetadataMap;
}
export { MetadataReader };
