/*
 * Copyright (c) 2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */

/**
 * Run in the container the command to get image being launched so we can detect the version of the image
 * @author Florent Benoit
 */
export class ContainerVersion {

    static version: string;

    constructor() {
        if (!ContainerVersion.version) {
            let execSync = require('child_process').execSync;
            ContainerVersion.version = execSync("docker inspect --format='{{.Config.Image}}' `hostname` | cut -d : -f2 -s").toString().replace(/[\n\r]/g, '');
        }
    }


    getVersion() : string {
        return ContainerVersion.version;
    }

}
