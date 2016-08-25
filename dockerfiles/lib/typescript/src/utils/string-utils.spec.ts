/// <reference path='../typings/tsd.d.ts' />

import {StringUtils} from './string-utils';

let expect = require('chai').expect;


describe("String Utils tests", () => {
    it("not included", () => {
        expect(StringUtils.startsWith("toto", 'n')).to.be.false;
    });

    it("starts with", () => {
        expect(StringUtils.startsWith("toto", "t")).to.be.true;
    });
});
