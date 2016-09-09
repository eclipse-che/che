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
package org.eclipse.che.commons.lang;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utils for ZIP.
 *
 * @author Eugene Voevodin
 * @author Sergii Kabashniuk
 */
public class ZipUtils {
    private static final int BUF_SIZE = 4096;

    public static void zipDir(String parentPath, File dir, File zip, FilenameFilter filter) throws IOException {
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Not a directory.");
        }
        if (!dir.getAbsolutePath().startsWith(parentPath)) {
            throw new IllegalArgumentException("Invalid parent directory path " + parentPath);
        }
        if (filter == null) {
            filter = IoUtil.ANY_FILTER;
        }
        try (ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zip)))) {
            zipOut.setLevel(0); // TODO: move in parameters of method
            addDirectoryRecursively(zipOut, parentPath, dir, filter);
            zipOut.finish();
        }
    }

    /**
     * Create an output ZIP stream and add each file to that stream. Directory files are added recursively. The contents
     * of the zip are written to the given file.
     *
     * @param zip
     *            The file to write the zip contents to.
     * @param files
     *            The files to add to the zip stream.
     * @throws IOException
     */
    public static void zipFiles(File zip, File... files) throws IOException {
        try (BufferedOutputStream bufferedOut = new BufferedOutputStream(new FileOutputStream(zip))) {
            zipFiles(bufferedOut, files);
        }
    }

    /**
     * Create an output ZIP stream and add each file to that stream. Directory files are added recursively. The contents
     * of the zip are written to the given stream.
     *
     * @param output
     *            The stream to write the zip contents to.
     * @param files
     *            The files to add to the zip stream.
     * @throws IOException
     */
    public static void zipFiles(OutputStream output, File... files) throws IOException {
        try (ZipOutputStream zipOut = new ZipOutputStream(output)) {
            for (File f : files) {
                if (f.isDirectory()) {
                    addDirectoryEntry(zipOut, f.getName());
                    final String parentPath = f.getParentFile().getAbsolutePath();
                    addDirectoryRecursively(zipOut, parentPath, f, IoUtil.ANY_FILTER);
                } else if (f.isFile()) {
                    addFileEntry(zipOut, f.getName(), f);
                }
            }
        }
    }

    private static void addDirectoryRecursively(ZipOutputStream zipOut, String parentPath, File dir, FilenameFilter filter)
            throws IOException {
        final int parentPathLength = parentPath.length() + 1;
        final LinkedList<File> q = new LinkedList<>();
        q.add(dir);
        while (!q.isEmpty()) {
            final File current = q.pop();
            final File[] list = current.listFiles();
            if (list != null) {
                for (File f : list) {
                    if (filter.accept(current, f.getName())) {
                        final String entryName = f.getAbsolutePath().substring(parentPathLength).replace('\\', '/');
                        if (f.isDirectory()) {
                            addDirectoryEntry(zipOut, entryName);
                            q.push(f);
                        } else if (f.isFile()) {
                            addFileEntry(zipOut, entryName, f);
                        }
                    }
                }
            }
        }
    }

    private static void addDirectoryEntry(ZipOutputStream zipOut, String entryName) throws IOException {
        final ZipEntry zipEntry = new ZipEntry(entryName.endsWith("/") ? entryName : (entryName + '/'));
        zipOut.putNextEntry(zipEntry);
        zipOut.closeEntry();
    }

    private static void addFileEntry(ZipOutputStream zipOut, String entryName, File file) throws IOException {
        final ZipEntry zipEntryEntry = new ZipEntry(entryName);
        zipOut.putNextEntry(zipEntryEntry);
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            final byte[] buf = new byte[BUF_SIZE];
            int r;
            while ((r = in.read(buf)) != -1) {
                zipOut.write(buf, 0, r);
            }
        }
        zipOut.closeEntry();
    }

    public static Collection<String> listEntries(File zip) throws IOException {
        try (InputStream in = new FileInputStream(zip)) {
            return listEntries(in);
        }
    }

    public static Collection<String> listEntries(InputStream in) throws IOException {
        final List<String> list = new LinkedList<>();
        final ZipInputStream zipIn = new ZipInputStream(in);
        ZipEntry zipEntry;
        while ((zipEntry = zipIn.getNextEntry()) != null) {
            if (!zipEntry.isDirectory()) {
                list.add(zipEntry.getName());
            }
            zipIn.closeEntry();
        }
        return list;
    }

    public static void unzip(File zip, File targetDir) throws IOException {
        try (InputStream in = new FileInputStream(zip)) {
            unzip(in, targetDir);
        }
    }

    public static void unzip(InputStream in, File targetDir) throws IOException {
        final ZipInputStream zipIn = new ZipInputStream(in);
        final byte[] b = new byte[BUF_SIZE];
        ZipEntry zipEntry;
        while ((zipEntry = zipIn.getNextEntry()) != null) {
            final File file = new File(targetDir, zipEntry.getName());
            if (!zipEntry.isDirectory()) {
                final File parent = file.getParentFile();
                if (!parent.exists()) {
                    if (!parent.mkdirs()) {
                        throw new IOException("Unable to create parent folder " + parent.getAbsolutePath());
                    }

                }
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    int r;
                    while ((r = zipIn.read(b)) != -1) {
                        fos.write(b, 0, r);
                    }
                }
            } else {
                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        throw new IOException("Unable to create folder " + file.getAbsolutePath());
                    }
                }

            }
            zipIn.closeEntry();
        }
    }

    /**
     * Provides streams to all resources matching {@code filter} criteria inside the archive.
     *
     * @param zip
     *      zip file to get resources from
     * @param filter
     *      the search criteria
     * @throws IOException
     */
    public static void getResources(ZipFile zip, Pattern filter, Consumer<InputStream> consumer) throws IOException {
        Enumeration<? extends ZipEntry> zipEntries = zip.entries();
        while (zipEntries.hasMoreElements()) {
            ZipEntry zipEntry = zipEntries.nextElement();
            final String name = zipEntry.getName();
            if (filter.matcher(name).matches()) {
                try (InputStream in = zip.getInputStream(zipEntry)) {
                    consumer.accept(in);
                }
            }
        }
    }

    /**
     * Checks is specified file is zip file or not. Zip file <a href="http://en.wikipedia.org/wiki/Zip_(file_format)#File_headers">headers
     * description</a>.
     */
    public static boolean isZipFile(File file) throws IOException {
        if (file.isDirectory()) {
            return false;
        }
        // NOTE: little-indian bytes order!
        final byte[] bytes = new byte[4];
        try (FileInputStream fIn = new FileInputStream(file)) {
            if (fIn.read(bytes) != bytes.length) {
                return false;
            }
        }

        ByteBuffer zipFileHeaderSignature = ByteBuffer.wrap(bytes);
        zipFileHeaderSignature.order(ByteOrder.LITTLE_ENDIAN);
        return 0x04034b50 == zipFileHeaderSignature.getInt();
    }

    private ZipUtils() {
    }
}
