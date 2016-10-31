/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.commons.lang;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

        ZipUtils.getResources(new ZipFile(testJar.getFile()), Pattern.compile(".*[//]?codenvy/[^//]+[.]json"), consumer);

        verify(consumer, times(2)).accept(any(InputStream.class));
    }
}
