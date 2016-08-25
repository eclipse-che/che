

export class I18n {

    mapOfConstants : Map<string, string>;

    constructor() {
        this.mapOfConstants = new Map<string, string>();
    }

    get(key: string, ...optional: Array<any>) : string {

        let constant : string = this.mapOfConstants.get(key);
        // not found, return the key
        if (!constant) {
            return key;
        }

        // replace values
        return constant.replace(/{(\d+)}/g, (match, number) => {
            return typeof optional[number] != 'undefined'
                ? optional[number]
                : match
        });
    }


    add(key : string, value : string) {
        this.mapOfConstants.set(key, value);
    }

}