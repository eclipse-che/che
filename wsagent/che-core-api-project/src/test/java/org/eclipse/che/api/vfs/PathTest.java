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
package org.eclipse.che.api.vfs;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author andrew00x
 */
@RunWith(DataProviderRunner.class)
public class PathTest {
    @DataProvider
    public static Object[][] legalPaths() throws Exception {
        return new Object[][]{
                {"/", "/", new String[0]},
                {"", "/", new String[0]},
                {null, "/", new String[0]},
                {"/a/b/c/d", "/a/b/c/d", new String[]{"a", "b", "c", "d"}},
                {"/a/b/c/../d", "/a/b/d", new String[]{"a", "b", "d"}},
                {"/a/b/c/./d", "/a/b/c/d", new String[]{"a", "b", "c", "d"}},
                {"a/b/c/d", "a/b/c/d", new String[]{"a", "b", "c", "d"}},
                {"./a/b/c/d", "a/b/c/d", new String[]{"a", "b", "c", "d"}}
        };
    }

    @DataProvider
    public static Object[][] illegalPaths() throws Exception {
        return new Object[][]{
                {".."},
                {"/a/../.."},
                {"/a/b/../../.."},
                {"/a/b/../../../c/././.."},
                };
    }

    @DataProvider
    public static Object[][] newPaths() throws Exception {
        return new Object[][]{
                {"/a/b", "/c/d", "/a/b/c/d"},
                {"/a/b", "x/../../y", "/a/y"},
                {"/a/b", "../..", "/"},
                {"a/b", "../..", ""}
        };
    }

    @UseDataProvider("legalPaths")
    @Test
    public void buildsPathFromString(String rawPath, String parsedPath, String[] pathElements) {
        Path path = Path.of(rawPath);
        assertEquals(parsedPath, path.toString());
        assertArrayEquals(pathElements, path.elements());
    }

    @UseDataProvider("illegalPaths")
    @Test(expected = IllegalArgumentException.class)
    public void failsBuildPathWhenStringIsInvalid(String rawPath) {
        Path.of(rawPath);
    }

    @Test
    public void providesCorrectParentPath() {
        Path path = Path.of("/a/b/c/d");
        Path expectedParent = Path.of("/a/b/c");
        assertEquals(expectedParent, path.getParent());
    }

    @Test
    public void buildsSubPathWithBeginIndex() {
        final String raw = "/a/b/c/d";
        Path parsed = Path.of(raw);
        assertEquals("c/d", parsed.subPath(2).toString());
    }

    @Test
    public void buildsSubPathWithBeginAndEndIndex() {
        final String raw = "/a/b/c/d/";
        Path parsed = Path.of(raw);
        assertEquals("/a/b/c", parsed.subPath(0, parsed.length() - 1).toString());
    }

    @UseDataProvider("newPaths")
    @Test
    public void buildsNewPathBasedOnExisted(String basePath, String subPath, String newPath) {
        Path parsed = Path.of(basePath);
        assertEquals(newPath, parsed.newPath(subPath).toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void failsBuildNewPathIfSubPathIsOutsideOfRoot() {
        Path basePath = Path.of("/a/b");
        basePath.newPath("../../..");
    }

    @Test
    public void detectsCorrectlyChildPath() {
        Path parent = Path.of("/a/b/c");
        Path child1 = Path.of("/a/b/c/d");
        Path child2 = Path.of("/a/b/c/d/e");
        assertTrue(child1.isChild(parent));
        assertTrue(child2.isChild(parent));
        assertTrue(child2.isChild(child1));
        assertFalse(child1.isChild(child2));
        assertFalse(parent.isChild(child1));
    }
}
