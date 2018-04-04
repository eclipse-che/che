/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.fs.server.impl;

import static org.apache.commons.io.FileUtils.readFileToByteArray;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.io.Files;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Random;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Tests for {@link FsOperations} */
public class FsOperationsTest {

  private static final String FILE_NAME = "filename";
  private static final String DIR_NAME = "dirname";
  private static final String PARENT_NAME = "parent";
  private static final String TEXT_MESSAGE = "text message";
  private static final byte[] BINARY_MESSAGE = new byte[20];

  static {
    new Random().nextBytes(BINARY_MESSAGE);
  }

  private final FsOperations fsOperations = new FsOperations();

  private File rootDir;
  private File srcDir;
  private File dstDir;

  private File file;
  private File srcFile;
  private File dstFile;

  @BeforeMethod
  public void setUp() {
    rootDir = Files.createTempDir();
    srcDir = Files.createTempDir();
    dstDir = Files.createTempDir();
  }

  @AfterMethod
  public void tearDown() {
    rootDir.deleteOnExit();
    srcDir.deleteOnExit();
    dstDir.deleteOnExit();

    if (file != null) {
      file.deleteOnExit();
    }

    if (srcFile != null) {
      srcFile.deleteOnExit();
    }

    if (dstFile != null) {
      dstFile.deleteOnExit();
    }
  }

  @Test
  public void shouldCreateFile() throws Exception {
    Path fsPath = rootDir.toPath().resolve(FILE_NAME);
    file = fsPath.toFile();

    assertFalse(file.exists());
    assertEquals(file.getName(), FILE_NAME);

    fsOperations.createFile(fsPath);

    assertTrue(file.exists());
    assertTrue(file.isFile());
    assertEquals(file.getName(), FILE_NAME);
  }

  @Test
  public void shouldCreateFileWithParents() throws Exception {
    Path fsPath = rootDir.toPath().resolve(PARENT_NAME).resolve(FILE_NAME);
    file = fsPath.toFile();

    assertFalse(file.exists());
    assertEquals(file.getName(), FILE_NAME);

    assertFalse(file.getParentFile().exists());
    assertEquals(file.getParentFile().getName(), PARENT_NAME);

    fsOperations.createFileWithParents(fsPath);

    assertTrue(file.exists());
    assertTrue(file.isFile());
    assertEquals(file.getName(), FILE_NAME);

    assertTrue(file.getParentFile().exists());
    assertTrue(file.getParentFile().isDirectory());
    assertEquals(file.getParentFile().getName(), PARENT_NAME);
  }

  @Test
  public void shouldCreateDir() throws Exception {
    Path fsPath = rootDir.toPath().resolve(DIR_NAME);
    file = fsPath.toFile();

    assertFalse(file.exists());
    assertEquals(file.getName(), DIR_NAME);

    fsOperations.createDir(fsPath);

    assertTrue(file.exists());
    assertTrue(file.isDirectory());
    assertEquals(file.getName(), DIR_NAME);
  }

  @Test
  public void shouldCreateDirWithParents() throws Exception {
    Path fsPath = rootDir.toPath().resolve(PARENT_NAME).resolve(DIR_NAME);
    file = fsPath.toFile();

    assertFalse(file.exists());
    assertEquals(file.getName(), DIR_NAME);

    assertFalse(file.getParentFile().exists());
    assertEquals(file.getParentFile().getName(), PARENT_NAME);

    fsOperations.createDirWithParents(fsPath);

    assertTrue(file.exists());
    assertTrue(file.isDirectory());
    assertEquals(file.getName(), DIR_NAME);

    assertTrue(file.getParentFile().exists());
    assertTrue(file.getParentFile().isDirectory());
    assertEquals(file.getParentFile().getName(), PARENT_NAME);
  }

  @Test
  public void shouldCopyFile() throws Exception {
    assertTrue(srcDir.exists());
    assertTrue(srcDir.isDirectory());
    assertTrue(dstDir.exists());
    assertTrue(dstDir.isDirectory());

    srcFile = new File(srcDir, FILE_NAME);
    assertTrue(srcFile.createNewFile());
    assertTrue(srcFile.exists());
    assertTrue(srcFile.isFile());

    dstFile = dstDir.toPath().resolve(FILE_NAME).toFile();
    assertFalse(dstFile.exists());

    fsOperations.copy(srcFile.toPath(), dstFile.toPath());

    assertTrue(srcFile.exists());
    assertTrue(srcFile.isFile());

    assertTrue(dstFile.exists());
    assertTrue(dstFile.isFile());
  }

