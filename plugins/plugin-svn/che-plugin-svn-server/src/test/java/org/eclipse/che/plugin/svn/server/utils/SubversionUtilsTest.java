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
package org.eclipse.che.plugin.svn.server.utils;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.util.List;

import static org.eclipse.che.plugin.svn.server.utils.SubversionUtils.recognizeProjectUri;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link SubversionUtils}.
 */
public class SubversionUtilsTest {

    /**
     * Test for {@link SubversionUtils#getCheckoutRevision(List)}.
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testGetCheckoutRevision() throws Exception {
        assertEquals(-1, SubversionUtils.getCheckoutRevision(ImmutableList.of(
                "Some output",
                "Revision ABC"
        )));

        assertEquals(-1, SubversionUtils.getCheckoutRevision(ImmutableList.of(
                "Some output",
                "Checked out revision ABC"
        )));

        assertEquals(1588353, SubversionUtils.getCheckoutRevision(ImmutableList.of(
                "Some output",
                "Checked out revision 1588353."
        )));
    }

    /**
     * Test for {@link SubversionUtils#getUpdateRevision(List)}.
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testGetUpdateRevision() throws Exception {
        assertEquals(-1, SubversionUtils.getUpdateRevision(ImmutableList.of(
                "Some output",
                "Updated to revision ABC"
        )));

        assertEquals(-1, SubversionUtils.getUpdateRevision(ImmutableList.of(
                "Some output",
                "At revision ABC"
        )));

        assertEquals(1588353, SubversionUtils.getUpdateRevision(ImmutableList.of(
                "Some output",
                "Updated to revision 1588353."
        )));

        assertEquals(1588353, SubversionUtils.getUpdateRevision(ImmutableList.of(
                "Some output",
                "At revision 1588353."
        )));
    }

    @Test()
    public void testRecognizeProjectUri() throws Exception {
        assertEquals("http://a/b/c/project", recognizeProjectUri("http://a/b/c", "^/project/branches/3.1"));
        assertEquals("http://a/b/c/project", recognizeProjectUri("http://a/b/c", "^/project/trunk"));
        assertEquals("http://a/b/c/project", recognizeProjectUri("http://a/b/c", "^/project/d/e/f"));
        assertEquals("http://a/b/c", recognizeProjectUri("http://a/b/c", "^/trunk"));
        assertEquals("http://a/b/c", recognizeProjectUri("http://a/b/c", "^/"));
    }
}
