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
package org.eclipse.che.api.vfs.search;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.eclipse.che.api.vfs.VirtualFile;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(DataProviderRunner.class)
public class MediaTypeFilterTest {


    @DataProvider
    public static Object[][] testData() throws Exception {
        return new Object[][]{
       {virtualFileWithContent("to be or not to be".getBytes()), false},
       {virtualFileWithContent("<html><head></head></html>".getBytes()), false},
       {virtualFileWithContent("<a><b/></a>".getBytes()), false},
       {virtualFileWithContent("public class SomeClass {}".getBytes()), false},
       {virtualFileWithContent(new byte[10]), true}
        };
    }

    private static VirtualFile virtualFileWithContent(byte[] content) throws Exception {
        VirtualFile virtualFile = mock(VirtualFile.class);
        when(virtualFile.getContent()).thenReturn(new ByteArrayInputStream(content));
        return virtualFile;
    }

    private MediaTypeFilter mediaTypeFilter;

    @Before
    public void setUp() throws Exception {
        mediaTypeFilter = new MediaTypeFilter();
    }

    @UseDataProvider("testData")
    @Test
    public void testFilesShouldAccepted(VirtualFile virtualFile, boolean expectedResult) throws Exception {
        assertEquals(expectedResult, mediaTypeFilter.accept(virtualFile));
    }
}
