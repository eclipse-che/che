export class NameGenerator {
    public static generate(prefix: string, randomLength: number): string {
        let possibleCharacters: string = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        let i: number;
        let randomPart: string = "";

        for (i = 0; i < randomLength; i++) {
            let currentRandomIndex: number = Math.floor(Math.random() * Math.floor(52));

            randomPart += possibleCharacters[currentRandomIndex];
        }

        return prefix + randomPart;
    }

}
