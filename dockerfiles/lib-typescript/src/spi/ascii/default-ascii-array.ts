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
import {AsciiFormatter} from "./ascii-formatter";
import {FormatterMode} from "./formatter-mode";
import {CSVFormatter} from "./csv-formatter";
import {AsciiArray} from "./ascii-array";
import {AsciiArrayInfo} from "./ascii-array-info";
import {AsciiFormat} from "./ascii-format";
import {ModernFormatter} from "./modern-formatter";


/**
 * Implementation of {@link AsciiArray}
 * @author Florent Benoit
 */
export class DefaultAsciiArray implements AsciiArray {

    /**
     * Columns of this array
     */
    private  columns:Array<Array<string>>;

    /**
     * Columns of this array
     */
    private  rows:Array<Array<string>>;

    /**
     * Titles of each column (if any)
     */
    private titles:Array<string>;

    /**
     * Formatters.
     */
    private formatters:Map<FormatterMode, AsciiFormatter>;

    /**
     * Formatter
     */
    private  formatterMode:FormatterMode;


    /**
     * Show the titles.
     */
    private showTitles:boolean = true;


    /**
     * Order and name of column titles
     */
    private formatColumnTitles:Array<string>;


    /**
     * Used when rendering. number of columns to render.
     */
    private numberOfColumns:number;

    /**
     * Used when rendering.
     */
    private columnMapping:Array<number>;


    /**
     * Default constructor
     */
    public constructor() {
        this.columns = new Array<Array<string>>();
        this.rows = new Array<Array<string>>();
        this.titles = new Array<string>();
        this.formatters = new Map<FormatterMode, AsciiFormatter>();
        this.formatColumnTitles = new Array<string>();
        //this.formatters.se(EIGHTIES, new EightiesFormatter());
        this.formatters.set('MODERN', new ModernFormatter());
        this.formatters.set('CSV', new CSVFormatter());
        this.columnMapping = new Array<number>();
        this.formatterMode = 'MODERN';
    }

    /**
     * Specify the titles for this array
     *
     * @param columnsTitle
     *         the given titles
     * @return the current array
     */

    public  withTitles(...columnsTitle:Array<string>):AsciiArray {
        this.titles = columnsTitle;
        return this;
    }


    /**
     * Specify the columns (containing data) for this array
     *
     * @param columns
     *         the given data column
     * @return the current array
     */

    public  withColumns(...columns:Array<Array<string>>):AsciiArray {
        if (this.rows.length > 0) {
            throw new Error("Array is already built with columns, cannot mix both inputs");
        }

        columns.forEach((column) => {
            this.addColumn(column);
        });
        return this;
    }

    /**
     * Specify the rows (containing data) for this array
     *
     * @param rows
     *         the given data column
     * @return the current array
     */

    public withRows(rows:Array<Array<string>>):AsciiArray {
        if (this.columns.length > 0) {
            throw new Error("Array is already built with columns, cannot mix both inputs");
        }
        rows.forEach((row) => {
            this.addRow(row);
        });
        return this;
    }

    /**
     * Specify the rows (containing data) for this array
     *
     * @param rows
     *         the given data column
     * @return the current array
     */

    public withListRows(...rows:Array<Array<string>>):AsciiArray {
        if (this.columns.length > 0) {
            throw new Error("Array is already built with columns, cannot mix both inputs");
        }
        rows.forEach((row) => {
            this.addRow(row);
        });
        return this;
    }

    /**
     * Add the given column
     *
     * @param column
     *         the column
     */
    protected addColumn(column:Array<string>):void {
        this.columns.push(column);
    }


    /**
     * Add the given row
     *
     * @param row
     *         the row
     */
    protected addRow(row:Array<string>):void {
        this.rows.push(row);
    }


