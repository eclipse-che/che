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
package org.eclipse.che.ide.util;

import static org.junit.Assert.*;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests for the NameUtils class
 *
 * @see NameUtils
 * @author Paul-Julien Vauthier
 */
public class NameUtilsTest {

  private static String[] VALID_FILENAMES =
      new String[] {
        "README", "readme.md", "che.xml", "évacuation.html", "私の自転車.png", "I love maths.jpeg"
      };

  private static String[] INVALID_FILENAMES =
      new String[] {"\0", "a/b", "/", "\\", "*", "?", "|", "<", ">", "I<3Math.png", ""};

  @Test(dataProvider = "ValidFilenames")
  public void validFilenamesShouldGetValidated(String validFilename) {
    assertTrue(validFilename + " is supposed to be valid", NameUtils.checkFileName(validFilename));
  }

  @Test(dataProvider = "InvalidFilenames")
  public void invalidFilenamesShouldNotGetValidated(String invalidFileName) {
    assertFalse(
        invalidFileName + " is supposed to be invalid", NameUtils.checkFileName(invalidFileName));
  }

  @Test(dataProvider = "ValidFilenames")
  public void validFolderNamesShouldGetValidated(String validFolderName) {
    assertTrue(
        validFolderName + " is supposed to be valid", NameUtils.checkFileName(validFolderName));
  }

  @Test(dataProvider = "InvalidFilenames")
  public void invalidFolderNamesShouldNotGetValidated(String invalidFolderName) {
    assertFalse(
        invalidFolderName + " is supposed to be invalid",
        NameUtils.checkFileName(invalidFolderName));
  }

  @Test
  public void getFileExtension() {
    assertEquals("txt", NameUtils.getFileExtension("123.txt"));
    assertEquals("", NameUtils.getFileExtension("123"));
    assertEquals("zz", NameUtils.getFileExtension("123.txt.zz"));
  }

  @DataProvider(name = "ValidFilenames")
  public Object[][] getValidFilenames() {
    return toDataProviderData(VALID_FILENAMES);
  }

  @DataProvider(name = "InvalidFilenames")
  public Object[][] getInvalidFilenames() {
    return toDataProviderData(INVALID_FILENAMES);
  }

  private Object[][] toDataProviderData(String[] strings) {
    Object[][] data = new Object[strings.length][];
    for (int i = 0; i < strings.length; i++) {
      data[i] = new Object[] {strings[i]};
    }
    return data;
  }
}
