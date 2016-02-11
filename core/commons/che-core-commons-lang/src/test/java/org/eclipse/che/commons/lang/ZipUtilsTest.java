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
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
}