import { interfaces } from "../interfaces/interfaces";
declare function multiInject(serviceIdentifier: interfaces.ServiceIdentifier<any>): (target: any, targetKey: string, index?: number | undefined) => void;
export { multiInject };
