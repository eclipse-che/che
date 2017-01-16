/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.vfs.util;

import org.eclipse.che.api.core.util.FileCleaner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Delete java.io.File after closing.
 *
 * @author andrew00x
 */
public final class DeleteOnCloseFileInputStream extends FileInputStream {
    private final java.io.File file;

    public DeleteOnCloseFileInputStream(java.io.File file) throws FileNotFoundException {
        super(file);
        this.file = file;
    }

    /** @see java.io.FileInputStream#close() */
    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            FileCleaner.addFile(file);
        }
    }
}