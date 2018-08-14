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
import {DefaultAsciiForm} from "./default-ascii-form";
import {AsciiForm} from "./ascii-form";
let expect = require('chai').expect;

/**
 * Provides Unit Tests for AsciiForm
 * @author Florent Benoit
 */

describe("AsciiForm tests", () => {

    it("testEmptyForm", () => {

        let asciiForm:DefaultAsciiForm = new DefaultAsciiForm();
        let result:string = asciiForm.toAscii();
        expect(result.length).to.equal(0);
        expect(result).to.equal("");
    });


    it("testOneLineForm", () => {

        let asciiForm:AsciiForm = new DefaultAsciiForm().withEntry("id", "value1");
        let result:string = asciiForm.toAscii();
        expect(result.length).to.above(0);
        expect(result).to.equal("id value1");
    });



    it("testThreeLinesFormUppercase", () => {

        let asciiForm:AsciiForm = new DefaultAsciiForm().withEntry("id", "value1").withEntry("a very long Id", "123456789").withEntry("short id", "abc").withUppercasePropertyName();
        let result:string = asciiForm.toAscii();
        expect(result.length).to.above(0);
        expect(result).to.equal("ID             value1\n" + "A VERY LONG ID 123456789\n" +
            "SHORT ID       abc");
    });


});
