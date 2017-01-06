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
package org.eclipse.che.api.core.util;

import org.eclipse.che.api.core.Page;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.eclipse.che.api.core.util.PagingUtil.createLinkHeader;
import static org.eclipse.che.api.core.util.PagingUtil.parseLinkHeader;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;

/**
 * Tests for {@link PagingUtil}.
 *
 * @author Yevhenii Voevodin
 */
public class PagingUtilTest {

    @Test
    public void testCreatingLinksHeader() throws Exception {
        final Page<String> page = new Page<>(asList("item3", "item4", "item5"), 3, 3, 7);
        final URI srcUri = URI.create("http://localhost:8080/path?qp=test");


        final String linkHeader = createLinkHeader(page, srcUri);


        final String[] expLinks = ("<http://localhost:8080/path?qp=test&skipCount=0&maxItems=3>; rel=\"first\", " +
                                   "<http://localhost:8080/path?qp=test&skipCount=6&maxItems=3>; rel=\"last\", " +
                                   "<http://localhost:8080/path?qp=test&skipCount=0&maxItems=3>; rel=\"prev\", " +
                                   "<http://localhost:8080/path?qp=test&skipCount=6&maxItems=3>; rel=\"next\"").split(", ");
        assertEqualsNoOrder(linkHeader.split(", "), expLinks);
    }

    @Test
    public void testParsingLinksHeader() throws Exception {
        final Map<String, String> relToLinks =
                parseLinkHeader("<http://localhost:8080/path?qp=test&skipCount=0&maxItems=3>; rel=\"first\", " +
                                "<http://localhost:8080/path?qp=test&skipCount=4&maxItems=3>; rel=\"last\", " +
                                "<http://localhost:8080/path?qp=test&skipCount=0&maxItems=3>; rel=\"prev\", " +
                                "<http://localhost:8080/path?qp=test&skipCount=5&maxItems=3>; rel=\"next\"");

        assertEquals(relToLinks.size(), 4);
        assertEquals(relToLinks.get("first"), "http://localhost:8080/path?qp=test&skipCount=0&maxItems=3");
        assertEquals(relToLinks.get("last"), "http://localhost:8080/path?qp=test&skipCount=4&maxItems=3");
        assertEquals(relToLinks.get("next"), "http://localhost:8080/path?qp=test&skipCount=5&maxItems=3");
        assertEquals(relToLinks.get("prev"), "http://localhost:8080/path?qp=test&skipCount=0&maxItems=3");
    }
}
