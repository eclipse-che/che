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
package org.eclipse.che.api.vfs.server.util;

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
    private static final int  BUFFER        = 100 * 1024; // 100k
    private static final int  BUFFER_SIZE   = 8 * 1024; // 8k
    /** The threshold after that checking of ZIP ratio started. */
    private static final long ZIP_THRESHOLD = 1000000;
    /**
     * Max compression ratio. If the number of bytes uncompressed data is exceed the number
     * of bytes of compressed stream more than this ratio (and number of uncompressed data
     * is more than threshold) then VirtualFileSystemRuntimeException is thrown.
     */
    private static final int  ZIP_RATIO     = 100;

    public static ZipContent newInstance(InputStream in) throws IOException {
        java.io.File file = null;
        byte[] inMemory = null;

        int count = 0;
        ByteArrayOutputStream inMemorySpool = new ByteArrayOutputStream(BUFFER);

        int bytes;
        final byte[] buff = new byte[BUFFER_SIZE];
        while (count <= BUFFER && (bytes = in.read(buff)) != -1) {
            inMemorySpool.write(buff, 0, bytes);
            count += bytes;
        }

        InputStream spool;
        if (count > BUFFER) {
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

        // Counts numbers of compressed data.
        try (CountingInputStream compressedCounter = new CountingInputStream(spool);
             ZipInputStream zip = new ZipInputStream(compressedCounter)) {

            class UncompressedCounterInputStream extends CountingInputStream {

                public UncompressedCounterInputStream(InputStream in) {
                    super(in);
                }

                @Override
                public int read() throws IOException {
                    int i = super.read();
                    checkCompressionRatio();
                    return i;
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    int i = super.read(b, off, len);
                    checkCompressionRatio();
                    return i;
                }

                @Override
                public int read(byte[] b) throws IOException {
                    int i = super.read(b);
                    checkCompressionRatio();
                    return i;
                }

                @Override
                public long skip(long length) throws IOException {
                    long i = super.skip(length);
                    checkCompressionRatio();
                    return i;
                }

                private void checkCompressionRatio() {
                    long uncompressedBytes = getByteCount(); // number of uncompressed bytes
                    if (uncompressedBytes > ZIP_THRESHOLD) {
                        long compressedBytes = compressedCounter.getByteCount(); // number of compressed bytes
                        if (uncompressedBytes > (ZIP_RATIO * compressedBytes)) {
                            throw new RuntimeException("Zip bomb detected. ");
                        }
                    }
                }
            }

            // Counts number of uncompressed data.
            try (CountingInputStream uncompressedCounter = new UncompressedCounterInputStream(zip)) {
                ZipEntry zipEntry;
                while ((zipEntry = zip.getNextEntry()) != null) {
                    if (!zipEntry.isDirectory()) {
                        while (uncompressedCounter.read(buff) != -1) {
                            // Read full data from stream to be able detect zip-bomb.
                        }
                    }
                }
            }

            return new ZipContent(inMemory != null ? new ByteArrayInputStream(inMemory) : new DeleteOnCloseFileInputStream(file),
                                  file == null);
        }
    }

    public final InputStream zippedData;
    public final boolean     inMemory;

    private ZipContent(InputStream zippedData, boolean inMemory) {
        this.zippedData = zippedData;
        this.inMemory = inMemory;
    }
}
