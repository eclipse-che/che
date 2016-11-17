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
package org.eclipse.che.ide.resource;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the {@link Path}.
 *
 * @author Vlad Zhukovskyi
 * @see Path
 */
public class PathTest {

    @Test
    public void testShouldReturnNewPathBasedOnValue() throws Exception {
        final String sPath = "/path/segment";
        final Path path = Path.valueOf(sPath);

        assertEquals(path.toString(), "/path/segment");
    }

    @Test
    public void testShouldReturnNewPathWithCallingConstructor() throws Exception {
        final String sPath = "/path/segment";
        final Path path = new Path(sPath);

        assertEquals(path.toString(), "/path/segment");
    }

    @Test
    public void testShouldReturnNewPathWithDeviceId() throws Exception {
        final String sPath = "/path/segment";
        final String sDevice = "/mnt";
        final Path path = new Path(sDevice, sPath);

        assertEquals(path.toString(), "/mnt/path/segment");
    }

    @Test
    public void testShouldReturnNewPathAfterAddingExtension() throws Exception {
        final Path path = Path.valueOf("/foo");
        final Path file = path.addFileExtension("ext");

        assertNotSame(path, file);
        assertTrue(path.toString().equals("/foo"));
        assertTrue(file.toString().equals("/foo.ext"));
    }

    @Test
    public void testShouldReturnNewPathAfterOperationWithTrailingSeparatorToPath() throws Exception {
        final Path path = Path.valueOf("/foo");

        assertFalse(path.hasTrailingSeparator());

        final Path pathWithSeparator = path.addTrailingSeparator();

        assertNotSame(path, pathWithSeparator);
        assertFalse(path.hasTrailingSeparator());
        assertTrue(pathWithSeparator.hasTrailingSeparator());
        assertTrue(path.toString().equals("/foo"));
        assertTrue(pathWithSeparator.hasTrailingSeparator());
        assertTrue(pathWithSeparator.toString().equals("/foo/"));

        final Path pathWithRemovedSeparator = pathWithSeparator.removeTrailingSeparator();

        assertTrue(path.equals(pathWithRemovedSeparator));
        assertFalse(pathWithRemovedSeparator.hasTrailingSeparator());
    }

    @Test
    public void testShouldReturnNewPathAfterAppendingAnotherPath() throws Exception {
        final Path path = Path.valueOf("/foo");
        final Path tmpPath = Path.valueOf("/tmp");

        final Path appended = path.append(tmpPath);

        assertNotSame(path, appended);
        assertTrue(path.toString().equals("/foo"));
        assertTrue(tmpPath.toString().equals("/tmp"));
        assertTrue(appended.toString().equals("/foo/tmp"));
    }

    @Test
    public void testShouldReturnNewPathAfterAppendingAnotherStringPath() throws Exception {
        final Path path = Path.valueOf("/foo");

        final Path appended = path.append("/tmp");

        assertNotSame(path, appended);
        assertTrue(path.toString().equals("/foo"));
        assertTrue(appended.toString().equals("/foo/tmp"));
    }

    @Test
    public void testShouldCheckPathEquality() throws Exception {
        assertTrue(Path.valueOf("/foo").equals(Path.valueOf("/foo")));
        assertTrue(Path.valueOf("/foo").equals(Path.valueOf("/foo/")));
        assertTrue(new Path("/mnt", "/foo").equals(new Path("/mnt", "/foo")));
        assertFalse(Path.valueOf("/foo").equals(Path.valueOf("/foo/a/b")));
        assertFalse(new Path("/mnt", "/foo").equals(new Path("/mount", "/foo")));
        assertFalse(Path.valueOf("/foo").equals(Path.valueOf("foo/")));
    }

    @Test
    public void testShouldCheckDeviceId() throws Exception {
        final Path path = new Path("/mnt", "/foo");

        assertTrue(path.getDevice().equals("/mnt"));
    }

