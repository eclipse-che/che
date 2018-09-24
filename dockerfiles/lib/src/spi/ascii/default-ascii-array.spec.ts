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
import {DefaultAsciiArray} from "./default-ascii-array";
import {AsciiArray} from "./ascii-array";
let expect = require('chai').expect;

/**
 * Provides Unit Tests for AsciiArray
 * @author Florent Benoit
 */
describe("AsciiArray tests", () => {

    it("testEmptyArray", () => {

        let asciiArray:DefaultAsciiArray = new DefaultAsciiArray();
        let result:string = asciiArray.toAscii();
        expect(result.length).to.equal(0);
        expect(result).to.equal("");
    });

    it("testColumnsSize", () => {

        let column1:Array<string> = ["a", "ab", "abc", "a"];

        let asciiArray:DefaultAsciiArray = new DefaultAsciiArray();
        asciiArray.withColumns(column1);
        let columnsSize:Array<number> = asciiArray.getColumnsSize();

        expect(columnsSize).to.exist;
        expect(columnsSize.length).to.equal(1);
        expect(columnsSize[0]).to.equal(3);
    });


    it("testColumnsSizeTwoColumns", () => {

        let column1:Array<string> = ["a", "ab", "abcdef", " abcdef "];
        let column2:Array<string> = ["defgh", "d", "e", "f"];

        let asciiArray:DefaultAsciiArray = new DefaultAsciiArray();
        asciiArray.withColumns(column1, column2);
        let columnsSize:Array<number> = asciiArray.getColumnsSize();

        expect(columnsSize).to.exist;
        expect(columnsSize.length).to.equal(2);
        expect(columnsSize[0]).to.equal(8);
        expect(columnsSize[1]).to.equal(5);
    });


    it("testColumnsSizeWihTitle", () => {

        let column1:Array<string> = ["a", "ab", "abcdef", " abcdef "];
        let column2:Array<string> = ["defgh", "d", "e", "f"];

        let asciiArray:DefaultAsciiArray = new DefaultAsciiArray();
        asciiArray.withColumns(column1, column2).withTitles("Col1", " My Column 2");
        let columnsSize:Array<number> = asciiArray.getColumnsSize();

        expect(columnsSize).to.exist;
        expect(columnsSize.length).to.equal(2);
        expect(columnsSize[0]).to.equal(8);
        expect(columnsSize[1]).to.equal(12);
    });


    it("testTwoColumnsWithTitleCsvFormatter", () => {

        let column1:Array<string> = ["row1", "row2", "row3"];
        let column2:Array<string> = ["1", "2", "3"];

        let asciiArray:DefaultAsciiArray = new DefaultAsciiArray();
        asciiArray.withColumns(column1, column2).withTitles("name", "id").withFormatter("CSV");

        let result:string = asciiArray.toAscii();

        expect(result).to.equal("NAME,ID\nrow1,1\nrow2,2\nrow3,3");
    });

    it("testTwoRowsWithTitleCsvFormatter", () => {

        let row1:Array<string> = ["hello1", "hello2", "hello3"];
        let row2:Array<string> = ["another1", "another2", "another3"];

        let asciiArray:DefaultAsciiArray = new DefaultAsciiArray();
        asciiArray.withListRows(row1, row2).withTitles("name1", "name2", "name3").withFormatter("CSV");

        let result:string = asciiArray.toAscii();

        expect(result).to.equal("NAME1,NAME2,NAME3\nhello1,hello2,hello3\nanother1,another2,another3");
    });


    it("testTwoColumnsWithTitleModernFormatter", () => {

        let column1:Array<string> = ["row1", "row2", "row3"];
        let column2:Array<string> = ["1", "2", "3"];

        let asciiArray:DefaultAsciiArray = new DefaultAsciiArray();
        asciiArray.withColumns(column1, column2).withTitles("name", "id").withFormatter("MODERN");

        let result:string = asciiArray.toAscii();

        expect(result).to.equal("NAME  ID  \nrow1  1   \nrow2  2   \nrow3  3   ");
    });


    it("testTwoRowsWithTitleModernFormatter", () => {

        let row1:Array<string> = ["hello1", "hello2", "hello3"];
        let row2:Array<string> = ["another1", "another2", "another3"];

        let asciiArray:DefaultAsciiArray = new DefaultAsciiArray();
        asciiArray.withListRows(row1, row2).withTitles("name1", "name2", "name3").withFormatter("MODERN");

        let result:string = asciiArray.toAscii();

        expect(result).to.equal("NAME1     NAME2     NAME3     \nhello1    hello2    hello3    \nanother1  another2  another3  ");
    });


    it("testTwoRowsWithTitleLongDataColumnModernFormatter", () => {

        let row1:Array<string> = ["local", "workspace1x42j3pjczrnnqdr", "RUNNING"];

        let asciiArray:DefaultAsciiArray = new DefaultAsciiArray();
        asciiArray.withListRows(row1).withTitles("name", "id", "status").withFormatter("MODERN");

        let result:string = asciiArray.toAscii();

        expect(result).to.equal("NAME   ID                         STATUS   \nlocal  workspace1x42j3pjczrnnqdr  RUNNING  ");
    });


    it("testSkipTitleCsvFormatter", () => {

        let row1:Array<string> = ["id1", "name1", "status1"];

        let asciiArray:DefaultAsciiArray = new DefaultAsciiArray();
        asciiArray.withListRows(row1).withTitles("id", "name", "status very long").withShowTitles(false).withFormatter('CSV');

        let result:string = asciiArray.toAscii();

        expect(result).to.equal("id1,name1,status1");
    });


    it("testCustomOrderingCsvFormatter", () => {

        let row1:Array<string> = ["id1", "name1", "status1"];
        let row2:Array<string> = ["id2", "name2", "status2"];

        let asciiArray:DefaultAsciiArray = new DefaultAsciiArray();
        asciiArray.withListRows(row1, row2).withTitles("id", "name", "status").withFormatter("CSV").withFormatColumnTitles("status", "id");

        let result:string = asciiArray.toAscii();

        expect(result).to.equal("STATUS,ID\nstatus1,id1\nstatus2,id2");
    });


    it("testCustomOrderingCommaSeparatedCsvFormatter", () => {

        let row1:Array<string> = ["id1", "name1", "status1"];
        let row2:Array<string> = ["id2", "name2", "status2"];

        let asciiArray:DefaultAsciiArray = new DefaultAsciiArray();
        asciiArray.withListRows(row1, row2).withTitles("id", "name", "status").withFormatter("CSV").withFormatColumns("status,NAME");

        let result:string = asciiArray.toAscii();

        expect(result).to.equal("STATUS,NAME\nstatus1,name1\nstatus2,name2");
    });


});
