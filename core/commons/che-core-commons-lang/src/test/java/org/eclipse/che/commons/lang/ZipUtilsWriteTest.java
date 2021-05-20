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

import static org.eclipse.che.commons.lang.ZipUtils.add;
import static org.eclipse.che.commons.lang.ZipUtils.listEntries;
import static org.eclipse.che.commons.lang.ZipUtils.stream;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Random;
import java.util.zip.ZipOutputStream;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ZipUtilsWriteTest {
  private Path zipFile;
  private Path tempDir;

  private static Random random = new Random();

  @BeforeMethod
  public void setUp() throws IOException {
    this.zipFile = Files.createTempFile("ZipUtilsTest", ".zip");
    this.tempDir = createZipDir();
  }

  private Path createZipDir() throws IOException {
    Path rootDir = Files.createTempDirectory("ZipUtilTest");
    createFile(rootDir, "foo.bar");
    createFile(rootDir, "inner/temp.bla");
    return rootDir;
  }

  private void createFile(Path rootDir, String relativePath) throws IOException {
    Path file = rootDir.resolve(relativePath);
    Files.createDirectories(file.getParent());
    byte[] buf = new byte[1024];
    random.nextBytes(buf);
    Files.write(file, buf);
  }

  @AfterMethod
  public void tearDown() throws IOException {
    Files.delete(zipFile);
    Files.walkFileTree(
        tempDir,
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
            if (e == null) {
              Files.delete(dir);
              return FileVisitResult.CONTINUE;
            } else {
              throw e;
            }
          }
        });
  }

  @Test
  public void testAddFileWithParent() throws IOException {
    try (ZipOutputStream out = stream(zipFile)) {
      add(out, tempDir, tempDir);
    }
    Collection<String> entries = listEntries(zipFile.toFile());
    assertTrue(entries.contains("foo.bar"));
    assertTrue(entries.contains(Paths.get("inner/temp.bla").toString()));
  }

  @Test
  public void testAddFile() throws IOException {
    try (ZipOutputStream out = stream(zipFile)) {
      add(out, tempDir);
    }
    Collection<String> entries = listEntries(zipFile.toFile());
    String tempFileName = tempDir.getFileName().toString();
    assertTrue(entries.contains(Paths.get(tempFileName + "/foo.bar").toString()));
    assertTrue(entries.contains(Paths.get(tempFileName + "/inner/temp.bla").toString()));
  }
}