    @Test
    public void testShouldCheckFileExtension() throws Exception {
        final Path path = Path.valueOf("/foo");

        assertNull(path.getFileExtension());

        final Path extPath = path.addFileExtension("ext");

        assertNotSame(path, extPath);
        assertNull(path.getFileExtension());
        assertNotNull(extPath.getFileExtension());
        assertTrue(extPath.getFileExtension().equals("ext"));
        assertTrue(extPath.toString().equals("/foo.ext"));

        final Path removedExtPath = extPath.removeFileExtension();

        assertNotSame(path, removedExtPath);
        assertNotSame(extPath, removedExtPath);
        assertTrue(path.equals(removedExtPath));
        assertTrue(removedExtPath.toString().equals("/foo"));
    }

    @Test
    public void testShouldReturnTrueWhenTwoEqualPathHaveSameHashCode() throws Exception {
        assertTrue(Path.valueOf("/foo").hashCode() == Path.valueOf("/foo").hashCode());
        assertTrue(Path.valueOf("/foo").hashCode() == Path.valueOf("/foo/").hashCode());
    }

    @Test
    public void testShouldCheckExistenceOfTrailingSeparator() throws Exception {
        assertTrue(Path.valueOf("/foo/").hasTrailingSeparator());
        assertFalse(Path.valueOf("/foo").hasTrailingSeparator());
        assertFalse(Path.valueOf("/").hasTrailingSeparator());
        assertFalse(Path.valueOf("").hasTrailingSeparator());
    }

    @Test
    public void testShouldCheckAbsolutePath() throws Exception {
        assertTrue(Path.valueOf("/foo").isAbsolute());
        assertFalse(Path.valueOf("foo").isAbsolute());
    }

    @Test
    public void testShouldCheckEmptiness() throws Exception {
        assertTrue(Path.valueOf("").isEmpty());
        assertFalse(Path.valueOf("/").isEmpty());
    }

    @Test
    public void testShouldCheckPathPrefix() throws Exception {
        assertTrue(Path.valueOf("").isPrefixOf(Path.valueOf("")));
        assertTrue(Path.valueOf("/").isPrefixOf(Path.valueOf("")));
        assertTrue(Path.valueOf("").isPrefixOf(Path.valueOf("/")));
        assertTrue(Path.valueOf("/").isPrefixOf(Path.valueOf("/")));
        assertTrue(Path.valueOf("/").isPrefixOf(Path.valueOf("/foo/a/b/c")));
        assertTrue(Path.valueOf("/foo").isPrefixOf(Path.valueOf("/foo/a/b/c")));
        assertTrue(Path.valueOf("/foo/").isPrefixOf(Path.valueOf("/foo/a/b/c")));
        assertTrue(Path.valueOf("foo").isPrefixOf(Path.valueOf("/foo/a/b/c")));
        assertTrue(Path.valueOf("/foo/a/b/c").isPrefixOf(Path.valueOf("/foo/a/b/c")));
        assertFalse(Path.valueOf("/foo/a/b/c/d").isPrefixOf(Path.valueOf("/foo/a/b/c")));
        assertFalse(Path.valueOf("/foo/a/x/c").isPrefixOf(Path.valueOf("/foo/a/b/c")));
        assertFalse(Path.valueOf("foo/a/x/c").isPrefixOf(Path.valueOf("/foo/a/b/c")));
        assertFalse(Path.valueOf("abc").isPrefixOf(Path.valueOf("def")));
    }

    @Test
    public void testShouldCheckIsPathRoot() throws Exception {
        assertTrue(Path.valueOf("/").isRoot());
        assertFalse(Path.valueOf("/foo").isRoot());
        assertFalse(Path.valueOf("").isRoot());
        assertFalse(Path.valueOf("foo").isRoot());
    }

    @Test
    public void testShouldCheckIfPathIsUnc() throws Exception {
        final Path path = Path.valueOf("//foo");

        assertNull(path.getDevice());
        assertTrue(path.isUNC());
        assertFalse(Path.valueOf("/foo").isUNC());
    }

    @Test
    public void testShouldCheckPathValidity() throws Exception {
        assertTrue(Path.isValidPath("/"));
        assertTrue(Path.isValidPath("/foo/bar"));

        assertFalse(Path.isValidSegment(""));
        assertFalse(Path.isValidSegment("foo/bar"));
    }

