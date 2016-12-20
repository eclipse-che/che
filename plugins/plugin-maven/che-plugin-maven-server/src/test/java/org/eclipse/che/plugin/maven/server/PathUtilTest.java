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
package org.eclipse.che.plugin.maven.server;

import org.eclipse.che.plugin.maven.server.core.project.PathUtil;
import org.testng.annotations.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Tests for {@link PathUtil}
 */
public class PathUtilTest {

    @Test
    public void testCanonicalPath() throws Exception {
        String path = PathUtil.toCanonicalPath("/foo/../bar", false);
        assertThat(path).isNotNull().isNotEmpty().isEqualTo("/bar");
    }

    @Test
    public void testRemoveLastSlash() throws Exception {
        String path = PathUtil.toCanonicalPath("/foo/bar/", true);
        assertThat(path).isNotNull().isNotEmpty().isEqualTo("/foo/bar");
    }

    @Test
    public void testEliminationDot() throws Exception {
        String path = PathUtil.toCanonicalPath("./bar", false);
        assertThat(path).isNotNull().isNotEmpty().isEqualTo("bar");

    }

    @Test
    public void testCanonicalPathWithFile() throws Exception {
        String path = PathUtil.toCanonicalPath("/foo/../bar/pom.xml", false);
        assertThat(path).isNotNull().isNotEmpty().isEqualTo("/bar/pom.xml");
    }
}
