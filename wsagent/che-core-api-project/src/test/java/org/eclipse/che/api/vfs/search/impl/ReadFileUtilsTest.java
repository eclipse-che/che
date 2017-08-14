/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.vfs.search.impl;

import org.eclipse.che.api.vfs.util.ReadFileUtils;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.testng.Assert.assertEquals;

public class ReadFileUtilsTest {

    String TEST_CONTENT2 = "Apollo set several major human spaceflight milestones,\n" +
                           "Maybe you should think twice,\n" +
                           "To be or not to be beeeee lambergeeene,\n" +
                           "In early 1961, direct ascent was generally the mission mode in favor at NASA,\n" +
                           "Time to think";

    @Test
    public void genLineByOffset() throws Exception {
        final Path tempFile = Files.createTempFile("my", "_test");
        Files.write(tempFile, TEST_CONTENT2.getBytes());
        ReadFileUtils.Line line = ReadFileUtils.getLine(tempFile.toFile(), 10);
        assertEquals(line.getLineNumber(), 0);
        assertEquals(line.getLineContent(), "Apollo set several major human spaceflight milestones,");

        line = ReadFileUtils.getLine(tempFile.toFile(), 85);
        assertEquals(line.getLineNumber(), 2);
        assertEquals(line.getLineContent(),  "To be or not to be beeeee lambergeeene,");

        line = ReadFileUtils.getLine(tempFile.toFile(), 130);
        assertEquals(line.getLineNumber(), 3);
        assertEquals(line.getLineContent(),  "In early 1961, direct ascent was generally the mission mode in favor at NASA,");
    }


    @Test(expectedExceptions = IOException.class,
          expectedExceptionsMessageRegExp = "File is not long enough")
    public void genLineByOffsetShouldFail() throws Exception {
        final Path tempFile = Files.createTempFile("my", "_test");
        Files.write(tempFile, TEST_CONTENT2.getBytes());
        ReadFileUtils.getLine(tempFile.toFile(), 10000);

    }






}