    @Test
    public void testShouldCheckLastSegmentOperations() throws Exception {
        final Path path = Path.valueOf("/foo/a/b/c/bar");

        assertTrue(path.lastSegment().equals("bar"));
        assertTrue(path.segmentCount() == 5);
        assertTrue(Arrays.equals(path.segments(), new String[]{"foo", "a", "b", "c", "bar"}));
        assertEquals(path.segment(0), "foo");
        assertEquals(path.segment(1), "a");
        assertEquals(path.segment(2), "b");
        assertEquals(path.segment(3), "c");
        assertEquals(path.segment(4), "bar");
        assertTrue(path.matchingFirstSegments(Path.valueOf("/foo/a/b/c/bar")) == 5);
        assertTrue(path.matchingFirstSegments(Path.valueOf("/foo/a/x/x/x")) == 2);
        assertTrue(path.matchingFirstSegments(Path.valueOf("/x/x/x/x/x")) == 0);
        assertTrue(path.removeFirstSegments(1).equals(Path.valueOf("a/b/c/bar")));
        assertTrue(path.removeLastSegments(1).equals(Path.valueOf("/foo/a/b/c")));
        assertTrue(path.uptoSegment(1).equals(Path.valueOf("/foo")));
        assertTrue(path.uptoSegment(2).equals(Path.valueOf("/foo/a")));
        assertTrue(path.uptoSegment(3).equals(Path.valueOf("/foo/a/b")));
        assertTrue(path.uptoSegment(4).equals(Path.valueOf("/foo/a/b/c")));
    }

    @Test
    public void testShouldSetCorrectDevice() throws Exception {
        final Path path = Path.valueOf("/foo");

        assertNull(path.getDevice());

        final Path pathWithDevice = path.setDevice("mnt:");

        assertNotSame(path, pathWithDevice);
        assertFalse(path.equals(pathWithDevice));
        assertTrue(pathWithDevice.toString().equals("mnt:/foo"));
    }

    @Test
    public void testShouldReturnCommonPath() throws Exception {
        final Path common = Path.valueOf("/foo");

        final Path path1 = common.append("a/b");
        final Path path2 = common.append("a/c");
        final Path path3 = common.append("a/d");
        final Path path4 = common.append("c/d/b");
        final Path path5 = common.append("a/d/c");
        final Path path6 = common.append("a/c/c");

        final Path result = Path.commonPath(path1, path2, path3, path4, path5, path6);

        assertEquals(result, common);
    }

    @Test
    public void testShouldReturnRootPathAsCommon() throws Exception {
        final Path path1 = Path.valueOf("/a");
        final Path path2 = Path.valueOf("/b");
        final Path path3 = Path.valueOf("/c");
        final Path path4 = Path.valueOf("/d");
        final Path path5 = Path.valueOf("/e");

        final Path result = Path.commonPath(path1, path2, path3, path4, path5);

        assertEquals(result, Path.ROOT);
    }

    @Test
    public void testShouldReturnRootPathAsCommon2() throws Exception {
        final Path path1 = Path.valueOf("/a");
        final Path path2 = Path.valueOf("b");
        final Path path3 = Path.valueOf("/c");
        final Path path4 = Path.valueOf("d");
        final Path path5 = Path.valueOf("/e");

        final Path result = Path.commonPath(path1, path2, path3, path4, path5);

        assertEquals(result, Path.ROOT);
    }

    @Test
    public void testShouldReturnEmptyPathForEmptyInputArray() throws Exception {
        final Path result = Path.commonPath();

        assertEquals(result, Path.EMPTY);
    }

    @Test(expected = NullPointerException.class)
    public void testShouldThrowNPEOnNullArgument() throws Exception {
        Path.commonPath(null);
    }

    @Test
    public void testShouldReturnSamePathAsOneGivenAsArgument() throws Exception {
        final Path path = Path.valueOf("/some/path");

        final Path result = Path.commonPath(path);

        assertEquals(result, path);
    }

    @Test
    public void testShouldReturnCorrectCommonPathIfPathHasSegmentsMoreThanPathCount() throws Exception {
        final Path common = Path.valueOf("/foo");

        final Path path1 = common.append("a/b/c/d/e/f/g/h/i/j/k/l");
        final Path path2 = common.append("b/c");

        final Path result = Path.commonPath(path1, path2);

        assertEquals(result, common);
    }
}
