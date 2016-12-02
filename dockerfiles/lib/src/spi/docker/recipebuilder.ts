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
                    if (cheStructWorkspace.runtime.docker.image) {
                        return {"contentType": "text/x-dockerfile", "type": "dockerimage", "location": cheStructWorkspace.runtime.docker.image};
                    } else if (cheStructWorkspace.runtime.docker.content) {
                        return {
                            "contentType": "text/x-dockerfile",
                            "type": "dockerfile",
                            "content": cheStructWorkspace.runtime.docker.content
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
