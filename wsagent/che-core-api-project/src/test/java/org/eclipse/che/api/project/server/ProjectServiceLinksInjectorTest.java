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
package org.eclipse.che.api.project.server;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.api.project.shared.Constants.LINK_REL_CHILDREN;
import static org.eclipse.che.api.project.shared.Constants.LINK_REL_DELETE;
import static org.eclipse.che.api.project.shared.Constants.LINK_REL_GET_CONTENT;
import static org.eclipse.che.api.project.shared.Constants.LINK_REL_TREE;
import static org.eclipse.che.api.project.shared.Constants.LINK_REL_UPDATE_CONTENT;
import static org.eclipse.che.api.project.shared.Constants.LINK_REL_UPDATE_PROJECT;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.List;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.project.server.impl.ProjectServiceLinksInjector;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(value = {MockitoTestNGListener.class})
public class ProjectServiceLinksInjectorTest {
  private static final String PROJECT_PATH = "/project_path";
  private static final String FOLDER_PATH = "/project1/folder";
  private static final String FILE_PATH = "/project1/folder/file";

  @Mock private ServiceContext serviceContext;

  private ProjectServiceLinksInjector projectServiceLinksInjector;

  @BeforeMethod
  public void setUp() throws Exception {
    UriBuilder uriBuilder = UriBuilder.fromPath("localhost:8080");

    when(serviceContext.getBaseUriBuilder()).thenReturn(uriBuilder);

    projectServiceLinksInjector = new ProjectServiceLinksInjector();
  }

  @Test
  public void verifyProjectLinks() throws Exception {
    ProjectConfigDto projectConfigDto = DtoFactory.newDto(ProjectConfigDto.class);
    projectConfigDto.withPath(PROJECT_PATH);

    ProjectConfigDto result =
        projectServiceLinksInjector.injectProjectLinks(projectConfigDto, serviceContext);

    final List<Link> links = result.getLinks();
    assertEquals(4, links.size());

    final Link updateProjectLink = links.get(0);
    assertNotNull(updateProjectLink);
    assertEquals("localhost:8080/project/project_path", updateProjectLink.getHref());
    assertEquals(HttpMethod.PUT, updateProjectLink.getMethod());
    assertEquals(LINK_REL_UPDATE_PROJECT, updateProjectLink.getRel());
    assertEquals(APPLICATION_JSON, updateProjectLink.getConsumes());
    assertEquals(APPLICATION_JSON, updateProjectLink.getProduces());

    final Link childrenProjectLink = links.get(1);
    assertNotNull(childrenProjectLink);
    assertEquals("localhost:8080/project/children/project_path", childrenProjectLink.getHref());
    assertEquals(HttpMethod.GET, childrenProjectLink.getMethod());
    assertEquals(LINK_REL_CHILDREN, childrenProjectLink.getRel());
    assertEquals(APPLICATION_JSON, childrenProjectLink.getProduces());

    final Link treeProjectLink = links.get(2);
    assertNotNull(treeProjectLink);
    assertEquals("localhost:8080/project/tree/project_path", treeProjectLink.getHref());
    assertEquals(HttpMethod.GET, treeProjectLink.getMethod());
    assertEquals(LINK_REL_TREE, treeProjectLink.getRel());
    assertEquals(APPLICATION_JSON, treeProjectLink.getProduces());

    final Link deleteProjectLink = links.get(3);
    assertNotNull(deleteProjectLink);
    assertEquals("localhost:8080/project/project_path", deleteProjectLink.getHref());
    assertEquals(HttpMethod.DELETE, deleteProjectLink.getMethod());
    assertEquals(LINK_REL_DELETE, deleteProjectLink.getRel());
  }

  @Test
  public void verifyFolderLinks() throws Exception {
    ItemReference itemReference = DtoFactory.newDto(ItemReference.class);
    itemReference.withPath(FOLDER_PATH);

    ItemReference result =
        projectServiceLinksInjector.injectFolderLinks(itemReference, serviceContext);

    assertEquals(3, result.getLinks().size());

    Link getChildrenLink = result.getLink("children");
    assertNotNull(getChildrenLink);
    assertEquals("localhost:8080/project/children/project1/folder", getChildrenLink.getHref());
    assertEquals(HttpMethod.GET, getChildrenLink.getMethod());
    assertEquals(LINK_REL_CHILDREN, getChildrenLink.getRel());
    assertEquals(APPLICATION_JSON, getChildrenLink.getProduces());

    Link getTreeLink = result.getLink("tree");
    assertNotNull(getTreeLink);
    assertEquals("localhost:8080/project/tree/project1/folder", getTreeLink.getHref());
    assertEquals(HttpMethod.GET, getTreeLink.getMethod());
    assertEquals(LINK_REL_TREE, getTreeLink.getRel());
    assertEquals(APPLICATION_JSON, getTreeLink.getProduces());

    Link deleteLink = result.getLink("delete");
    assertNotNull(deleteLink);
    assertEquals("localhost:8080/project/project1/folder", deleteLink.getHref());
    assertEquals(HttpMethod.DELETE, deleteLink.getMethod());
    assertEquals(LINK_REL_DELETE, deleteLink.getRel());
  }

  @Test
  public void verifyFileLinks() throws Exception {
    ItemReference itemReference = DtoFactory.newDto(ItemReference.class);
    itemReference.withPath(FILE_PATH);

    ItemReference result =
        projectServiceLinksInjector.injectFileLinks(itemReference, serviceContext);

    assertEquals(3, result.getLinks().size());

    Link updateLink = result.getLink("update content");
    assertNotNull(updateLink);
    assertEquals("localhost:8080/project/file/project1/folder/file", updateLink.getHref());
    assertEquals(HttpMethod.PUT, updateLink.getMethod());
    assertEquals(LINK_REL_UPDATE_CONTENT, updateLink.getRel());
    assertEquals(null, updateLink.getProduces());
    assertEquals(MediaType.WILDCARD, updateLink.getConsumes());

    Link getContentLink = result.getLink("get content");
    assertNotNull(getContentLink);
    assertEquals("localhost:8080/project/file/project1/folder/file", getContentLink.getHref());
    assertEquals(HttpMethod.GET, getContentLink.getMethod());
    assertEquals(LINK_REL_GET_CONTENT, getContentLink.getRel());
    assertEquals(APPLICATION_JSON, getContentLink.getProduces());

    Link deleteLink = result.getLink("delete");
    assertNotNull(deleteLink);
    assertEquals("localhost:8080/project/project1/folder/file", deleteLink.getHref());
    assertEquals(HttpMethod.DELETE, deleteLink.getMethod());
    assertEquals(LINK_REL_DELETE, deleteLink.getRel());
  }
}
