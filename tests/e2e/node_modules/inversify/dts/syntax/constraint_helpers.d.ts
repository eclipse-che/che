import { interfaces } from "../interfaces/interfaces";
declare const traverseAncerstors: (request: interfaces.Request, constraint: interfaces.ConstraintFunction) => boolean;
declare const taggedConstraint: (key: PropertyKey) => (value: any) => interfaces.ConstraintFunction;
declare const namedConstraint: (value: any) => interfaces.ConstraintFunction;
declare const typeConstraint: (type: string | Function) => (request: interfaces.Request | null) => boolean;
export { traverseAncerstors, taggedConstraint, namedConstraint, typeConstraint };