  @Test
  public void shouldCopyFileWithParents() throws Exception {
    assertTrue(srcDir.exists());
    assertTrue(srcDir.isDirectory());
    assertTrue(dstDir.exists());
    assertTrue(dstDir.isDirectory());

    srcFile = new File(srcDir, FILE_NAME);
    assertTrue(srcFile.createNewFile());
    assertTrue(srcFile.exists());

    dstFile = dstDir.toPath().resolve(PARENT_NAME).resolve(FILE_NAME).toFile();
    assertFalse(dstFile.exists());
    assertFalse(dstFile.getParentFile().exists());

    fsOperations.copyWithParents(srcFile.toPath(), dstFile.toPath());

    assertTrue(srcFile.exists());
    assertTrue(srcFile.isFile());

    assertTrue(dstFile.getParentFile().exists());
    assertTrue(dstFile.getParentFile().isDirectory());

    assertTrue(dstFile.exists());
    assertTrue(dstFile.isFile());
  }

  @Test
  public void shouldMoveFile() throws Exception {
    assertTrue(srcDir.exists());
    assertTrue(srcDir.isDirectory());
    assertTrue(dstDir.exists());
    assertTrue(dstDir.isDirectory());

    srcFile = new File(srcDir, FILE_NAME);
    assertTrue(srcFile.createNewFile());
    assertTrue(srcFile.exists());
    assertTrue(srcFile.isFile());

    dstFile = dstDir.toPath().resolve(FILE_NAME).toFile();
    assertFalse(dstFile.exists());

    fsOperations.move(srcFile.toPath(), dstFile.toPath());

    assertFalse(srcFile.exists());
    assertFalse(srcFile.isFile());

    assertTrue(dstFile.exists());
    assertTrue(dstFile.isFile());
  }

  @Test
  public void shouldMoveFileWithParents() throws Exception {
    assertTrue(srcDir.exists());
    assertTrue(srcDir.isDirectory());
    assertTrue(dstDir.exists());
    assertTrue(dstDir.isDirectory());

    srcFile = new File(srcDir, FILE_NAME);
    assertTrue(srcFile.createNewFile());
    assertTrue(srcFile.exists());
    assertTrue(srcFile.isFile());

    dstFile = dstDir.toPath().resolve(PARENT_NAME).resolve(FILE_NAME).toFile();
    assertFalse(dstFile.exists());
    assertFalse(dstFile.getParentFile().exists());

    fsOperations.moveWithParents(srcFile.toPath(), dstFile.toPath());

    assertFalse(srcFile.exists());
    assertFalse(srcFile.isFile());

    assertTrue(dstFile.getParentFile().exists());
    assertTrue(dstFile.getParentFile().isDirectory());

    assertTrue(dstFile.exists());
    assertTrue(dstFile.isFile());
  }

  @Test
  public void shouldCopyDir() throws Exception {
    assertTrue(srcDir.exists());
    assertTrue(srcDir.isDirectory());
    assertTrue(dstDir.exists());
    assertTrue(dstDir.isDirectory());

    srcFile = new File(srcDir, FILE_NAME);
    assertTrue(srcFile.mkdir());
    assertTrue(srcFile.exists());
    assertTrue(srcFile.isDirectory());

    dstFile = dstDir.toPath().resolve(FILE_NAME).toFile();
    assertFalse(dstFile.exists());

    fsOperations.copy(srcFile.toPath(), dstFile.toPath());

    assertTrue(srcFile.exists());
    assertTrue(srcFile.isDirectory());

    assertTrue(dstFile.exists());
    assertTrue(dstFile.isDirectory());
  }

  @Test
  public void shouldCopyDirWithParents() throws Exception {
    assertTrue(srcDir.exists());
    assertTrue(srcDir.isDirectory());
    assertTrue(dstDir.exists());
    assertTrue(dstDir.isDirectory());

    srcFile = new File(srcDir, FILE_NAME);
    assertTrue(srcFile.mkdir());
    assertTrue(srcFile.isDirectory());

    dstFile = dstDir.toPath().resolve(PARENT_NAME).resolve(FILE_NAME).toFile();
    assertFalse(dstFile.exists());
    assertFalse(dstFile.getParentFile().exists());

    fsOperations.copyWithParents(srcFile.toPath(), dstFile.toPath());

    assertTrue(srcFile.exists());
    assertTrue(srcFile.isDirectory());

    assertTrue(dstFile.getParentFile().exists());
    assertTrue(dstFile.getParentFile().isDirectory());

    assertTrue(dstFile.exists());
    assertTrue(dstFile.isDirectory());
  }

