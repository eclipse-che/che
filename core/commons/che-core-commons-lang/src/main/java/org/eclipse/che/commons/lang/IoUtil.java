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

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.ProviderNotFoundException;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import javax.ws.rs.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IoUtil {

  private static final Logger LOG = LoggerFactory.getLogger(IoUtil.class);

  private IoUtil() {}

  /** Represents filter what select any file */
  public static final FilenameFilter ANY_FILTER =
      new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return true;
        }
      };
  /** Represent filter, that excludes .git entries. */
  public static final FilenameFilter GIT_FILTER =
      new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return !(".git".equals(name));
        }
      };

  /**
   * Reads bytes from input stream and builds a string from them.
   *
   * @param inputStream source stream
   * @return string
   * @throws java.io.IOException if any i/o error occur
   */
  public static String readStream(InputStream inputStream) throws IOException {
    if (inputStream == null) {
      return null;
    }
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    byte[] buf = new byte[8192];
    int r;
    while ((r = inputStream.read(buf)) != -1) {
      bout.write(buf, 0, r);
    }
    return bout.toString("UTF-8");
  }

  /**
   * Reads bytes from input stream and builds a string from them. InputStream closed after
   * consumption.
   *
   * @param inputStream source stream
   * @return string
   * @throws java.io.IOException if any i/o error occur
   */
  public static String readAndCloseQuietly(InputStream inputStream) throws IOException {
    try {
      return readStream(inputStream);
    } catch (IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
      throw e;
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          LOG.error(e.getLocalizedMessage(), e);
        }
      }
    }
  }

  /**
   * Looking for resource by given path. If no file exist by this path, method will try to find it
   * in context.
   *
   * @param resource - path to resource
   * @return - InputStream of resource
   * @throws IOException when resource is not a file or resource not found
   */
  public static InputStream getResource(String resource) throws IOException {
    File resourceFile = new File(resource);
    if (resourceFile.exists() && !resourceFile.isFile()) {
      throw new IOException(String.format("%s is not a file. ", resourceFile.getAbsolutePath()));
    }
    InputStream is =
        resourceFile.exists()
            ? new FileInputStream(resourceFile)
            : Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
    if (is == null) {
      throw new FileNotFoundException(String.format("Resource %s is not found", resource));
    }
    return is;
  }

  /**
   * Lists all children resources.
   *
   * @param parent the root path represented in {@link URI} format
   * @param consumer consumer for children resources
   * @throws java.io.IOException if any i/o error occur
   * @throws ProviderNotFoundException if a provider supporting the URI scheme is not installed
   */
  public static void listResources(URI parent, Consumer<Path> consumer) throws IOException {
    FileSystem fileSystem = null;
    try {
      if (!"file".equals(parent.getScheme())) {
        try {
          fileSystem = FileSystems.newFileSystem(parent, Collections.emptyMap());
        } catch (FileSystemAlreadyExistsException ignore) {
        }
      }

      Path root = Paths.get(parent);
      Files.list(root).forEach(consumer);
    } finally {
      // close FS only if only it has been initialized here
      if (fileSystem != null) {
        fileSystem.close();
      }
    }
  }

  /** Remove directory and all its sub-resources with specified path */
  public static boolean removeDirectory(String pathToDir) {
    return deleteRecursive(new File(pathToDir));
  }

  /**
   * Remove specified file or directory.
   *
   * @param fileOrDirectory the file or directory to cancel
   * @return <code>true</code> if specified File was deleted and <code>false</code> otherwise
   */
  public static boolean deleteRecursive(File fileOrDirectory) {
    if (fileOrDirectory.isDirectory()) {
      File[] list = fileOrDirectory.listFiles();
      if (list == null) {
        return false;
      }
      for (File f : list) {
        if (!deleteRecursive(f)) {
          return false;
        }
      }
    }
    if (!fileOrDirectory.delete()) {
      if (fileOrDirectory.exists()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Remove specified file or directory.
   *
   * @param fileOrDirectory the file or directory to cancel
   * @param followLinks are symbolic links followed or not?
   * @return <code>true</code> if specified File was deleted and <code>false</code> otherwise
   */
  public static boolean deleteRecursive(File fileOrDirectory, boolean followLinks) {
    if (fileOrDirectory.isDirectory()) {
      // If fileOrDirectory represents a symbolic link to a folder,
      // do not read a target folder content. Just remove this symbolic link.
      if (!followLinks && java.nio.file.Files.isSymbolicLink(fileOrDirectory.toPath())) {
        return !fileOrDirectory.exists() || fileOrDirectory.delete();
      }
      File[] list = fileOrDirectory.listFiles();
      if (list == null) {
        return false;
      }
      for (File f : list) {
        if (!deleteRecursive(f, followLinks)) {
          return false;
        }
      }
    }
    if (!fileOrDirectory.delete()) {
      if (fileOrDirectory.exists()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Download file.
   *
   * @param parent parent directory, may be <code>null</code> then use 'java.io.tmpdir'
   * @param prefix prefix of temporary file name, may not be <code>null</code> and must be at least
   *     three characters long
   * @param suffix suffix of temporary file name, may be <code>null</code>
   * @param url URL for download
   * @return downloaded file
   * @throws java.io.IOException if any i/o error occurs
   */
  public static File downloadFile(File parent, String prefix, String suffix, URL url)
      throws IOException {
    File file = File.createTempFile(prefix, suffix, parent);
    URLConnection conn = null;
    final String protocol = url.getProtocol().toLowerCase(Locale.ENGLISH);
    try {
      conn = url.openConnection();
      if ("http".equals(protocol) || "https".equals(protocol)) {
        HttpURLConnection http = (HttpURLConnection) conn;
        http.setInstanceFollowRedirects(false);
        http.setRequestMethod(HttpMethod.GET);
      }
      try (InputStream input = conn.getInputStream();
          FileOutputStream fOutput = new FileOutputStream(file)) {
        byte[] b = new byte[8192];
        int r;
        while ((r = input.read(b)) != -1) {
          fOutput.write(b, 0, r);
        }
      }
    } finally {
      if (conn != null && ("http".equals(protocol) || "https".equals(protocol))) {
        ((HttpURLConnection) conn).disconnect();
      }
    }
    return file;
  }

  /**
   * Download file with redirection if got status 301, 302, 303. Will useful in case redirection
   * http -> https
   *
   * @param parent parent directory, may be <code>null</code> then use 'java.io.tmpdir'
   * @param prefix prefix of temporary file name, may not be <code>null</code> and must be at least
   *     three characters long
   * @param suffix suffix of temporary file name, may be <code>null</code>
   * @param url URL for download
   * @return downloaded file
   * @throws java.io.IOException if any i/o error occurs
   */
  public static File downloadFileWithRedirect(File parent, String prefix, String suffix, URL url)
      throws IOException {
    File file = File.createTempFile(prefix, suffix, parent);
    URLConnection conn = null;
    final String protocol = url.getProtocol().toLowerCase(Locale.ENGLISH);
    try {
      conn = url.openConnection();
      boolean redirect = false;
      if ("http".equals(protocol) || "https".equals(protocol)) {
        HttpURLConnection http = (HttpURLConnection) conn;
        http.setRequestMethod(HttpMethod.GET);
        int status = http.getResponseCode();
        if (status == HttpURLConnection.HTTP_MOVED_TEMP
            || status == HttpURLConnection.HTTP_MOVED_PERM
            || status == HttpURLConnection.HTTP_SEE_OTHER) {
          redirect = true;
        }
        if (redirect) {
          String newUrl = conn.getHeaderField("Location");
          // open the new connection again
          http.disconnect();
          conn = new URL(newUrl).openConnection();
          http = (HttpURLConnection) conn;
          http.setRequestMethod(HttpMethod.GET);
        }
      }
      try (InputStream input = conn.getInputStream();
          FileOutputStream fOutput = new FileOutputStream(file)) {
        byte[] b = new byte[8192];
        int r;
        while ((r = input.read(b)) != -1) {
          fOutput.write(b, 0, r);
        }
      }
    } finally {
      if (conn != null && ("http".equals(protocol) || "https".equals(protocol))) {
        ((HttpURLConnection) conn).disconnect();
      }
    }
    return file;
  }

  /**
   * Copy file or directory to the specified destination. Existed files in destination directory
   * will be overwritten.
   *
   * @param source copy source
   * @param target copy destination
   * @param filter copy filter
   * @throws java.io.IOException if any i/o error occurs
   */
  public static void copy(File source, File target, FilenameFilter filter) throws IOException {
    copy(source, target, filter, false, true);
  }

  /**
   * Copy file or directory to the specified destination. Existed files in destination directory
   * will be overwritten.
   *
   * <p>This method use java.nio for coping files.
   *
   * @param source copy source
   * @param target copy destination
   * @param filter copy filter
   * @throws java.io.IOException if any i/o error occurs
   */
  public static void nioCopy(File source, File target, FilenameFilter filter) throws IOException {
    copy(source, target, filter, true, true);
  }

  /**
   * Copy file or directory to the specified destination.
   *
   * @param source copy source
   * @param target copy destination
   * @param filter copy filter
   * @param replaceIfExists if <code>true</code> existed files in destination directory will be
   *     overwritten
   * @throws java.io.IOException if any i/o error occurs
   */
  public static void copy(File source, File target, FilenameFilter filter, boolean replaceIfExists)
      throws IOException {
    copy(source, target, filter, false, replaceIfExists);
  }

  /**
   * Copy file or directory to the specified destination.
   *
   * <p>This method use java.nio for coping files.
   *
   * @param source copy source
   * @param target copy destination
   * @param filter copy filter
   * @param replaceIfExists if <code>true</code> existed files in destination directory will be
   *     overwritten
   * @throws java.io.IOException if any i/o error occurs
   */
  public static void nioCopy(
      File source, File target, FilenameFilter filter, boolean replaceIfExists) throws IOException {
    copy(source, target, filter, true, replaceIfExists);
  }

  private static void copy(
      File source, File target, FilenameFilter filter, boolean nio, boolean replaceIfExists)
      throws IOException {
    if (source.isDirectory()) {
      if (!(target.exists() || target.mkdirs())) {
        throw new IOException(
            String.format("Unable create directory '%s'. ", target.getAbsolutePath()));
      }
      if (filter == null) {
        filter = ANY_FILTER;
      }
      String sourceRoot = source.getAbsolutePath();
      LinkedList<File> q = new LinkedList<>();
      q.add(source);
      while (!q.isEmpty()) {
        File current = q.pop();
        File[] list = current.listFiles();
        if (list != null) {
          for (File f : list) {
            if (!filter.accept(current, f.getName())) {
              continue;
            }
            File newFile = new File(target, f.getAbsolutePath().substring(sourceRoot.length() + 1));
            if (f.isDirectory()) {
              if (!(newFile.exists() || newFile.mkdirs())) {
                throw new IOException(
                    String.format("Unable create directory '%s'. ", newFile.getAbsolutePath()));
              }
              if (!f.equals(target)) {
                q.push(f);
              }
            } else {
              if (nio) {
                nioCopyFile(f, newFile, replaceIfExists);
              } else {
                copyFile(f, newFile, replaceIfExists);
              }
            }
          }
        }
      }
    } else {
      File parent = target.getParentFile();
      if (!(parent.exists() || parent.mkdirs())) {
        throw new IOException(
            String.format("Unable create directory '%s'. ", parent.getAbsolutePath()));
      }
      if (nio) {
        nioCopyFile(source, target, replaceIfExists);
      } else {
        copyFile(source, target, replaceIfExists);
      }
    }
  }

  private static void copyFile(File source, File target, boolean replaceIfExists)
      throws IOException {
    if (!target.createNewFile()) {
      if (target.exists() && !replaceIfExists) {
        throw new IOException(
            String.format("File '%s' already exists. ", target.getAbsolutePath()));
      }
    }

    byte[] b = new byte[8192];
    try (FileInputStream in = new FileInputStream(source);
        FileOutputStream out = new FileOutputStream(target)) {
      int r;
      while ((r = in.read(b)) != -1) {
        out.write(b, 0, r);
      }
    }
  }

  private static void nioCopyFile(File source, File target, boolean replaceIfExists)
      throws IOException {
    if (!target.createNewFile()) // atomic
    {
      if (target.exists() && !replaceIfExists) {
        throw new IOException(
            String.format("File '%s' already exists. ", target.getAbsolutePath()));
      }
    }

    try (FileInputStream sourceStream = new FileInputStream(source);
        FileOutputStream targetStream = new FileOutputStream(target);
        FileChannel sourceChannel = sourceStream.getChannel();
        FileChannel targetChannel = targetStream.getChannel()) {

      final long size = sourceChannel.size();
      long transferred = 0L;
      while (transferred < size) {
        transferred += targetChannel.transferFrom(sourceChannel, transferred, (size - transferred));
      }
    }
  }

  public static List<File> list(File dir, FilenameFilter filter) {
    if (!dir.isDirectory()) {
      throw new IllegalArgumentException("Not a directory. ");
    }
    if (filter == null) {
      filter = ANY_FILTER;
    }
    List<File> files = new ArrayList<>();
    LinkedList<File> q = new LinkedList<>();
    q.add(dir);
    while (!q.isEmpty()) {
      File current = q.pop();
      File[] list = current.listFiles();
      if (list != null) {
        for (File f : list) {
          if (!filter.accept(current, f.getName())) {
            continue;
          }
          if (f.isDirectory()) {
            q.push(f);
          } else {
            files.add(f);
          }
        }
      }
    }
    return files;
  }

  public static String countFileHash(File file, MessageDigest digest) throws IOException {
    byte[] b = new byte[8192];
    try (DigestInputStream dis = new DigestInputStream(new FileInputStream(file), digest)) {
      while (dis.read(b) != -1) ;
      return toHex(digest.digest());
    }
  }

  private static final char[] HEX = "0123456789abcdef".toCharArray();

  public static String toHex(byte[] hash) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < hash.length; i++) {
      b.append(HEX[(hash[i] >> 4) & 0x0f]);
      b.append(HEX[hash[i] & 0x0f]);
    }
    return b.toString();
  }

  /**
   * Detects and returns {@code Path} to file by name pattern.
   *
   * @param pattern file name pattern
   * @param folder path to folder that contains project sources
   * @return pom.xml path
   * @throws java.io.IOException if an I/O error is thrown while finding pom.xml
   * @throws IllegalArgumentException if pom.xml not found
   */
  public static File findFile(String pattern, File folder) throws IOException {
    Finder finder = new Finder(pattern);
    Files.walkFileTree(
        folder.toPath(), EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, finder);
    if (finder.getFirstMatchedFile() == null) {
      throw new IllegalArgumentException("File not found.");
    }
    return finder.getFirstMatchedFile().toFile();
  }

  /** A {@code FileVisitor} that finds first file that match the specified pattern. */
  private static class Finder extends SimpleFileVisitor<Path> {
    private final PathMatcher matcher;
    private Path firstMatchedFile;

    Finder(String pattern) {
      matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
    }

    /** {@inheritDoc} */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      Path fileName = file.getFileName();
      if (fileName != null && matcher.matches(fileName)) {
        firstMatchedFile = file;
        return TERMINATE;
      }
      return CONTINUE;
    }

    /** Returns the first matched {@link java.nio.file.Path}. */
    Path getFirstMatchedFile() {
      return firstMatchedFile;
    }
  }
}
