/*
 * Copyright (c) 2016-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
/**
 * Provides helper method to format content
 * @author Florent Benoit
 */
export class AsciiFormat {


    /**
     * Allow to format values provided as array of string with the formatter
     * @param formatter it can include %s, %S (uppercase), %d number, %n carriage return, -<digits>%s or -<digits>%S for left aligned values
     * @param values the values to include in formatted string
     * @returns {string} the formatted string
     */
    public static format(formatter:string, values:Array<string>):string {
        let stringToEval:string = formatter;
        let index:number = 0;
        return stringToEval.replace(/%((%)|s|S|d|n|-[0-9]*(s|S))/g, (match) => {
            var val = null;

            if (index > values.length) {
                return "N/A : invalid formatting";
            }

            switch (match[1]) {
                case '-':
                    // number of spaces are all digits after - and before last character
                    let numberChar:number = 2;
                    let spaceValue:string = "";
                    while (numberChar < match.length - 1) {
                        spaceValue += match[numberChar];
                        numberChar++;
                    }

                    let numberOfSpaces = parseInt(spaceValue);

                    val = values[index];
                    while (val.length < numberOfSpaces) {
                        val = val + " ";
                    }
                    // uppercase
                    if (match[match.length - 1] && 'S' === match[match.length - 1]) {
                        val = val.toUpperCase();
                    }
                    index++;
                    break;
                case 'd':
                    val = parseFloat(values[index]);
                    if (isNaN(val)) {
                        val = 0;
                    }
                    index++;
                    break;
                case 'S':
                    val = values[index].toUpperCase();
                    index++;
                    break;
                case 'n':
                    val = "\n";
                    break;
                case 's':
                    val = values[index];
                    index++;
                    break;
            }

            return val;
        });
    }

}