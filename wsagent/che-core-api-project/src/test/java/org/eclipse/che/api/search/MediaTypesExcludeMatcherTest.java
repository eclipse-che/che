/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.search;

import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import org.junit.runner.RunWith;

/** @author Valeriy Svydenko */
@RunWith(DataProviderRunner.class)
public class MediaTypesExcludeMatcherTest {
  //
  //  @DataProvider
  //  public static Object[][] testData() throws Exception {
  //    return new Object[][] {
  //      {virtualFileWithContent("to be or not to be".getBytes()), false},
  //      {virtualFileWithContent("<html><head></head></html>".getBytes()), false},
  //      {virtualFileWithContent("<a><b/></a>".getBytes()), false},
  //      {virtualFileWithContent("public class SomeClass {}".getBytes()), false},
  //      {virtualFileWithContent(new byte[10]), true}
  //    };
  //  }
  //
  //  private static VirtualFile virtualFileWithContent(byte[] content) throws Exception {
  //    VirtualFile virtualFile = mock(VirtualFile.class);
  //    when(virtualFile.getContent()).thenReturn(new ByteArrayInputStream(content));
  //    return virtualFile;
  //  }
  //
  //  private MediaTypesExcludeMatcher mediaTypesExcludeMatcher;
  //
  //  @Before
  //  public void setUp() throws Exception {
  //    mediaTypesExcludeMatcher = new MediaTypesExcludeMatcher();
  //  }
  //
  //  @UseDataProvider("testData")
  //  @Test
  //  public void testFilesShouldAccepted(VirtualFile virtualFile, boolean expectedResult)
  //      throws Exception {
  //    assertEquals(expectedResult, mediaTypesExcludeMatcher.accept(virtualFile));
  //  }
}
