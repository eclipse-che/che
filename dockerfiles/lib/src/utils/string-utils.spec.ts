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
import {StringUtils} from './string-utils';

let expect = require('chai').expect;
let vm = require('vm');

describe("String Utils tests", () => {
    it("not included", () => {
        expect(StringUtils.startsWith("toto", 'n')).to.be.false;
    });

    it("starts with", () => {
        expect(StringUtils.startsWith("toto", "t")).to.be.true;
    });


    it("remove # comments", () => {
        let text : string = "This is a string\n" + "# THis is a comment\n" + " and here it is not a # comment";
        let expectedText : string = "This is a string\n\n" + " and here it is not a # comment";
        expect(StringUtils.removeSharpComments(text)).to.equal(expectedText);
    });


    it("Check dockerfile are handled with raw strings", () => {
        let workspace : any = {};
        workspace.content = "";
        let text : string = "workspace.content=\`" + String.raw`
            FROM Image

        RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        locales \
        openssh-server \
        sudo && \
    rm -rf /var/lib/apt/lists/* && \
 mkdir /var/run/sshd && \
 sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd && \
 echo "%sudo ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers && \
 useradd -u 1000 -G users,sudo -p $(openssl rand -base64 32) -s /bin/bash -m user && \
 echo "#! /bin/bash\n set -e\n sudo /usr/sbin/sshd -D &\n exec \"\$@\"" > /home/user/entrypoint.sh && \
 chmod a+x /home/user/entrypoint.sh
`+ "\`";

        text = StringUtils.keepWorkspaceRawStrings(text);

        // create sandboxed object
        var sandbox = {  "workspace": workspace};
        vm.runInNewContext(text, sandbox);

        expect(workspace.content).to.contain(String.raw`s@session\s*required\s*`);
    });


});
