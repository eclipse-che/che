/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.commons.lang;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utils for ZIP.
 *
 * @author Eugene Voevodin
 * @author Sergii Kabashniuk
 * @author Thomas Mäder
 */
public class ZipUtils {
  private static final int BUF_SIZE = 4096;

  private static class ExceptionWrapper extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ExceptionWrapper(Throwable cause) {
      super(cause);
    }
  }

  /**
   * Creates a {@link ZipOutputStream} with proper buffering and options
   *
   * @param zip
   * @return a newly opened stream
   * @throws FileNotFoundException
   */
  public static ZipOutputStream stream(Path zip) throws FileNotFoundException {
    ZipOutputStream result =
        new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zip.toFile())));
    result.setLevel(0);
    return result;
  }

  /**
   * This is equivalent to writing {@code #add(out, f, f.getParent())}
   *
   * @param out the stream to write to
   * @param f the file or directory
   * @throws IOException
   */
  public static void add(ZipOutputStream out, Path f) throws IOException {
    add(out, f, f.getParent());
  }

  /**
   * Recursively add the contents of the given file or directory to a {@link ZipOutputStream}
   *
   * @param out The zip file to add to
   * @param f the file or directory to add to
   * @param rootPath The path the zip entries are relative to. If f is /a/b/c and rootPath is /a,
   *     the entry corresponding to f will be named b/c. Must be a prefix of f.
   * @throws IOException
   */
  public static void add(ZipOutputStream out, Path f, Path rootPath) throws IOException {
    if (!f.startsWith(rootPath)) {
      throw new IllegalArgumentException(
          "'" + String.valueOf(rootPath) + "' is not a prefix of '" + String.valueOf(f) + "'");
    }
    if (Files.isDirectory(f)) {
      addDirectory(out, f, rootPath);
    } else {
      addFileEntry(out, relativePath(rootPath, f), f.toFile());
    }
  }

  private static void addDirectory(ZipOutputStream out, Path d, Path root) throws IOException {
    if (!root.equals(d)) {
      addDirectoryEntry(out, relativePath(root, d));
    }
    try (Stream<Path> entries = Files.list(d)) {
      entries.forEach(
          path -> {
            try {
              add(out, path, root);
            } catch (IOException e) {
              throw new ExceptionWrapper(e);
            }
          });
    } catch (ExceptionWrapper e) {
      throw (IOException) e.getCause();
    }
  }

  private static String relativePath(Path root, Path f) {
    return root.relativize(f).toString().replaceAll(File.pathSeparator, "/");
  }

  private static void addDirectoryEntry(ZipOutputStream zipOut, String entryName)
      throws IOException {
    final ZipEntry zipEntry = new ZipEntry(entryName.endsWith("/") ? entryName : (entryName + '/'));
    zipOut.putNextEntry(zipEntry);
    zipOut.closeEntry();
  }

  private static void addFileEntry(ZipOutputStream zipOut, String entryName, File file)
      throws IOException {
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
   * @param zip zip file to get resources from
   * @param filter the search criteria
   * @throws IOException
   */
  public static void getResources(ZipFile zip, Pattern filter, Consumer<InputStream> consumer)
      throws IOException {
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
   * Checks is specified file is zip file or not. Zip file <a
   * href="http://en.wikipedia.org/wiki/Zip_(file_format)#File_headers">headers description</a>.
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

  private ZipUtils() {}
}
