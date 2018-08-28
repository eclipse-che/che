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

import {Log} from "../log/log";
import {CheFileStructWorkspace} from "../../internal/dir/chefile-struct/che-file-struct";
/**
 * Build a default recipe.
 * @author Florent Benoit
 */
export class RecipeBuilder {

    static DEFAULT_DOCKERFILE_CONTENT: string = 'FROM codenvy/ubuntu_jdk8';
    path: any;
    fs: any;
    currentFolder : any;

    constructor(currentFolder) {
        this.path = require('path');
        this.fs = require('fs');
        this.currentFolder = currentFolder;
    }


    getRecipe(cheStructWorkspace : CheFileStructWorkspace) : any {

        // do we have a custom property in Chefile
        if (cheStructWorkspace) {
            if (cheStructWorkspace.runtime) {
                if (cheStructWorkspace.runtime.docker) {

                    // make it backward compliant
                    if (cheStructWorkspace.runtime.docker.content) {
                        cheStructWorkspace.runtime.docker.dockerfile = cheStructWorkspace.runtime.docker.content;
                    }

                    if (cheStructWorkspace.runtime.docker.image) {
                        return {"contentType": "text/x-dockerfile", "type": "dockerimage", "content": cheStructWorkspace.runtime.docker.image};
                    } else if (cheStructWorkspace.runtime.docker.content) {
                        return {
                            "contentType": "text/x-dockerfile",
                            "type": "dockerfile",
                            "content": cheStructWorkspace.runtime.docker.content
                        };
                    } else if (cheStructWorkspace.runtime.docker.dockerfile) {
                        return {
                            "contentType": "text/x-dockerfile",
                            "type": "dockerfile",
                            "content": cheStructWorkspace.runtime.docker.dockerfile
                        };
                    } else if (cheStructWorkspace.runtime.docker.composefile) {
                        return {
                            "contentType": "application/x-yaml",
                            "type": "compose",
                            "content": cheStructWorkspace.runtime.docker.composefile
                        };
                    } else if (cheStructWorkspace.runtime.docker.location) {
                        return {
                            "contentType": "text/x-dockerfile",
                            "type": "recipe",
                            "location": cheStructWorkspace.runtime.docker.location
                        };
                    }
                }
            }
        }

        // build path to the Dockerfile in current directory
        var dockerFilePath = this.path.resolve(this.currentFolder, 'Dockerfile');

        // use synchronous API
        try {
            var stats = this.fs.statSync(dockerFilePath);
            Log.getLogger().info('Using a custom project Dockerfile \'' + dockerFilePath + '\' for the setup of the workspace.');
            var content = this.fs.readFileSync(dockerFilePath, 'utf8');
            return {"contentType": "text/x-dockerfile", "type": "dockerfile", "content": content};
        } catch (e) {
            // file does not exist, return default
            return {"contentType": "text/x-dockerfile", "type": "dockerfile", "content": RecipeBuilder.DEFAULT_DOCKERFILE_CONTENT} ;
        }

    }



}
