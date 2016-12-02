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
