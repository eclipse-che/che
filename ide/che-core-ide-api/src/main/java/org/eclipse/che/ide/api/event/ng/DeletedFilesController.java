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
package org.eclipse.che.ide.api.event.ng;

import com.google.inject.Singleton;

import java.util.HashSet;
import java.util.Set;

/**
 * Contains paths to opened files which are needed to skip for the updating process.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class DeletedFilesController {
    private Set<String> deletedFiles = new HashSet<>();

    /**
     * Adds the path to the file which need to skip.
     *
     * @param path
     *         path to the file
     */
    public void add(String path) {
        deletedFiles.add(path);
    }

    /**
     * Removes the path to the file which need to skip.
     *
     * @param path
     *         path to the file
     *
     * @return {@code true} if set contains the specified path
     */
    public boolean remove(String path) {
        return deletedFiles.remove(path);
    }

    /** Returns {@code true} if this set contains the specified path. */
    public boolean contains(String path) {
        return deletedFiles.contains(path);
    }
}
