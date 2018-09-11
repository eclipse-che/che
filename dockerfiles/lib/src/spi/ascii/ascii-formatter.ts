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
import {AsciiFormInfo} from "./ascii-form-info";
import {AsciiArrayInfo} from "./ascii-array-info";

/**
 * Allow to format Ascii Arrays of Ascii Forms
 * @author Florent Benoit
 */
export interface AsciiFormatter {

    /**
     * If array has a borderline, allow to format it.
     * @param asciiArrayInfo the data on the array
     */
    getBorderLine(asciiArrayInfo:AsciiArrayInfo) : string;

    /**
     * Gets formatter for the given array info
     * @param asciiArrayInfo the data on the array
     */
    getFormatter(asciiArrayInfo:AsciiArrayInfo) : string;

    /**
     * Gets formatter but for the title (which can be different)
     * @param asciiArrayInfo the data on the array
     */
    getTitleFormatter(asciiArrayInfo:AsciiArrayInfo) : string;

    /**
     * Format the title of a ascii form
     * @param name the name to format
     * @param asciiFormInfo data about the form
     */
    formatFormTitle(name:string, asciiFormInfo:AsciiFormInfo) : string;

    /**
     * Format the value of a ascii form
     * @param value the name to format
     * @param asciiFormInfo data about the form
     */
    formatFormValue(value:string, asciiFormInfo:AsciiFormInfo) : string;

}


