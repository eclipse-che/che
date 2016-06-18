/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.project.node;

import javax.validation.constraints.NotNull;

/**
 * Represent storable path for any object that support storing.
 * For example for file or folder it will be the path from root of the workspace.
 *
 * @author Vlad Zhukovskiy
 */
public interface HasStorablePath {

    /** Default implementation of the {@link HasStorablePath} */
    public class StorablePath implements HasStorablePath {

        private String path;

        public StorablePath(String path) {
            this.path = path;
        }

        @NotNull
        @Override
        public String getStorablePath() {
            return path;
        }
    }

    /**
     * Storable path for the element. For files and folder this may be path which starts from the root of the workspace.
     *
     * @return path for the element
     */
    @NotNull
    String getStorablePath();
}
