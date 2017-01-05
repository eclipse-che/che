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

import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.rest.shared.dto.LinkParameter;
import org.eclipse.che.dto.server.DtoFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * @author andrew00x
 */
public class LinksHelper {

    public static Link createLink(String method, String href, String consumes, String produces, String rel, LinkParameter... params) {
        List<LinkParameter> l = null;
        if (params != null && params.length > 0) {
            l = new LinkedList<>();
            java.util.Collections.addAll(l, params);
        }
        return createLink(method, href, consumes, produces, rel, l);
    }

    public static Link createLink(String method, String href, String consumes, String produces, String rel, List<LinkParameter> params) {
        return DtoFactory.getInstance().createDto(Link.class)
                         .withMethod(method)
                         .withHref(href)
                         .withConsumes(consumes)
                         .withProduces(produces)
                         .withRel(rel)
                         .withParameters(params);
    }

    public static Link createLink(String method, String href, String consumes, String produces, String rel) {
        return DtoFactory.getInstance().createDto(Link.class)
                         .withMethod(method)
                         .withHref(href)
                         .withConsumes(consumes)
                         .withProduces(produces)
                         .withRel(rel);
    }

    public static Link createLink(String method, String href, String produces, String rel) {
        return DtoFactory.getInstance().createDto(Link.class).withMethod(method).withHref(href).withProduces(produces).withRel(rel);
    }

    public static Link createLink(String method, String href, String rel) {
        return DtoFactory.getInstance().createDto(Link.class).withMethod(method).withHref(href).withRel(rel);
    }

    public static Link createLink(String method, String href, String rel, List<LinkParameter> params) {
        return DtoFactory.getInstance()
                         .createDto(Link.class)
                         .withMethod(method)
                         .withHref(href)
                         .withRel(rel)
                         .withParameters(params);
    }

    private LinksHelper() {
    }
}
