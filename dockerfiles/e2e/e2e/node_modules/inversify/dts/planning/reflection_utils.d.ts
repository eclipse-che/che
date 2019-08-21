import { interfaces } from "../interfaces/interfaces";
import { getFunctionName } from "../utils/serialization";
declare function getDependencies(metadataReader: interfaces.MetadataReader, func: Function): interfaces.Target[];
declare function getBaseClassDependencyCount(metadataReader: interfaces.MetadataReader, func: Function): number;
export { getDependencies, getBaseClassDependencyCount, getFunctionName };
