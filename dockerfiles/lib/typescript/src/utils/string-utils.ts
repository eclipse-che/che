
export class StringUtils {
    static startsWith(value:string, searchString:string):boolean {
        return value.substr(0, searchString.length) === searchString;
    }
}