    /**
     * Checks that the array is valid before trying to get its stringified version
     */
    protected checkIntegrity():void {
        // check that columns have the same row length
        if (this.columns.length > 0) {
            let size:number = this.columns[0].length;

            this.columns.forEach((column) => {
                if (column.length != size) {
                    throw new Error("The columns have not the same sized. : " + column.length + " vs " + size);
                }
            });

            // if there are titles check that we've the same number of columns
            if (this.titles) {
                if (this.columns.length > 0) {
                    if (this.titles.length != this.columns.length) {
                        throw new Error(
                            "Invalid expected titles. There are " + this.columns.length + " while there are " + this.titles.length + " titles.");
                    }
                }
            }

        }
    }


    /**
     * Transform the given data into an ascii array
     *
     * @return stringified table of the array
     */

    public toAscii():string {

        // check
        this.checkIntegrity();

        // handle empty
        if (this.columns.length === 0 && this.rows.length === 0) {
            return "";
        }

        // compute mapping
        this.computeMapping();

        // first line is the border
        let computeBorder:string = this.getBorderLine();
        let beginBorderLine:string;
        let endBorderLine:string;
        if (computeBorder != null) {
            beginBorderLine = computeBorder + "%n";
            endBorderLine = computeBorder;
        } else {
            beginBorderLine = "";
            endBorderLine = "";
        }

        let buffer:string = beginBorderLine;
        let formatter:string = this.getFormatter();
        let titleFormatter:string = this.getTitleFormatter();

        // now add titles if any
        if (this.titles.length > 0 && this.showTitles) {
            let value:Array<string> = this.titles;
            if (this.formatColumnTitles.length > 0) {
                value = this.getMappingRow(value);
            }


            buffer += AsciiFormat.format(titleFormatter, value);


            buffer += beginBorderLine;
        }

        // data ?
        if (this.rows.length > 0) {
            this.rows.forEach((row) => {
                if (this.formatColumnTitles.length > 0) {
                    row = this.getMappingRow(row);
                }

                buffer += AsciiFormat.format(formatter, row);
            });
            buffer += endBorderLine;

        }


        if (this.columns.length > 0) {
            let nbRows:number = this.columns[0].length;


            for (let row:number = 0; row < nbRows; row++) {
                let rowElements = this.getRow(row);
                if (this.formatColumnTitles.length > 0) {
                    rowElements = this.getMappingRow(rowElements);
                }
                buffer += AsciiFormat.format(formatter, rowElements);
            }
            buffer += endBorderLine;
        }


        if (buffer.length > 0 && buffer.slice(-1) === '\n') {
            // remove it
            buffer = buffer.slice(0, -1);
        }

        return buffer;

    }

    /**
     * Extract columns from the existing row
     * @param row the row to analyze and extract and sort column
     * @returns {Array<string>}
     */
    protected  getMappingRow(row:Array<string>):Array<string> {
        let mapped:Array<string> = new Array<string>();
        for (let i = 0; i < this.numberOfColumns; i++) {
            mapped.push(row[this.columnMapping[i]]);
        }
        return mapped;
    }

    /**
     * Get content of a selected row for the given array
     *
     * @param index
     *         the index in the columns
     * @return the content
     */
    protected getRow(index:number):Array<string> {
        let row:Array<string> = new Array<string>();
        let i:number = 0;
        this.columns.forEach((column) => {
            row[i++] = column[index];
        });
        return row;
    }

    /**
     * @return formatter
     */
    protected  getFormatterMode():AsciiFormatter {
        return this.formatters.get(this.formatterMode);
    }

    protected  getArrayInfo():AsciiArrayInfo {
        return new MyAsciiArrayInfo(this);
    }

    /**
     * @return formatter used to format row content
     */
    protected  getFormatter():string {
        return this.getFormatterMode().getFormatter(this.getArrayInfo());
    }

    /**
     * @return formatter used to format title content
     */
    protected  getTitleFormatter():string {
        return this.getFormatterMode().getTitleFormatter(this.getArrayInfo());
    }


    /**
     * @return value used as border of the array
     */
    protected getBorderLine():string {
        return this.getFormatterMode().getBorderLine(this.getArrayInfo());
    }


