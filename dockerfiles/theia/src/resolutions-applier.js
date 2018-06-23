"use strict";
/*
 * Copyright (c) 2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */

const fs = require("fs");

const RESOLUTIONS_JSON_PATH = process.argv[2];
const PACKAGE_JSON_PATH = process.argv[3];

const RESOLITIONS = require(RESOLUTIONS_JSON_PATH);
const PACKAGE_JSON = require(PACKAGE_JSON_PATH);

PACKAGE_JSON["resolutions"] = RESOLITIONS;
writeJsonToFile(PACKAGE_JSON_PATH, PACKAGE_JSON);

/**
 * Write json to the file by path.
 * 
 * @param filePath - file system location of the file.
 * @param json - json object to write.
 */
function writeJsonToFile(filePath, json) {
    const content = JSON.stringify(json, undefined, 4);
    fs.writeFileSync(filePath, content, 'utf8');
}
