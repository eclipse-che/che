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
package org.eclipse.che.api.core.rest.shared;

import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;

import java.util.LinkedList;
import java.util.List;

/**
 * Helper class for working with links.
 *
 * @author andrew00x
 */
public class Links {
    /**
     * Find first link in the specified list by its relation.
     *
     * @param rel
     *         link's relation
     * @param links
     *         list of links
     * @return found link or {@code null}
     */
    public static Link getLink(String rel, List<Link> links) {
        for (Link link : links) {
            if (rel.equals(link.getRel())) {
                return link;
            }
        }
        return null;
    }

    /**
     * Find all links in the specified list by its relation.
     *
     * @param rel
     *         link's relation
     * @param links
     *         list of links
     * @return found link or {@code null}
     */
    public static List<Link> getLinks(String rel, List<Link> links) {
        final List<Link> result = new LinkedList<>();
        for (Link link : links) {
            if (rel.equals(link.getRel())) {
                result.add(link);
            }
        }
        return result;
    }

    public static Link getLink(Hyperlinks links, String rel) {
        return getLink(rel, links.getLinks());
    }

    public static List<Link> getLinks(Hyperlinks links, String rel) {
        return getLinks(rel, links.getLinks());
    }

    private Links() {
    }
}
