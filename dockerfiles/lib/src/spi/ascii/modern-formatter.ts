/*
 * Copyright (c) 2016-2017 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc.- initial API and implementation
 */
import {AsciiFormatter} from "./ascii-formatter";
import {AsciiArrayInfo} from "./ascii-array-info";
import {AsciiFormInfo} from "./ascii-form-info";
import {AsciiFormat} from "./ascii-format";

/**
 * Modern formatter with spaces (2 spaces) between columns and left-aligned values.
 */
export class ModernFormatter implements AsciiFormatter {


    /**
     * No border for modern formatter
     * @param asciiArrayInfo
     * @return empty
     */

    public getBorderLine(asciiArrayInfo:AsciiArrayInfo):string {
        return null;
    }


    public  getFormatter(asciiArrayInfo:AsciiArrayInfo):string {
        let buffer:string = "";

        asciiArrayInfo.getColumnsSize().forEach((columnSize) => {
            buffer += "%-" + columnSize + "s";
            buffer += "  ";
        });
        buffer += "%n";

        return buffer;

    }

    public  getTitleFormatter(asciiArrayInfo:AsciiArrayInfo):string {
        let buffer:string = "";

        asciiArrayInfo.getColumnsSize().forEach((columnSize) => {
            // uppercase
            buffer += "%-" + columnSize + "S";
            buffer += "  ";
        });
        buffer += "%n";

        return buffer;

    }


    public formatFormTitle(name:string, asciiFormInfo:AsciiFormInfo):string {
        let entryName:string = name;

        // format it
        let flag:string = "s";
        if (asciiFormInfo.isUppercasePropertyName()) {
            flag = "S";
        }
        return AsciiFormat.format("%-" + (asciiFormInfo.getTitleColumnSize()) + flag, [name]);
    }


    public  formatFormValue(value:string, asciiFormInfo:AsciiFormInfo):string {
        if (!value) {
            return " ";
        }

        // just adding text after adding a space
        return " " + value;
    }

}
