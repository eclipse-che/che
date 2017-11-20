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

import static org.apache.commons.io.IOUtils.toInputStream;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.search.server.excludes.MediaTypesExcludeMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Valeriy Svydenko */
@Listeners(MockitoTestNGListener.class)
public class MediaTypesExcludeMatcherTest {

  @Mock private FsManager fsManager;
  @Mock private PathTransformer pathTransformer;
  @InjectMocks private MediaTypesExcludeMatcher mediaTypesExcludeMatcher;

  @Mock private Path path;

  @BeforeMethod
  public void setUp() throws Exception {
    String WS_PATH = "/ws/path";

    when(pathTransformer.transform(path)).thenReturn(WS_PATH);
    when(pathTransformer.transform(WS_PATH)).thenReturn(path);
  }

  @Test(enabled = false)
  public void shouldMatchEmptyFile() throws Exception {
    when(fsManager.read(anyString())).thenReturn(toInputStream(""));

    assertTrue(mediaTypesExcludeMatcher.matches(path));
  }

  @Test(enabled = false)
  public void shouldNotMatchTextFile() throws Exception {
    when(fsManager.read(anyString())).thenReturn(toInputStream("to be or not to be"));

    assertFalse(mediaTypesExcludeMatcher.matches(path));
  }

  @Test(enabled = false)
  public void shouldNotMatchHtmlFile() throws Exception {
    when(fsManager.read(anyString())).thenReturn(toInputStream("<html><head></head></html>"));

    assertFalse(mediaTypesExcludeMatcher.matches(path));
  }

  @Test(enabled = false)
  public void shouldNotMatchJavaFile() throws Exception {
    when(fsManager.read(anyString())).thenReturn(toInputStream("public class SomeClass {}"));

    assertFalse(mediaTypesExcludeMatcher.matches(path));
  }

  @Test(enabled = false)
  public void shouldNotMatchMarkUpFile() throws Exception {
    when(fsManager.read(anyString())).thenReturn(toInputStream("<a><b/></a>"));

    assertFalse(mediaTypesExcludeMatcher.matches(path));
  }
}
