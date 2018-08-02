/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.server.impl;

import static javax.ws.rs.HttpMethod.DELETE;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.PUT;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.api.core.util.LinksHelper.createLink;
import static org.eclipse.che.api.project.shared.Constants.LINK_REL_CHILDREN;
import static org.eclipse.che.api.project.shared.Constants.LINK_REL_DELETE;
import static org.eclipse.che.api.project.shared.Constants.LINK_REL_GET_CONTENT;
import static org.eclipse.che.api.project.shared.Constants.LINK_REL_TREE;
import static org.eclipse.che.api.project.shared.Constants.LINK_REL_UPDATE_CONTENT;
import static org.eclipse.che.api.project.shared.Constants.LINK_REL_UPDATE_PROJECT;

import com.google.inject.Inject;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.project.server.ProjectService;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;

/** Helps to inject {@link ProjectService} related links. */
@Singleton
public class ProjectServiceLinksInjector {
  @Inject
  public ProjectServiceLinksInjector() {}

  /**
   * Adds links for working with a file. Operations which are supported:
   *
   * <p>get content
   *
   * <p>update content
   *
   * <p>delete
   *
   * @param itemReference information about node
   * @param serviceContext context of {@link ProjectService}
   * @return node with injected file's links
   */
  public ItemReference injectFileLinks(ItemReference itemReference, ServiceContext serviceContext) {
    final UriBuilder uriBuilder = getUriBuilder(serviceContext);
    final List<Link> links = new ArrayList<>();
    final String relPath = itemReference.getPath().substring(1);

    links.add(
        createLink(
            GET,
            tuneUrl(
                uriBuilder
                    .clone()
                    .path(ProjectService.class)
                    .path(ProjectService.class, "getFile")
                    .build(new String[] {relPath}, false)),
            APPLICATION_JSON,
            LINK_REL_GET_CONTENT));
    links.add(
        createLink(
            PUT,
            tuneUrl(
                uriBuilder
                    .clone()
                    .path(ProjectService.class)
                    .path(ProjectService.class, "updateFile")
                    .build(new String[] {relPath}, false)),
            MediaType.WILDCARD,
            null,
            LINK_REL_UPDATE_CONTENT));
    links.add(
        createLink(
            DELETE,
            tuneUrl(
                uriBuilder
                    .clone()
                    .path(ProjectService.class)
                    .path(ProjectService.class, "delete")
                    .build(new String[] {relPath}, false)),
            LINK_REL_DELETE));

    return itemReference.withLinks(links);
  }

  /**
   * Adds links for working with a folder. Operations which are supported:
   *
   * <p>get children
   *
   * <p>get tree
   *
   * <p>delete
   *
   * @param itemReference information about node
   * @param serviceContext context of {@link ProjectService}
   * @return node with injected folder's links
   */
  public ItemReference injectFolderLinks(
      ItemReference itemReference, ServiceContext serviceContext) {
    final UriBuilder uriBuilder = getUriBuilder(serviceContext);
    final List<Link> links = new ArrayList<>();
    final String relPath = itemReference.getPath().substring(1);

    links.add(
        createLink(
            GET,
            tuneUrl(
                uriBuilder
                    .clone()
                    .path(ProjectService.class)
                    .path(ProjectService.class, "getChildren")
                    .build(new String[] {relPath}, false)),
            APPLICATION_JSON,
            LINK_REL_CHILDREN));
    links.add(
        createLink(
            GET,
            tuneUrl(
                uriBuilder
                    .clone()
                    .path(ProjectService.class)
                    .path(ProjectService.class, "getTree")
                    .build(new String[] {relPath}, false)),
            APPLICATION_JSON,
            LINK_REL_TREE));
    links.add(
        createLink(
            DELETE,
            tuneUrl(
                uriBuilder
                    .clone()
                    .path(ProjectService.class)
                    .path(ProjectService.class, "delete")
                    .build(new String[] {relPath}, false)),
            LINK_REL_DELETE));

    return itemReference.withLinks(links);
  }

  /**
   * Adds links for working with a project. Operations which are supported:
   *
   * <p>get tree
   *
   * <p>get children
   *
   * <p>update project
   *
   * <p>delete
   *
   * @param projectConfig information about project
   * @param serviceContext context of {@link ProjectService}
   * @return node with injected project's links
   */
  public ProjectConfigDto injectProjectLinks(
      ProjectConfigDto projectConfig, ServiceContext serviceContext) {
    final UriBuilder uriBuilder = getUriBuilder(serviceContext);
    final List<Link> links = new ArrayList<>();
    final String relPath = projectConfig.getPath().substring(1);

    links.add(
        createLink(
            PUT,
            tuneUrl(
                uriBuilder
                    .clone()
                    .path(ProjectService.class)
                    .path(ProjectService.class, "updateProject")
                    .build(new String[] {relPath}, false)),
            APPLICATION_JSON,
            APPLICATION_JSON,
            LINK_REL_UPDATE_PROJECT));
    links.add(
        createLink(
            GET,
            tuneUrl(
                uriBuilder
                    .clone()
                    .path(ProjectService.class)
                    .path(ProjectService.class, "getChildren")
                    .build(new String[] {relPath}, false)),
            APPLICATION_JSON,
            LINK_REL_CHILDREN));
    links.add(
        createLink(
            GET,
            tuneUrl(
                uriBuilder
                    .clone()
                    .path(ProjectService.class)
                    .path(ProjectService.class, "getTree")
                    .build(new String[] {relPath}, false)),
            APPLICATION_JSON,
            LINK_REL_TREE));
    links.add(
        createLink(
            DELETE,
            tuneUrl(
                uriBuilder
                    .clone()
                    .path(ProjectService.class)
                    .path(ProjectService.class, "delete")
                    .build(new String[] {relPath}, false)),
            LINK_REL_DELETE));

    return projectConfig.withLinks(links);
  }

  /** @return base URI of context of {@link ProjectService} */
  protected UriBuilder getUriBuilder(ServiceContext serviceContext) {
    return serviceContext.getBaseUriBuilder();
  }

  /** Modifies uri, needs for hosted version. */
  protected String tuneUrl(URI uri) {
    return uri.toString();
  }
}
