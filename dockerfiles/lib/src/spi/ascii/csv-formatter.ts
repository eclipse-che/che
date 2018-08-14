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

/**
 * CSV formatter
 * @author Florent Benoit
 */
export class CSVFormatter implements AsciiFormatter {


    /**
     * No border for CSV formatter
     * @param asciiArrayInfo
     * @return empty
     */
    getBorderLine(asciiArrayInfo:AsciiArrayInfo):string {
        return null;
    }

    /**
     * Provides comma separated list of values
     * @returns {string}
     */
    getFormatter(asciiArrayInfo:AsciiArrayInfo):string {

        let value = "";
        let size:number = asciiArrayInfo.getColumnsSize().length;
        for (let c:number = 1; c <= size; c++) {
            value += "%s";
            if (c < size) {
                value += ",";
            }
        }
        value += "%n";
        return value;
    }

    /**
     * Provides comma separated list of values for title as well (with uppercase)
     * @returns {string}
     */
    getTitleFormatter(asciiArrayInfo:AsciiArrayInfo):string {
        let value = "";
        let size:number = asciiArrayInfo.getColumnsSize().length;
        for (let c:number = 1; c <= size; c++) {
            // uppercase
            value += "%S";
            if (c < size) {
                value += ",";
            }
        }
        value += "%n";
        return value;
    }

    /**
     * Do not format forms
     */
    formatFormTitle(name:string, asciiFormInfo:AsciiFormInfo):string {
        return null;
    }

    /**
     * Do not format forms
     */
    formatFormValue(value:string, asciiFormInfo:AsciiFormInfo):string {
        return null;
    }

}