  @Test
  public void shouldMoveDir() throws Exception {
    assertTrue(srcDir.exists());
    assertTrue(srcDir.isDirectory());
    assertTrue(dstDir.exists());
    assertTrue(dstDir.isDirectory());

    srcFile = new File(srcDir, FILE_NAME);
    assertTrue(srcFile.mkdir());
    assertTrue(srcFile.exists());
    assertTrue(srcFile.isDirectory());

    dstFile = dstDir.toPath().resolve(FILE_NAME).toFile();
    assertFalse(dstFile.exists());

    fsOperations.move(srcFile.toPath(), dstFile.toPath());

    assertFalse(srcFile.exists());

    assertTrue(dstFile.exists());
    assertTrue(dstFile.isDirectory());
  }

  @Test
  public void shouldDeleteFile() throws Exception {
    file = new File(rootDir, FILE_NAME);
    assertTrue(file.createNewFile());
    assertTrue(file.exists());
    assertTrue(file.isFile());

    fsOperations.delete(file.toPath());

    assertFalse(file.exists());
  }

  @Test
  public void shouldDeleteDir() throws Exception {
    file = new File(rootDir, FILE_NAME);
    assertTrue(file.mkdir());
    assertTrue(file.exists());
    assertTrue(file.isDirectory());

    fsOperations.delete(file.toPath());

    assertFalse(file.exists());
  }

  @Test
  public void shouldDeleteExistingFileIfExists() throws Exception {
    file = new File(rootDir, FILE_NAME);
    assertTrue(file.createNewFile());
    assertTrue(file.exists());
    assertTrue(file.isFile());

    fsOperations.deleteIfExists(file.toPath());

    assertFalse(file.exists());
  }

  @Test
  public void shouldDeleteExistingDirIfExists() throws Exception {
    file = new File(rootDir, FILE_NAME);
    assertTrue(file.mkdir());
    assertTrue(file.exists());
    assertTrue(file.isDirectory());

    fsOperations.deleteIfExists(file.toPath());

    assertFalse(file.exists());
  }

  @Test
  public void shouldDeleteNonExistingFileIfExists() throws Exception {
    file = new File(rootDir, FILE_NAME);
    assertFalse(file.exists());

    fsOperations.deleteIfExists(file.toPath());

    assertFalse(file.exists());
  }

  @Test
  public void shouldWriteToFile() throws Exception {
    String expected = TEXT_MESSAGE;

    file = new File(rootDir, FILE_NAME);
    assertTrue(file.createNewFile());
    assertTrue(file.exists());
    assertTrue(file.isFile());

    assertNotEquals(FileUtils.readFileToString(file), expected);

    try (PrintWriter p = new PrintWriter(fsOperations.write(file.toPath()))) {
      IOUtils.write(expected, p);
    }

    String actual = FileUtils.readFileToString(file);
    assertEquals(actual, expected);
  }

  @Test
  public void shouldReadFile() throws Exception {
    String expected = TEXT_MESSAGE;

    file = new File(rootDir, FILE_NAME);
    assertTrue(file.createNewFile());
    assertTrue(file.exists());
    assertTrue(file.isFile());

    FileUtils.write(file, expected);

    try (InputStream read = fsOperations.read(file.toPath())) {
      String actual = IOUtils.toString(read);

      assertEquals(actual, expected);
    }
  }

  @Test
  public void shouldUpdateTextFile() throws Exception {
    String expected = TEXT_MESSAGE;

    file = new File(rootDir, FILE_NAME);
    assertTrue(file.createNewFile());
    assertTrue(file.exists());
    assertTrue(file.isFile());

    assertNotEquals(FileUtils.readFileToString(file), expected);

    fsOperations.update(file.toPath(), IOUtils.toInputStream(expected));

    String actual = FileUtils.readFileToString(file);
    assertEquals(actual, expected);
  }

