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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Random;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ZipUtilsTest {

  private File zipFile;

  @BeforeMethod
  public void setUp() throws IOException {
    zipFile = File.createTempFile("test", "zip");
    zipFile.deleteOnExit();

    byte[] testData = new byte[2048];
    Random random = new Random();
    random.nextBytes(testData);

    try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
      ZipEntry entry = new ZipEntry("test");
      entry.setSize(testData.length);
      zos.putNextEntry(entry);
      zos.write(testData);
      zos.closeEntry();
      zos.close();
    }
  }

  @Test
  public void shouldBeAbleToDetectZipFile() throws IOException {
    Assert.assertTrue(ZipUtils.isZipFile(zipFile));
  }

  @Test
  public void testGetResources() throws Exception {
    URL testJar = ZipUtilsTest.class.getResource("/che/che.jar");
    @SuppressWarnings("unchecked")
    Consumer<InputStream> consumer = mock(Consumer.class);

    ZipUtils.getResources(
        new ZipFile(testJar.getFile()), Pattern.compile(".*[//]?codenvy/[^//]+[.]json"), consumer);

    verify(consumer, times(2)).accept(any(InputStream.class));
  }
}
