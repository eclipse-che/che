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
package org.eclipse.che.api.vfs;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Archiver for compressing and extracting content of folder. */
public abstract class Archiver {
    protected final VirtualFile folder;

    protected Archiver(VirtualFile folder) {
        this.folder = folder;
    }

    /**
     * Write compressed content of folder to specified output.
     *
     * @param compressOutput
     *         output for compressed content
     */
    public abstract void compress(OutputStream compressOutput) throws IOException, ServerException;

    /**
     * Write compressed content of folder to specified output.
     *
     * @param compressOutput
     *         output for compressed content
     * @param filter
     *         only files that match to this filter are written in {@code compressOutput}
     */
    public abstract void compress(OutputStream compressOutput, VirtualFileFilter filter) throws IOException, ServerException;

    /**
     * Extract compressed content to {@code folder}.
     *
     * @param compressedInput
     *         compressed content that needed to be extracted
     * @param overwrite
     *         overwrite existing files
     * @param stripNumber
     *         strip number leading components from file names on extraction.
     */
    public abstract void extract(InputStream compressedInput, boolean overwrite, int stripNumber)
            throws IOException, ForbiddenException, ConflictException, ServerException;
}