  @Test
  public void shouldUpdateBinaryFile() throws Exception {
    byte[] expected = BINARY_MESSAGE;

    file = new File(rootDir, FILE_NAME);
    assertTrue(file.createNewFile());
    assertTrue(file.exists());
    assertTrue(file.isFile());

    assertNotEquals(readFileToByteArray(file), expected);

    fsOperations.update(file.toPath(), new ByteArrayInputStream(expected));

    byte[] actual = readFileToByteArray(file);
    assertEquals(actual, expected);
  }

  @Test
  public void shouldReturnTrueForExistingFile() throws Exception {
    file = new File(rootDir, FILE_NAME);
    assertTrue(file.createNewFile());
    assertTrue(file.exists());
    assertTrue(file.isFile());

    boolean actual = fsOperations.exists(file.toPath());

    assertTrue(actual);
  }

  @Test
  public void shouldReturnTrueForExistingDir() throws Exception {
    file = new File(rootDir, DIR_NAME);
    assertTrue(file.mkdir());
    assertTrue(file.exists());
    assertTrue(file.isDirectory());

    boolean actual = fsOperations.exists(file.toPath());

    assertTrue(actual);
  }

  @Test
  public void shouldReturnFalseForNonExistingItem() throws Exception {
    file = new File(rootDir, FILE_NAME);
    assertFalse(file.exists());

    boolean actual = fsOperations.exists(file.toPath());

    assertFalse(actual);
  }

  @Test
  public void isFileShouldReturnTrueForFile() throws Exception {
    file = new File(rootDir, FILE_NAME);
    assertTrue(file.createNewFile());
    assertTrue(file.exists());
    assertTrue(file.isFile());

    boolean actual = fsOperations.isFile(file.toPath());

    assertTrue(actual);
  }

  @Test
  public void isFileShouldReturnFalseForDir() throws Exception {
    file = new File(rootDir, FILE_NAME);
    assertTrue(file.mkdir());
    assertTrue(file.exists());
    assertTrue(file.isDirectory());

    boolean actual = fsOperations.isFile(file.toPath());

    assertFalse(actual);
  }

  @Test
  public void isDirShouldReturnFalseForFile() throws Exception {
    file = new File(rootDir, FILE_NAME);
    assertTrue(file.createNewFile());
    assertTrue(file.exists());
    assertTrue(file.isFile());

    boolean actual = fsOperations.isDir(file.toPath());

    assertFalse(actual);
  }

  @Test
  public void isDirShouldReturnTrueForDir() throws Exception {
    file = new File(rootDir, DIR_NAME);
    assertTrue(file.mkdir());
    assertTrue(file.exists());
    assertTrue(file.isDirectory());

    boolean actual = fsOperations.isDir(file.toPath());

    assertTrue(actual);
  }

  @Test
  public void toIoFileShouldReturnFileForDir() throws Exception {
    file = new File(rootDir, DIR_NAME);
    assertTrue(file.mkdir());
    assertTrue(file.exists());
    assertTrue(file.isDirectory());

    File actual = fsOperations.toIoFile(file.toPath());

    assertEquals(file, actual);
  }

  @Test
  public void toIoFileShouldReturnFileForFile() throws Exception {
    file = new File(rootDir, FILE_NAME);
    assertTrue(file.createNewFile());
    assertTrue(file.exists());
    assertTrue(file.isFile());

    File actual = fsOperations.toIoFile(file.toPath());

    assertEquals(file, actual);
  }

  @Test
  public void getFileNamesShouldReturnFileNames() throws Exception {
    srcFile = new File(rootDir, FILE_NAME + 0);
    assertTrue(srcFile.createNewFile());
    assertTrue(srcFile.exists());
    assertTrue(srcFile.isFile());

    dstFile = new File(rootDir, FILE_NAME + 1);
    assertTrue(dstFile.createNewFile());
    assertTrue(dstFile.exists());
    assertTrue(dstFile.isFile());

    srcDir = new File(rootDir, DIR_NAME + 0);
    assertTrue(srcDir.mkdir());
    assertTrue(srcDir.exists());
    assertTrue(srcDir.isDirectory());

    dstDir = new File(rootDir, DIR_NAME + 1);
    assertTrue(dstDir.mkdir());
    assertTrue(dstDir.exists());
    assertTrue(dstDir.isDirectory());

    Set<String> fileNames = fsOperations.getFileNames(rootDir.toPath());

    assertTrue(fileNames.contains(srcFile.getName()));
    assertTrue(fileNames.contains(dstFile.getName()));
    assertEquals(fileNames.size(), 2);
  }

