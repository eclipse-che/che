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
package org.eclipse.che.api.factory.server;

import com.google.common.base.Strings;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.rest.shared.dto.LinkParameter;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.dto.server.DtoFactory;

import javax.inject.Singleton;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

/** Helper class for creation links. */
@Singleton
public class LinksHelper {

    private static final String IMAGE_REL_ATT                    = "image";
    private static final String RETRIEVE_FACTORY_REL_ATT         = "self";
    private static final String SNIPPET_REL_ATT                  = "snippet/";
    private static final String FACTORY_ACCEPTANCE_REL_ATT       = "accept";
    private static final String NAMED_FACTORY_ACCEPTANCE_REL_ATT = "accept-named";
    private static final String ACCEPTED_REL_ATT                 = "accepted";

    private static List<String> snippetTypes = Collections.unmodifiableList(Arrays.asList("markdown", "url", "html", "iframe"));

    /**
     * Creates factory links and links on factory images.
     *
     * @param images
     *         a set of factory images
     * @param uriInfo
     *         URI information about relative URIs are relative to the base URI
     * @return list of factory links
     * @throws UnsupportedEncodingException
     *         occurs when impossible to encode URL
     */
    public List<Link> createLinks(Factory factory, Set<FactoryImage> images, UriInfo uriInfo, String userName) throws UnsupportedEncodingException {
        final List<Link> links = new LinkedList<>(createLinks(factory, uriInfo, userName));
        final UriBuilder baseUriBuilder = uriInfo != null ? UriBuilder.fromUri(uriInfo.getBaseUri()) : UriBuilder.fromUri("/");

        // add path to factory service
        final UriBuilder factoryUriBuilder = baseUriBuilder.clone().path(FactoryService.class);
        final String factoryId = factory.getId();

        // uri's to retrieve images
        links.addAll(images.stream()
                           .map(image -> createLink(HttpMethod.GET,
                                                    IMAGE_REL_ATT,
                                                    null,
                                                    image.getMediaType(),
                                                    factoryUriBuilder.clone()
                                                                     .path(FactoryService.class, "getImage")
                                                                     .queryParam("imgId", image.getName())
                                                                     .build(factoryId)
                                                                     .toString()))
                           .collect(toList()));
        return links;
    }

    /**
     * Creates factory links.
     *
     * @param uriInfo
     *         URI information about relative URIs are relative to the base URI
     * @return list of factory links
     * @throws UnsupportedEncodingException
     *         occurs when impossible to encode URL
     */
    public List<Link> createLinks(Factory factory, UriInfo uriInfo, String userName) throws UnsupportedEncodingException {
        final List<Link> links = new LinkedList<>();
        final UriBuilder baseUriBuilder = uriInfo != null ? UriBuilder.fromUri(uriInfo.getBaseUri()) : UriBuilder.fromUri("/");

        // add path to factory service
        final UriBuilder factoryUriBuilder = baseUriBuilder.clone().path(FactoryService.class);
        final String factoryId = factory.getId();
        if (factoryId != null) {
            // uri to retrieve factory
            links.add(createLink(HttpMethod.GET,
                                 RETRIEVE_FACTORY_REL_ATT,
                                 null,
                                 MediaType.APPLICATION_JSON,
                                 factoryUriBuilder.clone()
                                                  .path(FactoryService.class, "getFactory")
                                                  .build(factoryId)
                                                  .toString()));

            // uri's of snippets
            links.addAll(snippetTypes.stream()
                                     .map(snippet -> createLink(HttpMethod.GET,
                                                                SNIPPET_REL_ATT + snippet,
                                                                null,
                                                                MediaType.TEXT_PLAIN,
                                                                factoryUriBuilder.clone()
                                                                                 .path(FactoryService.class, "getFactorySnippet")
                                                                                 .queryParam("type", snippet)
                                                                                 .build(factoryId)
                                                                                 .toString()))
                                     .collect(toList()));

            // uri to accept factory
            final Link createWorkspace = createLink(HttpMethod.GET,
                                                    FACTORY_ACCEPTANCE_REL_ATT,
                                                    null,
                                                    MediaType.TEXT_HTML,
                                                    baseUriBuilder.clone()
                                                                  .replacePath("f")
                                                                  .queryParam("id", factoryId)
                                                                  .build()
                                                                  .toString());
            links.add(createWorkspace);


            // links of analytics
            links.add(createLink(HttpMethod.GET,
                                 ACCEPTED_REL_ATT,
                                 null,
                                 MediaType.TEXT_PLAIN,
                                 baseUriBuilder.clone()
                                               .path("analytics")
                                               .path("public-metric/factory_used")
                                               .queryParam("factory", URLEncoder.encode(createWorkspace.getHref(), "UTF-8"))
                                               .build()
                                               .toString()));
        }

        if (!Strings.isNullOrEmpty(factory.getName()) && !Strings.isNullOrEmpty(userName)) {
            // uri to accept factory by name and creator
            final Link createWorkspaceFromNamedFactory = createLink(HttpMethod.GET,
                                                                    NAMED_FACTORY_ACCEPTANCE_REL_ATT,
                                                                    null,
                                                                    MediaType.TEXT_HTML,
                                                                    baseUriBuilder.clone()
                                                                                  .replacePath("f")
                                                                                  .queryParam("name", factory.getName())
                                                                                  .queryParam("user", userName)
                                                                                  .build()
                                                                                  .toString());
            links.add(createWorkspaceFromNamedFactory);
        }

        return links;
    }

    /**
     * Find links with given relation.
     *
     * @param links
     *         links for searching
     * @param relation
     *         searching relation
     * @return set of links with relation equal to desired, empty set if there is no such links
     */
    public List<Link> getLinkByRelation(List<Link> links, String relation) {
        if (relation == null || links == null) {
            throw new IllegalArgumentException("Value of parameters can't be null.");
        }
        return links.stream()
                    .filter(link -> relation.equals(link.getRel()))
                    .collect(toCollection(LinkedList::new));
    }

    /** Creates factory Link */
    private Link createLink(String method, String rel, String consumes, String produces, String href) {
        return createLink(method, rel, consumes, produces, href, null);
    }

    /** Creates factory Link */
    private Link createLink(String method, String rel, String consumes, String produces, String href, List<LinkParameter> params) {
        return DtoFactory.getInstance()
                         .createDto(Link.class)
                         .withMethod(method)
                         .withRel(rel)
                         .withProduces(produces)
                         .withConsumes(consumes)
                         .withHref(href)
                         .withParameters(params);
    }
}