    /**
     * Compute a mapping between existing columns and the columns that need to be displayed.
     */
    protected computeMapping():void {
        if (this.formatColumnTitles.length > 0) {
            this.numberOfColumns = this.formatColumnTitles.length;

            // compute column ordering.
            for (let column:number = 0; column < this.numberOfColumns; column++) {
                // indexOf

                var foundIndex = -1;
                this.titles.some((element, i) => {
                    if (this.formatColumnTitles[column].toLowerCase() === element.toLowerCase()) {
                        foundIndex = i;
                        return true;
                    }
                });
                if (foundIndex === -1) {
                    throw new Error("There is no column with name '" + this.formatColumnTitles[column] + "'. Existing names are " + this.titles);
                }
                this.columnMapping[column] = foundIndex;
            }

        } else {
            if (this.rows.length > 0) {
                this.numberOfColumns = this.rows[0].length;
            } else {
                this.numberOfColumns = this.columns.length;
            }
            for (let column:number = 0; column < this.numberOfColumns; column++) {
                // no mapping
                this.columnMapping[column] = column;
            }

        }
    }

    /**
     * @return the size of the column (by searching max size of each column, including title)
     */
    public getColumnsSize():Array<number> {
        let lengths:Array<number> = new Array<number>();

        // Array with rows input
        if (this.rows.length > 0) {


            // column number is first row length
            for (let column:number = 0; column < this.numberOfColumns; column++) {
                // for each column, set the max length
                let maxLength:number = 0;

                // for title
                if (this.titles && this.titles.length > 0 && this.showTitles) {
                    let currentLength:number = this.titles[this.columnMapping[column]].length;
                    if (currentLength > maxLength) {
                        maxLength = currentLength;
                    }
                }

                this.rows.forEach((row) => {
                    let currentLength = row[this.columnMapping[column]].length;
                    if (currentLength > maxLength) {
                        maxLength = currentLength;
                    }

                });

                lengths.push(maxLength);


            }
        }

        // Array with columns input
        if (this.columns.length > 0) {
            for (let column:number = 0; column < this.columns.length; column++) {
                // for each column, set the max length
                let maxLength:number = 0;

                // for title
                if (this.titles && this.titles.length > 0 && this.showTitles) {
                    let currentLength:number = this.titles[column].length;
                    if (currentLength > maxLength) {
                        maxLength = currentLength;
                    }
                }

                // for content
                let columnData:Array<string> = this.columns[column];
                columnData.forEach((row) => {
                    let currentLength:number = row.length;
                    if (currentLength > maxLength) {
                        maxLength = currentLength;
                    }
                });
                lengths.push(maxLength);
            }
        }

        return lengths;

    }

    public withFormatter(formatterMode:FormatterMode):AsciiArray {
        if (!formatterMode) {
            return this;
        }

        if (formatterMode !== 'CSV' && formatterMode !== 'MODERN') {
            throw new Error("The formatter mode " + formatterMode + " is not supported. Current support is CSV or MODERN.")
        }

        this.formatterMode = formatterMode;
        return this;
    }

    /**
     * Show or not the title when getting result
     * @param showTitles if true, display it
     * @returns {DefaultAsciiArray}
     */
    withShowTitles(showTitles:boolean):AsciiArray {
        if (!showTitles) {
            this.showTitles = false;
        }
        return this;
    }

    /**
     * Specify the order and columns that need to be displayed.
     * @param columnTitles an array of name of columns
     */
    withFormatColumnTitles(...columnTitles:Array<string>):AsciiArray {
        this.formatColumnTitles = columnTitles;
        return this;
    }


    /**
     * Specify the order and columns that need to be displayed.
     * @param columnNames a comma separated list of columns
     */
    withFormatColumns(columnNames:string):AsciiArray {
        if (columnNames) {
            this.formatColumnTitles = columnNames.split(",");
        }
        return this;
    }


}

/**
 * Inner class with column size data.
 */
export class MyAsciiArrayInfo implements AsciiArrayInfo {
    private array:DefaultAsciiArray;

    constructor(array:DefaultAsciiArray) {
        this.array = array;
    }

    public getColumnsSize():Array<number> {
        return this.array.getColumnsSize();
    }
}
