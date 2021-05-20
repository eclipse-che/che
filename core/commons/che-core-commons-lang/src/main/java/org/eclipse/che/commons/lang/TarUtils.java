/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

/** @author andrew00x */
public class TarUtils {
  private static final int BUF_SIZE = 4096;

  /**
   * Add content of directory {@code dir} to tar archive {@code tar}.
   *
   * @param parentPath parent path of tar archive. Typically if need add only content of {@code dir}
   *     this path should be absolute path to {@code dir} but if need to have in archive some
   *     parents this parameter may be used. For example if need add to archive content of directory
   *     '/a/b/c' but need to save directory 'c' in path:
   *     <pre>
   *                         {@code File dir = new File("a/b/c");
   *                      File tar = new File("archive.tar");
   *                      TarUtils.tarDir(dir.getParentFile().getAbsolutePath(), dir, tar, -1, IoUtil.ANY_FILTER);
   *                         }
   *                         </pre>
   *     In this case directory 'c' is added in tar archive.
   * @param dir dir to add
   * @param tar tar archive
   * @param modTime modification time that applied to all entries in archive instead modification
   *     time provided by method {@link File#lastModified()}. This parameter should be {@code -1} if
   *     don't need to set any specified time
   * @param filter optional filter for files to add in archive
   * @throws IOException if i/o error occurs
   * @throws IllegalArgumentException if {@code dir} is not directory or if {@code parentPath} is
   *     invalid, e.g. is neither parent nor equals to path of {@code dir}
   */
  public static void tarDir(
      String parentPath, File dir, File tar, long modTime, FilenameFilter filter)
      throws IOException {
    if (!dir.isDirectory()) {
      throw new IllegalArgumentException("Not a directory.");
    }
    if (!dir.getAbsolutePath().startsWith(parentPath)) {
      throw new IllegalArgumentException("Invalid parent directory path " + parentPath);
    }
    if (filter == null) {
      filter = IoUtil.ANY_FILTER;
    }
    try (TarArchiveOutputStream tarOut =
        new TarArchiveOutputStream(new BufferedOutputStream(new FileOutputStream(tar)))) {
      tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
      addDirectoryRecursively(tarOut, parentPath, dir, modTime, filter);
    }
  }

  public static void tarDir(String parentPath, File dir, File tar, FilenameFilter filter)
      throws IOException {
    tarDir(parentPath, dir, tar, -1, filter);
  }

  public static void tarFiles(File tar, long modTime, File... files) throws IOException {
    try (TarArchiveOutputStream tarOut =
        new TarArchiveOutputStream(new BufferedOutputStream(new FileOutputStream(tar)))) {
      tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
      for (File f : files) {
        if (f.isDirectory()) {
          addDirectoryEntry(tarOut, f.getName(), f, modTime);
          final String parentPath = f.getParentFile().getAbsolutePath();
          addDirectoryRecursively(tarOut, parentPath, f, modTime, IoUtil.ANY_FILTER);
        } else if (f.isFile()) {
          addFileEntry(tarOut, f.getName(), f, modTime);
        }
      }
    }
  }

  public static void tarFiles(File tar, File... files) throws IOException {
    tarFiles(tar, -1, files);
  }

  private static void addDirectoryRecursively(
      TarArchiveOutputStream tarOut,
      String parentPath,
      File dir,
      long modTime,
      FilenameFilter filter)
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
            final String entryName =
                f.getAbsolutePath().substring(parentPathLength).replace('\\', '/');
            if (f.isDirectory()) {
              addDirectoryEntry(tarOut, entryName, f, modTime);
              q.push(f);
            } else if (f.isFile()) {
              addFileEntry(tarOut, entryName, f, modTime);
            }
          }
        }
      }
    }
  }

  private static void addDirectoryEntry(
      TarArchiveOutputStream tarOut, String entryName, File directory, long modTime)
      throws IOException {
    final TarArchiveEntry tarEntry = new TarArchiveEntry(directory, entryName);
    if (modTime >= 0) {
      tarEntry.setModTime(modTime);
    }
    tarOut.putArchiveEntry(tarEntry);
    tarOut.closeArchiveEntry();
  }

  private static void addFileEntry(
      TarArchiveOutputStream tarOut, String entryName, File file, long modTime) throws IOException {
    final TarArchiveEntry tarEntry = new TarArchiveEntry(file, entryName);
    if (modTime >= 0) {
      tarEntry.setModTime(modTime);
    }
    tarOut.putArchiveEntry(tarEntry);
    try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
      final byte[] buf = new byte[BUF_SIZE];
      int r;
      while ((r = in.read(buf)) != -1) {
        tarOut.write(buf, 0, r);
      }
    }
    tarOut.closeArchiveEntry();
  }

  public static void untar(File tar, File targetDir) throws IOException {
    try (InputStream in = new FileInputStream(tar)) {
      untar(in, targetDir);
    }
  }

  /** @apiNote Caller should close `in` after calling this method. */
  public static void untar(InputStream in, File targetDir) throws IOException {
    final TarArchiveInputStream tarIn = new TarArchiveInputStream(in);
    byte[] b = new byte[BUF_SIZE];
    TarArchiveEntry tarEntry;
    while ((tarEntry = tarIn.getNextTarEntry()) != null) {
      final File file = new File(targetDir, tarEntry.getName());
      if (tarEntry.isDirectory()) {
        if (!file.mkdirs()) {
          throw new IOException("Unable to create folder " + file.getAbsolutePath());
        }
      } else {
        final File parent = file.getParentFile();
        if (!parent.exists()) {
          if (!parent.mkdirs()) {
            throw new IOException("Unable to create folder " + parent.getAbsolutePath());
          }
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
          int r;
          while ((r = tarIn.read(b)) != -1) {
            fos.write(b, 0, r);
          }
        }
      }
    }
  }

  public static boolean isTarFile(File file) throws IOException {
    if (file.isDirectory()) {
      return false;
    }
    // http://en.wikipedia.org/wiki/Tar_(computing)#File_header
    final byte[] header = new byte[512];
    try (FileInputStream fIn = new FileInputStream(file)) {
      if (fIn.read(header) != header.length) {
        return false;
      }
    }
    return TarArchiveInputStream.matches(header, header.length);
  }

  private TarUtils() {}
}
