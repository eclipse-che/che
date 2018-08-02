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
package org.eclipse.che.ide.ext.git.client.compare;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status;
import org.mockito.Mock;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** @author Mykola Morhun */
public class AlteredFilesTest {

  private static final String[] FILES = {
    "Test1.java", "Test2.java", "Test3.java", "/dir/Test4.java"
  };
  private static final String[] STATUSES_STRINGS = {"A", "M", "D", "M"};
  private static final Status[] STATUSES =
      Arrays.stream(STATUSES_STRINGS).map(FileStatus::defineStatus).toArray(Status[]::new);
  private static final int FILES_LEN = FILES.length;

  private static String diff;

  @Mock private Project project;

  private AlteredFiles alteredFiles;

  @BeforeClass
  public void setupClass() {
    StringBuilder diffBuilder = new StringBuilder();

    for (int i = 0; i < FILES_LEN; i++) {
      diffBuilder.append(STATUSES_STRINGS[i]).append("\t").append(FILES[i]).append("\n");
    }
    diffBuilder.setLength(diffBuilder.length() - 1);

    diff = diffBuilder.toString();
  }

  @BeforeMethod
  public void setup() {
    alteredFiles = null;
  }

  @Test
  public void shouldBeAbleToCreateAlteredFilesList() {
    alteredFiles = new AlteredFiles(project, diff);

    assertEquals(alteredFiles.getFilesQuantity(), FILES_LEN);
    assertEquals(alteredFiles.getProject(), project);
  }

  @Test
  public void shouldBeAbleToCreateEmptyFilesList() {
    alteredFiles = new AlteredFiles(project, "");

    assertTrue(alteredFiles.isEmpty());
    assertEquals(alteredFiles.getFilesQuantity(), 0);
  }

  @Test
  public void shouldBeAbleToGetFileByIndex() {
    alteredFiles = new AlteredFiles(project, diff);

    for (int i = 0; i < FILES_LEN; i++) {
      assertEquals(alteredFiles.getFileByIndex(i), FILES[i]);
    }
  }

  @Test
  public void shouldBeAbleToGetStatusByIndex() {
    alteredFiles = new AlteredFiles(project, diff);

    for (int i = 0; i < FILES_LEN; i++) {
      assertEquals(alteredFiles.getStatusByIndex(i), STATUSES[i]);
    }
  }

  @Test
  public void shouldBeAbleToGetStatusByFile() {
    alteredFiles = new AlteredFiles(project, diff);

    for (int i = 0; i < FILES_LEN; i++) {
      assertEquals(alteredFiles.getStatusByFilePath(FILES[i]), STATUSES[i]);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class, dataProvider = "invalidDiffFileData")
  public void shouldThrowIllegalArgumentExceptionIfDiffFileDescriptionIsInvalid(
      String invalidDiffFileData) {
    alteredFiles = new AlteredFiles(project, diff + '\n' + invalidDiffFileData);
  }

  @DataProvider(name = "invalidDiffFileData")
  private Object[][] getInvalidDiffFileData() {
    return new Object[][] {{"M "}, {"M_Test.java"}, {"Test.java"}, {" "}};
  }
}
