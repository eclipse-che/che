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
package org.eclipse.che.api.vfs.server;

import junit.framework.TestCase;

import org.eclipse.che.commons.lang.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author andrew00x
 */
public class PathTest extends TestCase {
    private Map<String, Pair<String, String[]>> legal;
    private String[]                            illegal;

    @SuppressWarnings("unchecked")
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        legal = new HashMap<>();
        legal.put("/a/b/c/d", new Pair("/a/b/c/d", new String[]{"a", "b", "c", "d"}));
        legal.put("/a/b/c/../d", new Pair("/a/b/d", new String[]{"a", "b", "d"}));
        legal.put("/a/b/c/./d", new Pair("/a/b/c/d", new String[]{"a", "b", "c", "d"}));
        legal.put("./a/b/c/./d", new Pair("/a/b/c/d", new String[]{"a", "b", "c", "d"}));
        illegal = new String[]{"..", "/a/../..", "/a/b/../../..", "/a/b/../../../c/././.."};
    }

    public void testPath() {
        for (Map.Entry<String, Pair<String, String[]>> e : legal.entrySet()) {
            Path parsed = Path.fromString(e.getKey());
            assertEquals(e.getValue().first, parsed.toString());
            assertTrue(
                    String.format("expected: %s but was: %s", Arrays.toString(e.getValue().second), Arrays.toString(parsed.elements())),
                    Arrays.equals(e.getValue().second, parsed.elements()));
        }
    }

    public void testSubPath() {
        final String raw = "/a/b/c/d";
        Path parsed = Path.fromString(raw);
        assertEquals("/c/d", parsed.subPath(2).toString());
    }

    public void testSubPath2() {
        final String raw = "/a/b/c/d/";
        Path parsed = Path.fromString(raw);
        assertEquals("/a/b/c", parsed.subPath(0, parsed.length() - 1).toString());
    }

    public void testNewPath() {
        final String raw = "/a/b";
        Path parsed = Path.fromString(raw);
        assertEquals("/a/b/c/d", parsed.newPath("/c/d").toString());
    }

    public void testNewPathRelative() {
        final String raw = "/a/b";
        Path parsed = Path.fromString(raw);
        assertEquals("/a/y", parsed.newPath("x/../../y").toString());
    }

    public void testChildPath() {
        Path parent = Path.fromString("/a/b/c");
        Path child1 = Path.fromString("/a/b/c/d");
        Path child2 = Path.fromString("/a/b/c/d/e");
        assertTrue(child1.isChild(parent));
        assertTrue(child2.isChild(parent));
        assertTrue(child2.isChild(child1));
        assertFalse(child1.isChild(child2));
        assertFalse(parent.isChild(child1));
    }

    public void testParentPath() {
        Path path = Path.fromString("/a/b/c/d");
        Path expectedParent = Path.fromString("/a/b/c");
        assertEquals(expectedParent, path.getParent());
    }

    public void testIllegalPath() {
        for (String s : illegal) {
            try {
                Path.fromString(s);
                fail(String.format("IllegalArgumentException expected for path '%s' ", s));
            } catch (IllegalArgumentException ok) {
                //System.err.println(ok.getMessage());
            }
        }
    }
}
