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

import org.apache.commons.io.input.CountingInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** @author andrew00x */
public final class ZipContent {
    /** Memory threshold. If zip stream over this size it spooled in file. */
    private static final int  KEEP_IN_MEMORY_THRESHOLD = 200 * 1024;
    private static final int  COPY_BUFFER_SIZE         = 8 * 1024;
    /** The threshold after that checking of ZIP ratio started. */
    private static final long ZIP_THRESHOLD            = 1000000;
    /**
     * Max compression ratio. If the number of bytes uncompressed data is exceed the number
     * of bytes of compressed stream more than this ratio (and number of uncompressed data
     * is more than threshold) then IOException is thrown.
     */
    private static final int  ZIP_RATIO                = 100;

    public static ZipContent of(InputStream in) throws IOException {
        java.io.File file = null;
        byte[] inMemory = null;

        int count = 0;
        ByteArrayOutputStream inMemorySpool = new ByteArrayOutputStream(KEEP_IN_MEMORY_THRESHOLD);

        int bytes;
        final byte[] buff = new byte[COPY_BUFFER_SIZE];
        while (count <= KEEP_IN_MEMORY_THRESHOLD && (bytes = in.read(buff)) != -1) {
            inMemorySpool.write(buff, 0, bytes);
            count += bytes;
        }

        InputStream spool;
        if (count > KEEP_IN_MEMORY_THRESHOLD) {
            file = java.io.File.createTempFile("import", ".zip");
            try (FileOutputStream fileSpool = new FileOutputStream(file)) {
                inMemorySpool.writeTo(fileSpool);
                while ((bytes = in.read(buff)) != -1) {
                    fileSpool.write(buff, 0, bytes);
                }
            }
            spool = new FileInputStream(file);
        } else {
            inMemory = inMemorySpool.toByteArray();
            spool = new ByteArrayInputStream(inMemory);
        }

        try (CountingInputStream compressedDataCounter = new CountingInputStream(spool);
             ZipInputStream zip = new ZipInputStream(compressedDataCounter)) {
            try (CountingInputStream uncompressedDataCounter = new CountingInputStream(zip)) {
                ZipEntry zipEntry;
                while ((zipEntry = zip.getNextEntry()) != null) {
                    if (!zipEntry.isDirectory()) {
                        while (uncompressedDataCounter.read(buff) != -1) {
                            long uncompressedBytes = uncompressedDataCounter.getByteCount();
                            if (uncompressedBytes > ZIP_THRESHOLD) {
                                long compressedBytes = compressedDataCounter.getByteCount();
                                if (uncompressedBytes > (ZIP_RATIO * compressedBytes)) {
                                    throw new IOException("Zip bomb detected");
                                }
                            }
                        }
                    }
                }
            }

            return new ZipContent(inMemory == null ? new DeleteOnCloseFileInputStream(file) : new ByteArrayInputStream(inMemory));
        }
    }

    private final InputStream zipContent;

    private ZipContent(InputStream zipContent) {
        this.zipContent = zipContent;
    }

    public InputStream getContent() {
        return zipContent;
    }
}