  @Test
  public void getDirNamesShouldReturnDirNames() throws Exception {
    srcFile = new File(rootDir, FILE_NAME + 0);
    assertTrue(srcFile.createNewFile());
    assertTrue(srcFile.exists());
    assertTrue(srcFile.isFile());

    dstFile = new File(rootDir, FILE_NAME + 1);
    assertTrue(dstFile.createNewFile());
    assertTrue(dstFile.exists());
    assertTrue(dstFile.isFile());

    srcDir = new File(rootDir, DIR_NAME + 0);
    assertTrue(srcDir.mkdir());
    assertTrue(srcDir.exists());
    assertTrue(srcDir.isDirectory());

    dstDir = new File(rootDir, DIR_NAME + 1);
    assertTrue(dstDir.mkdir());
    assertTrue(dstDir.exists());
    assertTrue(dstDir.isDirectory());

    Set<String> dirNames = fsOperations.getDirNames(rootDir.toPath());

    assertTrue(dirNames.contains(srcDir.getName()));
    assertTrue(dirNames.contains(dstDir.getName()));
    assertEquals(dirNames.size(), 2);
  }

  @Test
  public void getFilePathsShouldReturnFilePaths() throws Exception {
    srcFile = new File(rootDir, FILE_NAME + 0);
    assertTrue(srcFile.createNewFile());
    assertTrue(srcFile.exists());
    assertTrue(srcFile.isFile());

    dstFile = new File(rootDir, FILE_NAME + 1);
    assertTrue(dstFile.createNewFile());
    assertTrue(dstFile.exists());
    assertTrue(dstFile.isFile());

    srcDir = new File(rootDir, DIR_NAME + 0);
    assertTrue(srcDir.mkdir());
    assertTrue(srcDir.exists());
    assertTrue(srcDir.isDirectory());

    dstDir = new File(rootDir, DIR_NAME + 1);
    assertTrue(dstDir.mkdir());
    assertTrue(dstDir.exists());
    assertTrue(dstDir.isDirectory());

    Set<Path> filePaths = fsOperations.getFilePaths(rootDir.toPath());

    assertTrue(filePaths.contains(srcFile.toPath()));
    assertTrue(filePaths.contains(dstFile.toPath()));
    assertEquals(filePaths.size(), 2);
  }

  @Test
  public void getDirPathsShouldReturnDirPaths() throws Exception {
    srcFile = new File(rootDir, FILE_NAME + 0);
    assertTrue(srcFile.createNewFile());
    assertTrue(srcFile.exists());
    assertTrue(srcFile.isFile());

    dstFile = new File(rootDir, FILE_NAME + 1);
    assertTrue(dstFile.createNewFile());
    assertTrue(dstFile.exists());
    assertTrue(dstFile.isFile());

    srcDir = new File(rootDir, DIR_NAME + 0);
    assertTrue(srcDir.mkdir());
    assertTrue(srcDir.exists());
    assertTrue(srcDir.isDirectory());

    dstDir = new File(rootDir, DIR_NAME + 1);
    assertTrue(dstDir.mkdir());
    assertTrue(dstDir.exists());
    assertTrue(dstDir.isDirectory());

    Set<Path> dirPaths = fsOperations.getDirPaths(rootDir.toPath());

    assertTrue(dirPaths.contains(srcDir.toPath()));
    assertTrue(dirPaths.contains(dstDir.toPath()));
    assertEquals(dirPaths.size(), 2);
  }

  @Test
  public void shouldGetLength() throws Exception {
    long expected = TEXT_MESSAGE.length();

    file = new File(rootDir, FILE_NAME);
    assertTrue(file.createNewFile());
    assertTrue(file.exists());
    assertTrue(file.isFile());

    FileUtils.write(file, TEXT_MESSAGE);

    long actual = fsOperations.length(file.toPath());

    assertEquals(actual, expected);
  }

  @Test
  public void shouldGetLastModified() throws Exception {
    file = new File(rootDir, FILE_NAME);
    assertTrue(file.createNewFile());
    assertTrue(file.exists());
    assertTrue(file.isFile());

    long expected = file.lastModified();

    long actual = fsOperations.lastModified(file.toPath());

    assertEquals(actual, expected);
  }
}
