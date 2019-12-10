import { interfaces } from "../interfaces/interfaces";
declare class QueryableString implements interfaces.QueryableString {
    private str;
    constructor(str: string);
    startsWith(searchString: string): boolean;
    endsWith(searchString: string): boolean;
    contains(searchString: string): boolean;
    equals(compareString: string): boolean;
    value(): string;
}
export { QueryableString };
