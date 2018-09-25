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
import {FormatterMode} from "./formatter-mode";

/**
 * AsciiArray allow to build output arrays and format them in a simple way.
 * No need for each command to build its own logic.
 * There are CSV, Modern formatters, etc.
 * @author Florent Benoit
 */
export interface AsciiArray {

    /**
     * Specify the titles for this array
     *
     * @param columnsTitle
     *         the given titles
     * @return the current array
     */
    withTitles(...columnsTitle:Array<String>) : AsciiArray;

    /**
     * Specify the columns (containing data) for this array
     *
     * @param columns
     *         the given data column
     * @return the current array
     */
    withColumns(...columns:Array<Array<string>>) : AsciiArray;

    /**
     * Specify the rows (containing data) for this array
     *
     * @param rows
     *         the given data column
     * @return the current array
     */
    withListRows(...rows:Array<Array<string>>) : AsciiArray;

    /**
     * Specify the rows (containing data) for this array
     *
     * @param rows
     *         the given data column
     * @return the current array
     */
    withRows(rows:Array<Array<string>>) : AsciiArray;

    /**
     * Transform the given data into an ascii array
     *
     * @return stringified table of the array
     */
    toAscii() : string;

    /**
     * Apply a formatter to this array
     * @param formatterMode the name of the formatter
     */
    withFormatter(formatterMode:FormatterMode) : AsciiArray;

    /**
     * Show or not the title when getting result
     * @param showTitles if true, display it
     * @returns {DefaultAsciiArray}
     */
    withShowTitles(showTitles:boolean) : AsciiArray;


    /**
     * Specify the order and columns that need to be displayed.
     * @param columnTitles an array of name of columns
     */
    withFormatColumnTitles(...columnTitles:Array<string>) : AsciiArray;


    /**
     * Specify the order and columns that need to be displayed.
     * @param columnNames a comma separated list of columns
     */
    withFormatColumns(columnNames:string) : AsciiArray;

}
